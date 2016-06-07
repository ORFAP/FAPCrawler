package de.orfap.fap.crawler.controller;

import de.orfap.fap.crawler.crawlerpipes.Downloader;
import de.orfap.fap.crawler.crawlerpipes.ResourceBuilder;
import de.orfap.fap.crawler.crawlerpipes.Sender;
import de.orfap.fap.crawler.crawlerpipes.Unzipper;
import de.orfap.fap.crawler.domain.Airline;
import de.orfap.fap.crawler.domain.Market;
import de.orfap.fap.crawler.domain.Route;
import edu.hm.obreitwi.arch.lab08.Pipe;
import edu.hm.obreitwi.arch.lab08.Pump;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.zip.ZipFile;

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
        Downloader<File> AirlineDownloader = new Downloader<>("http://transtats.bts.gov/Download_Lookup.asp?Lookup=L_AIRLINE_ID",0, new File(""));
        ResourceBuilder<String, Airline> rbsa = new ResourceBuilder<>("", new Airline());
        Sender<Airline> AirlineSender = new Sender<>();
        //noinspection unchecked,unchecked,unchecked,unchecked,unchecked,unchecked,unchecked
//        AirlinePump.use(AirlineDownloader)
//                .connect(new Pipe<>())
//                .connect(Unzipper<>(new File("L_AIRLINE_ID.csv"), ""))
//                .connect(new Pipe<>())
//                .connect(rbsa)
//                .connect(new Pipe<>())
//                .connect(AirlineSender);
//        AirlinePump.interrupt();
        //MarketPipe:
        Pump MarketPump = new Pump<String>();
        Downloader<File> MarketDownloader = new Downloader<>("http://transtats.bts.gov/Download_Lookup.asp?Lookup=L_CITY_MARKET_ID",0, new File(""));
        ResourceBuilder<String, Market> rbsm = new ResourceBuilder<>("", new Market());
        Sender<Market> MarketSender = new Sender<>();
        MarketPump.use//(MarketDownloader)
                //.connect(new Pipe<>())
                //.connect
                        (new Unzipper<>(new File("L_CITY_MARKET_ID.csv"), ""))
                .connect(new Pipe<>())
                .connect(rbsm)
                .connect(new Pipe<>())
                .connect(MarketSender);
        MarketPump.interrupt();
        //RoutePipe:
        Pump RoutePump = new Pump<String>();
        Downloader<ZipFile> RouteDownloader = new Downloader<>("http://transtats.bts.gov/DownLoad_Table.asp?Table_ID=311&Has_Group=3&Is_Zipped=0", usedYear, new ZipFile(""));
        ResourceBuilder<String, Route> rbsr = new ResourceBuilder<>("", new Route());
        Sender<Route> RouteSender = new Sender<>();
//        RoutePump.use//(RouteDownloader)
//                //.connect(new Pipe<>())
//                //.connect
//                        (new Unzipper<>(new ZipFile("L_CITY_MARKET_ID.csv"), ""))
//                .connect(new Pipe<>())
//                .connect(rbsr)
//                .connect(new Pipe<>())
//                .connect(RouteSender);
        RoutePump.interrupt();
    }
}
