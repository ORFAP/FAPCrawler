package de.orfap.fap.crawler.controller;

import de.orfap.fap.crawler.crawlerpipes.Downloader;
import de.orfap.fap.crawler.crawlerpipes.ResourceBuilder;
import de.orfap.fap.crawler.crawlerpipes.Sender;
import de.orfap.fap.crawler.crawlerpipes.Unzipper;
import de.orfap.fap.crawler.domain.Airline;
import edu.hm.obreitwi.arch.lab08.Pipe;
import edu.hm.obreitwi.arch.lab08.Pump;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

/**
 * Created by Arne on 15.05.2016.
 */
@SuppressWarnings("DefaultFileTemplate")
@RestController
public class CrawlerController {

    @RequestMapping(value = "/crawlIntoBackend", method = RequestMethod.GET)
    public void crawlIntoBackend(@Param("year") String year) throws Exception {
        int usedYear;
        try {
            usedYear = Integer.parseInt(year);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("year must be a numerical value");
        }
        //AirlinePipe:
        Pump AirlinePump = new Pump<String>();
        Unzipper<File, String> AirlineUnzipper = new Unzipper<>(new File("L_AIRLINE_ID.csv"), "");
        ResourceBuilder<String, Airline> rbfa = new ResourceBuilder<>("", new Airline());
        Downloader<File> AirlineDownloader = new Downloader<>("http://transtats.bts.gov/Download_Lookup.asp?Lookup=L_AIRLINE_ID", new File(""));
        Sender<Airline> AirlineSender = new Sender<>();
        //noinspection unchecked,unchecked,unchecked,unchecked,unchecked,unchecked,unchecked
        AirlinePump.use(AirlineDownloader)
                .connect(new Pipe<>())
                .connect(AirlineUnzipper)
                .connect(new Pipe<>())
                .connect(rbfa)
                .connect(new Pipe<>())
                .connect(AirlineSender);
        AirlinePump.interrupt();
        //MarketPipe:
        //Pump MarketPump = new Pump<String>();
        //Downloader<File> MarketDownloader = new Downloader<>("http://transtats.bts.gov/Download_Lookup.asp?Lookup=L_CITY_MARKET_ID", new File(""));
    }
}
