package de.orfap.fap.crawler.controller;

import de.orfap.fap.crawler.crawler.Crawler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by Arne on 15.05.2016.
 */
@SuppressWarnings("DefaultFileTemplate")
@RestController
public class CrawlerController {

    @Autowired
    Crawler crawler;
    private final Logger LOG = LoggerFactory.getLogger(CrawlerController.class);
    @Value("${fap.backend.basePath}")
    private String basePath;

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


        Thread airlineCrawlers = crawler.getAirlines();
        Thread marketCrawlers = crawler.getMarkets();
        airlineCrawlers.join();
        marketCrawlers.join();


        List<Thread> routeCrawlers = crawler.getRoutes(usedYear,startMonth,endMonth);
        List<Thread> flightCrawlers = crawler.getFlights(usedYear,startMonth,endMonth);


        routeCrawlers.forEach(crawler -> {
                try {
                    crawler.join();
                } catch (InterruptedException e) {
                    throw new IllegalArgumentException("Not gonna happen");
                }
                LOG.info("Routes Crawler " + routeCrawlers.indexOf(crawler) + " of " + routeCrawlers.size() + " terminated");
        });
        flightCrawlers.forEach(crawler -> {
                try {
                    crawler.join();
                } catch (InterruptedException e) {
                    throw new IllegalArgumentException("Not gonna happen");
                }
                LOG.info("Flights Crawler " + flightCrawlers.indexOf(crawler) + " of " + flightCrawlers.size() + " terminated");
            });
        LOG.info("Crawling of " + year + ", months " + startMonth + "-" + endMonth + " done");
    }
}
