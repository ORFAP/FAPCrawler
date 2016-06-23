package de.orfap.fap.crawler.crawler;

import de.orfap.fap.crawler.crawlerpipes.*;
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
import org.springframework.hateoas.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Arne on 13.04.2016.
 */
@SuppressWarnings({"ALL", "DefaultFileTemplate"})
public class CrawlerImpl implements Crawler {
    private final Logger LOG = LoggerFactory.getLogger(CrawlerImpl.class);
    private final String airlineURL = "http://transtats.bts.gov/Download_Lookup.asp?Lookup=L_AIRLINE_ID";
    private final String marketURL = "http://www.transtats.bts.gov/Download_Lookup.asp?Lookup=L_CITY_MARKET_ID";
    private final String routeURL = "http://transtats.bts.gov/DownLoad_Table.asp?Table_ID=311&Has_Group=3&Is_Zipped=0";
    private final String flightURL = "http://transtats.bts.gov/DownLoad_Table.asp?Table_ID=236&Has_Group=3&Is_Zipped=0";

    private HashMap<String, Market> markets = new HashMap<>();
    private Set<String> usedMarkets;
    private HashMap<String, Airline> airlines = new HashMap<>();
    private Set<String> usedAirlines;

    private final AirlineClient airlineClient;
    private final MarketClient marketClient;
    private final RouteClient routeClient;
    private final String basePath;

    public CrawlerImpl(AirlineClient airlineClient, MarketClient marketClient, RouteClient routeClient, String basePath) {
        this.airlineClient = airlineClient;
        this.marketClient = marketClient;
        this.routeClient = routeClient;
        this.basePath = basePath;
    }

    @Override
    public Thread getAirlines() throws Exception {
        //AirlinePipe:
        usedAirlines = airlineClient.findAll()
            .getContent().stream()
            .map(Resource::getId)
            .map(idLink -> {
                String[] split = idLink.getHref().split("/");
                return split[split.length - 1];
            })
            .collect(Collectors.toSet());

        String airlineFilename = "airlines.csv";
//        new Downloader<>(airlineURL, 0, 0, "csv", airlineFilename);
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
        usedMarkets = marketClient.findAll()
            .getContent().stream()
            .map(Resource::getId)
            .map(idLink -> {
                String[] split = idLink.getHref().split("/");
                return split[split.length - 1];
            })
            .collect(Collectors.toSet());

        String marketFilename = "markets.csv";
//        new Downloader<>(marketURL, 0, 0, "csv", marketFilename);
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
//        new Downloader<>(routeURL, usedYear, month, downloadfileType, routeFilename);
        pump.use(new ZipFileStringExtractor(routeFilename))
                .connect(new Pipe<>())
                .connect(new RouteBuilder(true, basePath))
                .connect(new Pipe<>())
                .connect(new FlightFilter())
                .connect(new Pipe<>())
                .connect(new AirlineMarketSender<>(airlines, usedAirlines, markets, usedMarkets, airlineClient, marketClient))
                .connect(new SynchronizedQueue<>())
                .connect(new Collector<>())
                .connect(new Pipe<>())
                .connect(sink.use(new Sender<>(airlineClient,marketClient,routeClient)));
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
//        new Downloader<>(flightURL, usedYear, month, downloadfileType, flightFilename);
        pump.use(new ZipFileStringExtractor(flightFilename))
                .connect(new Pipe<>())
                .connect(new FlightBuilder(true, basePath))
                .connect(new Pipe<>())
                .connect(new FlightFilter())
                .connect(new Pipe<>())
                .connect(new AirlineMarketSender<>(airlines, usedAirlines, markets, usedMarkets, airlineClient, marketClient))
                .connect(new SynchronizedQueue<>())
                .connect(new Collector<>())
                .connect(new Pipe<>())
                .connect(sink.use(new Sender<>(airlineClient,marketClient,routeClient)));
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
