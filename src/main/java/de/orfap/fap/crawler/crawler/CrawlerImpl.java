package de.orfap.fap.crawler.crawler;

import de.orfap.fap.crawler.crawlerpipes.AirlineBuilder;
import de.orfap.fap.crawler.crawlerpipes.AirlineMarketSender;
import de.orfap.fap.crawler.crawlerpipes.Collector;
import de.orfap.fap.crawler.crawlerpipes.CsvFileStringExtractor;
import de.orfap.fap.crawler.crawlerpipes.Downloader;
import de.orfap.fap.crawler.crawlerpipes.FlightBuilder;
import de.orfap.fap.crawler.crawlerpipes.HashMapAdder;
import de.orfap.fap.crawler.crawlerpipes.MarketBuilder;
import de.orfap.fap.crawler.crawlerpipes.RouteBuilder;
import de.orfap.fap.crawler.crawlerpipes.Sender;
import de.orfap.fap.crawler.crawlerpipes.ZipFileStringExtractor;
import de.orfap.fap.crawler.domain.Airline;
import de.orfap.fap.crawler.domain.Market;
import de.orfap.fap.crawler.domain.Route;
import de.orfap.fap.crawler.feign.AirlineClient;
import de.orfap.fap.crawler.feign.MarketClient;
import de.orfap.fap.crawler.feign.RouteClient;
import edu.hm.obreitwi.arch.lab08.Pipe;
import edu.hm.obreitwi.arch.lab08.Pump;
import edu.hm.obreitwi.arch.lab08.Sink;
import edu.hm.obreitwi.arch.lab08.SynchronizedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Arne on 13.04.2016.
 */
@SuppressWarnings({"ALL", "DefaultFileTemplate"})
@Service
public class CrawlerImpl implements Crawler {
    private final Logger LOG = LoggerFactory.getLogger(CrawlerImpl.class);
    private final String airlineURL = "http://transtats.bts.gov/Download_Lookup.asp?Lookup=L_AIRLINE_ID";
    private final String marketURL = "http://www.transtats.bts.gov/Download_Lookup.asp?Lookup=L_CITY_MARKET_ID";
    private final String routeURL = "http://transtats.bts.gov/DownLoad_Table.asp?Table_ID=311&Has_Group=3&Is_Zipped=0";
    private final String flightURL = "http://transtats.bts.gov/DownLoad_Table.asp?Table_ID=236&Has_Group=3&Is_Zipped=0";
    @Value("${fap.backend.basePath}")
    private String basePath;
    private final HashMap<String, Market> markets = new HashMap<>();
    private final HashMap<String, Market> usedMarkets = new HashMap<>();
    private final HashMap<String, Airline> airlines = new HashMap<>();
    private final HashMap<String, Airline> usedAirlines = new HashMap<>();
    private final ArrayList<Route> routes = new ArrayList<>();
    private final ArrayList<Route> flights = new ArrayList<>();
    //Warnings suppressed because of: No beans needed
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private AirlineClient airlineClient;
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private MarketClient marketClient;
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private RouteClient routeClient;
    private Sender<List<Route>> flightSender=new Sender<>(airlineClient,marketClient,routeClient);
    private Sender<List<Route>> routeSender=new Sender<>(airlineClient,marketClient,routeClient);

    @Override
    public Thread getAirlines() throws Exception {
        //AirlinePipe:
        String airlineFilename = "airlines.csv";
        new Downloader<>(airlineURL, 0, 0, "csv", airlineFilename);
        Pump<String> airlinePump = new Pump<>();
        airlinePump.use(new CsvFileStringExtractor(airlineFilename))
                .connect(new Pipe<>())
                .connect(new AirlineBuilder(false, basePath))
                .connect(new Pipe<>())
                .connect(new HashMapAdder<>(airlines));
        airlinePump.interrupt();
        return airlinePump;
    }

    @Override
    public Thread getMarkets() throws Exception {
        //MarketPipe:
        String marketFilename = "markets.csv";
        new Downloader<>(marketURL, 0, 0, "csv", marketFilename);
        Pump<String> marketPump = new Pump<>();
        marketPump.use(new CsvFileStringExtractor(marketFilename))
                .connect(new Pipe<>())
                .connect(new MarketBuilder(false, basePath))
                .connect(new Pipe<>())
                .connect(new HashMapAdder<>(markets));
        marketPump.interrupt();
        return marketPump;
    }

    @Override
    public Thread getRoutes(final int usedYear, final int month) throws Exception {
        //RoutePipe:
        Pump<String> pump = new Pump<>();
        Sink<List<Route>> sink = new Sink<>();
        String routeFilename = "routes-" + usedYear + "-" + month + ".zip";
        String downloadfileType = "zip";
        new Downloader<>(routeURL, usedYear, month, downloadfileType, routeFilename);
        pump.use(new ZipFileStringExtractor(routeFilename))
                .connect(new Pipe<>())
                .connect(new RouteBuilder(true, basePath))
                .connect(new Pipe<>())
                .connect(new AirlineMarketSender<>(airlines, usedAirlines, markets, usedMarkets, airlineClient, marketClient))
                .connect(new SynchronizedQueue<>())
                .connect(new Collector<>())
                .connect(new Pipe<>())
                .connect(sink.use(routeSender));
        pump.interrupt();
        sink.interrupt();
        LOG.info("Started RouteCrawlThread for month: " + month);
        return sink;
    }

    @Override
    public Thread getFlights(final int usedYear, final int month) throws Exception {
        //FlightPipe:
        Pump<String> pump = new Pump<>();
        Sink<List<Route>> sink = new Sink<>();
        String flightFilename = "flights-" + usedYear + "-" + month + ".zip";
        String downloadfileType = "zip";
        new Downloader<>(flightURL, usedYear, month, downloadfileType, flightFilename);
        pump.use(new ZipFileStringExtractor(flightFilename))
                .connect(new Pipe<>())
                .connect(new FlightBuilder(true, basePath))
                .connect(new Pipe<>())
                .connect(new AirlineMarketSender<>(airlines, usedAirlines, markets, usedMarkets, airlineClient, marketClient))
                .connect(new SynchronizedQueue<>())
                .connect(new Collector<>())
                .connect(new Pipe<>())
                .connect(sink.use(flightSender));
        pump.interrupt();
        sink.interrupt();
        LOG.info("Started FlightCrawlThread for month: " + month);
        return sink;
    }

    @Override
    public RouteClient getRouteClient() {
        return routeClient;
    }
}
