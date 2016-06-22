package de.orfap.fap.crawler.controller;

import de.orfap.fap.crawler.crawlerpipes.AirlineMarketSender;
import de.orfap.fap.crawler.crawlerpipes.AirlineResourceBuilder;
import de.orfap.fap.crawler.crawlerpipes.Collector;
import de.orfap.fap.crawler.crawlerpipes.Downloader;
import de.orfap.fap.crawler.crawlerpipes.FlightResourceBuilder;
import de.orfap.fap.crawler.crawlerpipes.HashMapAdder;
import de.orfap.fap.crawler.crawlerpipes.MarketResourceBuilder;
import de.orfap.fap.crawler.crawlerpipes.ResourceBuilder;
import de.orfap.fap.crawler.crawlerpipes.RouteResourceBuilder;
import de.orfap.fap.crawler.crawlerpipes.Sender;
import de.orfap.fap.crawler.crawlerpipes.StringExtractor;
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
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Arne on 15.05.2016.
 */
@SuppressWarnings("DefaultFileTemplate")
@RestController
public class CrawlerController {
    private final String airlineURL = "http://transtats.bts.gov/Download_Lookup.asp?Lookup=L_AIRLINE_ID";
    private final String marketURL = "http://www.transtats.bts.gov/Download_Lookup.asp?Lookup=L_CITY_MARKET_ID";
    private final String routeURL = "http://transtats.bts.gov/DownLoad_Table.asp?Table_ID=311&Has_Group=3&Is_Zipped=0";
    private final String flightURL = "http://transtats.bts.gov/DownLoad_Table.asp?Table_ID=236&Has_Group=3&Is_Zipped=0";
    private final HashMap<Integer, Market> markets = new HashMap<>();
    private final HashMap<Integer, Market> usedMarkets = new HashMap<>();
    private final HashMap<Integer, Airline> airlines = new HashMap<>();
    private final HashMap<Integer, Airline> usedAirlines = new HashMap<>();@Autowired
    Sender<List<Route>> flightSender;
    private final Logger LOG = LoggerFactory.getLogger(CrawlerController.class);
    @Value("${fap.backend.basePath}")
    private String basePath;
    //Warnings suppressed because of: No beans needed
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    RouteClient routeClient;
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private AirlineClient airlineClient;
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private MarketClient marketClient;

    @RequestMapping(value = "/crawlIntoBackend", method = RequestMethod.GET)
    public void crawlIntoBackend(@Param("year") String year, @Param("month") String month) throws Exception {
        int usedYear;
        int startMonth;
        int endMonth;
        try {
            if (month == null) {
                startMonth = 1;
                endMonth = 12;
            } else if (month.contains("-")) {
                String[] working = month.split("-");
                startMonth = Integer.parseInt(working[0]);
                endMonth = Integer.parseInt(working[1]);
                if (endMonth <= 0 || endMonth > 12) {
                    endMonth = endMonth % 12;
                    endMonth++;
                }
                if (startMonth > endMonth) {
                    throw new IllegalArgumentException("endMonth must be greater startMonth");
                }
            } else {
                startMonth = Integer.parseInt(month);
                endMonth = startMonth;
            }
            if (startMonth <= 0 || startMonth > 12) {
                startMonth = startMonth % 12;
                startMonth++;
            }
            usedYear = Integer.parseInt(year);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("year/month must be a numerical value");
        }

        //AirlinePipe:
        String airlineFilename = "airlines.csv";
        new Downloader<>(airlineURL, usedYear, startMonth, "csv", airlineFilename);
        ResourceBuilder<String, Airline> rbsa = new AirlineResourceBuilder<>(false, basePath);
        Pump<String> airlinePump = new Pump<>();
        airlinePump.use(new StringExtractor<>("csv", airlineFilename, ""))
                .connect(new Pipe<>())
                .connect(rbsa)
                .connect(new Pipe<>())
                .connect(new HashMapAdder<>(airlines));
        airlinePump.interrupt();

        //MarketPipe:
        String marketFilename = "markets.csv";
        new Downloader<>(marketURL, usedYear, startMonth, "csv", marketFilename);
        ResourceBuilder<String, Market> rbsm = new MarketResourceBuilder<>(false, basePath);
        Pump<String> marketPump = new Pump<>();
        marketPump.use(new StringExtractor<>("csv", marketFilename, ""))
                .connect(new Pipe<>())
                .connect(rbsm)
                .connect(new Pipe<>())
                .connect(new HashMapAdder<>(markets));
        marketPump.interrupt();

        airlinePump.join();
        marketPump.join();

        //RoutePipe:
        ArrayList<Pump<String>> routePumps = new ArrayList();
        ArrayList<Sink<List<Route>>> routeSinks = new ArrayList();
        for (int i = startMonth; i <= endMonth; i++) {
            if (routeClient.isRouteInMonthOfYear(usedYear + "-" + i)) {
                continue;
            }
            synchronized (routePumps) {
                routePumps.add(new Pump<>());
                routeSinks.add(new Sink<>());
            }
            String routeFilename = "routes-" + usedYear + "-" + i + ".zip";
            String downloadfileType = "zip";
            new Downloader<>(routeURL, usedYear, i, downloadfileType, routeFilename);
            ResourceBuilder<String, Route> rbsr = new RouteResourceBuilder<>(true, basePath);
            routePumps.get(i - startMonth).use(new StringExtractor<>(downloadfileType, routeFilename, ""))
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
        for (int i = 0; i < routeSinks.size(); i++) {
            routeSinks.get(i).join();
            LOG.info("Sink " + (i + 1) + " of " + routeSinks.size() + " terminated");
        }

        //FlightPipe:
        ArrayList<Pump<String>> flightPumps = new ArrayList();
        ArrayList<Sink<List<Route>>> flightSinks = new ArrayList();
        for (int i = startMonth; i <= endMonth; i++) {
            if (routeClient.isRouteInMonthOfYear(usedYear + "-" + i)) {
                continue;
            }
            synchronized (flightPumps) {
                flightPumps.add(new Pump<>());
                flightSinks.add(new Sink<>());
            }
            String filename = "flights-" + usedYear + "-" + i + ".zip";
            String downloadfileType = "zip";
            new Downloader<>(flightURL, usedYear, i, downloadfileType, filename);
            ResourceBuilder<String, Route> rbsf = new FlightResourceBuilder<>(true, basePath);
            flightPumps.get(i - startMonth).use(new StringExtractor<>(downloadfileType, filename, ""))
                    .connect(new Pipe<>())
                    .connect(rbsf)
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
        for (int i = 0; i < flightSinks.size(); i++) {
            flightSinks.get(i).join();
            LOG.info("Sink " + (i + 1) + " of " + flightSinks.size() + " terminated");
        }
        LOG.info("Crawling of " + year + ", months " + startMonth + "-" + endMonth + " done");
    }
}
