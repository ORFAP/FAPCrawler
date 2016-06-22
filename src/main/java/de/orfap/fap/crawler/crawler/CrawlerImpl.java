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
    @Autowired
    Sender<List<Route>> flightSender;

    @Override
    public Thread getAirlines() throws Exception {
        //AirlinePipe:
        String airlineFilename = "airlines.csv";
        new Downloader<>(airlineURL, 0, 0, "csv", airlineFilename);
        AirlineBuilder rbsa = new AirlineBuilder(false, basePath);
        Pump<String> airlinePump = new Pump<>();
        airlinePump.use(new CsvFileStringExtractor(airlineFilename))
                .connect(new Pipe<>())
                .connect(rbsa)
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
        MarketBuilder rbsm = new MarketBuilder(false, basePath);
        Pump<String> marketPump = new Pump<>();
        marketPump.use(new CsvFileStringExtractor(marketFilename))
                .connect(new Pipe<>())
                .connect(rbsm)
                .connect(new Pipe<>())
                .connect(new HashMapAdder<>(markets));
        marketPump.interrupt();
        return marketPump;
    }

    @Override
    public List<Thread> getRoutes(int usedYear, int startMonth, int endMonth) throws Exception {
        //RoutePipe:
        ArrayList<Pump<String>> routePumps = new ArrayList<>();
        ArrayList<Sink<List<Route>>> routeSinks = new ArrayList<>();
        for (int i = startMonth; i <= endMonth; i++) {
            if (routeClient.isRouteInMonthOfYear(usedYear + "-" + i)) {
                continue;
            }
            routePumps.add(new Pump<>());
            routeSinks.add(new Sink<>());

            String routeFilename = "routes-" + usedYear + "-" + i + ".zip";
            String downloadfileType = "zip";
            new Downloader<>(routeURL, usedYear, i, downloadfileType, routeFilename);
            RouteBuilder rbsr = new RouteBuilder(true, basePath);
            routePumps.get(i - startMonth).use(new ZipFileStringExtractor(routeFilename))
                    .connect(new Pipe<>())
                    .connect(rbsr)
                    .connect(new Pipe<>())
                    .connect(new AirlineMarketSender<>(airlines, usedAirlines, markets, usedMarkets, airlineClient, marketClient))
                    .connect(new SynchronizedQueue<>())
                    .connect(new Collector<>())
                    .connect(new Pipe<>())
                    .connect(routeSinks.get(i - startMonth).use(flightSender));
            routePumps.get(i - startMonth).interrupt();
            routeSinks.get(i - startMonth).interrupt();
            LOG.info("Started RouteCrawlThread#" + i);
            if ((i - startMonth) % 4 == 0) {
                LOG.info("Waiting for RouteCrawlThread#" + i);
                routePumps.get(i - startMonth).join();
            }
        }
        return (List) routeSinks;
    }

    @Override
    public List<Thread> getFlights(int usedYear, int startMonth, int endMonth) throws Exception {
        //FlightPipe:
        ArrayList<Pump<String>> flightPumps = new ArrayList<>();
        ArrayList<Sink<List<Route>>> flightSinks = new ArrayList<>();
        for (int i = startMonth; i <= endMonth; i++) {
            if (routeClient.isRouteInMonthOfYear(usedYear + "-" + i)) {
                continue;
            }
            flightPumps.add(new Pump<>());
            flightSinks.add(new Sink<>());
            String flightFilename = "flights-" + usedYear + "-" + i + ".zip";
            String downloadfileType = "zip";
            new Downloader<>(flightURL, usedYear, i, downloadfileType, flightFilename);
            FlightBuilder rbsf = new FlightBuilder(true, basePath);
            flightPumps.get(i - startMonth).use(new ZipFileStringExtractor(flightFilename))
                    .connect(new Pipe<>())
                    .connect(rbsf)
                    .connect(new Pipe<>())
                    .connect(new AirlineMarketSender<>(airlines, usedAirlines, markets, usedMarkets, airlineClient, marketClient))
                    .connect(new SynchronizedQueue<>())
                    .connect(new Collector<>())
                    .connect(new Pipe<>())
                    .connect(flightSinks.get(i - startMonth).use(flightSender));
            flightPumps.get(i - startMonth).interrupt();
            flightSinks.get(i - startMonth).interrupt();
            LOG.info("Started FlightCrawlThread#" + i);
            if ((i - startMonth) % 4 == 0) {
                LOG.info("Waiting for FlightCrawlThread#" + i);
                flightPumps.get(i - startMonth).join();
            }
        }
        return (List) flightSinks;
    }

}
