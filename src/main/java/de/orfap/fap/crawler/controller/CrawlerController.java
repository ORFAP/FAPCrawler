package de.orfap.fap.crawler.controller;

import de.orfap.fap.crawler.crawler.Crawler;
import de.orfap.fap.crawler.crawlerpipes.Downloader;
import de.orfap.fap.crawler.crawlerpipes.ResourceBuilder;
import de.orfap.fap.crawler.crawlerpipes.Sender;
import de.orfap.fap.crawler.crawlerpipes.Unzipper;
import de.orfap.fap.crawler.domain.Route;
import edu.hm.obreitwi.arch.lab08.Pipe;
import edu.hm.obreitwi.arch.lab08.Pump;
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

    @RequestMapping(value = "/crawlIntoBackend", method = RequestMethod.GET)
    public void crawlIntoBackend(@Param("year") String year, @Param("month") String month) throws Exception {
        int usedYear;
        int usedMonth;
        try {
            usedMonth = Integer.parseInt(month);
            if (usedMonth <= 0 || usedMonth > 12) {
                usedMonth = usedMonth % 12;
                usedMonth++;
            }
            usedYear = Integer.parseInt(year);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("year/month must be a numerical value");
        }
//        crawler.getAirlines("http://transtats.bts.gov/Download_Lookup.asp?Lookup=L_AIRLINE_ID");
//        crawler.getMarkets("http://www.transtats.bts.gov/Download_Lookup.asp?Lookup=L_CITY_MARKET_ID");
//        crawler.getRoutes("http://transtats.bts.gov/DownLoad_Table.asp?Table_ID=311&Has_Group=3&Is_Zipped=0", usedYear);
//        crawler.sendDataToBackend();
        //FlightPipe:
        String filename = "flights-"+usedYear+"-"+usedMonth+".zip";
        String downloadfileType = "zip";
        Pump flightPump = new Pump<String>();
        Downloader<ZipFile> flightDownloader = new Downloader<>("http://transtats.bts.gov/DownLoad_Table.asp?Table_ID=236&Has_Group=3&Is_Zipped=0", usedYear, usedMonth, downloadfileType, filename);
        ResourceBuilder<String, Route> rbsf = new ResourceBuilder<>("", new Route());
        flightPump.use(flightDownloader)
                .connect(new Pipe<>())
                .connect(new Unzipper<>(downloadfileType, filename, ""))
                .connect(new Pipe<>())
                .connect(rbsf)
                .connect(new Pipe<>())
                .connect(flightSender);
        flightPump.interrupt();
    }
}
