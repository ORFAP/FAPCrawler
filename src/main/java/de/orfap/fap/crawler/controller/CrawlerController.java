package de.orfap.fap.crawler.controller;

import de.orfap.fap.crawler.crawler.Crawler;
import de.orfap.fap.crawler.crawlerpipes.Downloader;
import de.orfap.fap.crawler.crawlerpipes.ResourceBuilder;
import de.orfap.fap.crawler.crawlerpipes.Sender;
import de.orfap.fap.crawler.crawlerpipes.Unzipper;
import de.orfap.fap.crawler.domain.Route;
import edu.hm.obreitwi.arch.lab08.Pipe;
import edu.hm.obreitwi.arch.lab08.Pump;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.zip.ZipFile;

/**
 * Created by Arne on 15.05.2016.
 */
@SuppressWarnings("DefaultFileTemplate")
@RestController
public class CrawlerController {
    @Autowired
    Sender<Route> flightSender;
    @Autowired
    Crawler crawler;
    private final Logger LOG = LoggerFactory.getLogger(CrawlerController.class);

    @RequestMapping(value = "/crawlIntoBackend", method = RequestMethod.GET)
    public void crawlIntoBackend(@Param("year") String year, @Param("month") String month) throws Exception {
        int usedYear;
        int startMonth;
        int endMonth;
        try {
            if (month.contains("-")) {
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
        if (startMonth == 1) {
            crawler.getAirlines("http://transtats.bts.gov/Download_Lookup.asp?Lookup=L_AIRLINE_ID");
            crawler.getMarkets("http://www.transtats.bts.gov/Download_Lookup.asp?Lookup=L_CITY_MARKET_ID");
            crawler.getRoutes("http://transtats.bts.gov/DownLoad_Table.asp?Table_ID=311&Has_Group=3&Is_Zipped=0", usedYear);
            crawler.sendDataToBackend();
        }
        //FlightPipe:
        for (int i = startMonth; i <= endMonth; i++) {
            String filename = "flights-" + usedYear + "-" + i + ".zip";
            String downloadfileType = "zip";
            Pump<String> flightPump = new Pump<>();
            Downloader<ZipFile> flightDownloader = new Downloader<>("http://transtats.bts.gov/DownLoad_Table.asp?Table_ID=236&Has_Group=3&Is_Zipped=0", usedYear, i, downloadfileType, filename);
            ResourceBuilder<String, Route> rbsf = new ResourceBuilder<>("", new Route());
            flightPump.use(new Unzipper<>(downloadfileType, filename, ""))
                    .connect(new Pipe<>())
                    .connect(rbsf)
                    .connect(new Pipe<>())
                    .connect(flightSender);
            flightPump.interrupt();
            LOG.info("Started FlightCrawlThread#" + i);
            if (i % 6 == 0) {
                LOG.info("Waiting for completion of FlightCrawlThread#" + i);
                flightPump.join();
            }
        }
    }
}
