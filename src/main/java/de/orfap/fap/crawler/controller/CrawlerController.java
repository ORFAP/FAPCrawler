package de.orfap.fap.crawler.controller;

import de.orfap.fap.crawler.crawler.Crawler;
import de.orfap.fap.crawler.crawler.CrawlerImpl;
import de.orfap.fap.crawler.domain.CrawlerState;
import de.orfap.fap.crawler.feign.AirlineClient;
import de.orfap.fap.crawler.feign.MarketClient;
import de.orfap.fap.crawler.feign.RouteClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by Arne on 15.05.2016.
 */
@SuppressWarnings("DefaultFileTemplate")
@RestController
public class CrawlerController {

    private final ConcurrentHashMap<String, CrawlerState> CRAWLER_STATE_LIST = new ConcurrentHashMap<String, CrawlerState>();

    private final Logger LOG = LoggerFactory.getLogger(CrawlerController.class);
    @Value("${fap.backend.basePath}")
    private String basePath;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private AirlineClient airlineClient;
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private MarketClient marketClient;
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private RouteClient routeClient;

    @RequestMapping(value = "/status")
    public List<CrawlerState> getCrawlerStatus() {
        List<CrawlerState> crawlerStates = new ArrayList<>();
        synchronized (CRAWLER_STATE_LIST) {
            CRAWLER_STATE_LIST.forEach((s, crawlerState) -> crawlerStates.add(crawlerState));
        }
        crawlerStates.sort(Comparator.comparing(CrawlerState::getStartTime));
        crawlerStates.sort(Comparator.comparing(CrawlerState::isFinished));
        return crawlerStates;
    }

    @RequestMapping(value = "/clearStatus", method = RequestMethod.POST)
    public void clearStatus() {
        synchronized (CRAWLER_STATE_LIST) {
            Collections.list(CRAWLER_STATE_LIST.keys()).forEach(s -> {
                if(CRAWLER_STATE_LIST.get(s).isFinished()) {
                    CRAWLER_STATE_LIST.remove(s);
                }
            });
        }
    }

    @Async
    @RequestMapping(value = "/crawlIntoBackend", method = RequestMethod.GET)
    public void crawlIntoBackend(@Param("year") String year, @Param("month") String month) throws Exception {
        final String THREAD_ID = year + "-" + month + "-" + Thread.currentThread().getId();
        LOG.info("{}: Crawling", THREAD_ID);
        CRAWLER_STATE_LIST.put(THREAD_ID, new CrawlerState(year, month, LocalDateTime.now()));
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
            CRAWLER_STATE_LIST.remove(THREAD_ID);
            throw new IllegalArgumentException("year/month must be a numerical value");
        }

        Crawler crawler = new CrawlerImpl(airlineClient, marketClient, routeClient, basePath, THREAD_ID);

        Thread airlineCrawlers = crawler.getAirlines();
        Thread marketCrawlers = crawler.getMarkets();
        airlineCrawlers.join();
        marketCrawlers.join();

        for (int i = startMonth; i <= endMonth; i++) {
            if (crawler.getRouteClient().isRouteInMonthOfYear(usedYear + "-" + i)) {
                continue;
            }
            Thread routeCrawler = crawler.getRoutes(usedYear, i);
            Thread flightCrawler = crawler.getFlights(usedYear, i);

            routeCrawler.join();
            flightCrawler.join();

        }
        CRAWLER_STATE_LIST.get(THREAD_ID).setFinished(true);
        LOG.info("{}: Crawling done!");
    }
}
