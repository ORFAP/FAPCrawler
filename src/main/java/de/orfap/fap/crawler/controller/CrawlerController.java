package de.orfap.fap.crawler.controller;

import de.orfap.fap.crawler.crawler.Crawler;
import de.orfap.fap.crawler.crawlerpipes.Collector;
import de.orfap.fap.crawler.crawlerpipes.Downloader;
import de.orfap.fap.crawler.crawlerpipes.ResourceBuilder;
import de.orfap.fap.crawler.crawlerpipes.Sender;
import de.orfap.fap.crawler.crawlerpipes.Unzipper;
import de.orfap.fap.crawler.domain.Route;
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
import java.util.List;

/**
 * Created by Arne on 15.05.2016.
 */
@SuppressWarnings("DefaultFileTemplate")
@RestController
public class CrawlerController {
    @Autowired
    Sender<List<Route>> flightSender;
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

        //Crawl airlines, markets and routes
        crawler.getAirlines("http://transtats.bts.gov/Download_Lookup.asp?Lookup=L_AIRLINE_ID");
        crawler.getMarkets("http://www.transtats.bts.gov/Download_Lookup.asp?Lookup=L_CITY_MARKET_ID");
        crawler.getRoutes("http://transtats.bts.gov/DownLoad_Table.asp?Table_ID=311&Has_Group=3&Is_Zipped=0", usedYear);
        //And send them to the backend
        crawler.sendDataToBackend();

        //FlightPipe:
        ArrayList<Pump<String>> pumps = new ArrayList();
        ArrayList<Sink<List<Route>>> sinks = new ArrayList();
        for (int i = startMonth; i <= endMonth; i++) {
            pumps.add(new Pump<>());
            sinks.add(new Sink<>());
            String filename = "flights-" + usedYear + "-" + i + ".zip";
            String downloadfileType = "zip";
            new Downloader<>("http://transtats.bts.gov/DownLoad_Table.asp?Table_ID=236&Has_Group=3&Is_Zipped=0", usedYear, i, downloadfileType, filename);
            ResourceBuilder<String, Route> rbsf = new ResourceBuilder<>("", new Route(), true, basePath);
            pumps.get(i-startMonth).use(new Unzipper<>(downloadfileType, filename, ""))
                    .connect(new Pipe<>())
                    .connect(rbsf)
                    .connect(new SynchronizedQueue<>())
                    .connect(new Collector<>())
                    .connect(new Pipe<>())
                    .connect(sinks.get(i-startMonth).use(flightSender));
            pumps.get(i-startMonth).interrupt();
            sinks.get(i-startMonth).interrupt();
            LOG.info("Started FlightCrawlThread#" + i);
        }
        for (int i = 0; i < sinks.size(); i++) {
            sinks.get(i).join();
            LOG.info("Sink " + (i+1) + " von " + sinks.size() + " beendet");
        }
        LOG.info("Crawling of " + year + " done");
    }
}
