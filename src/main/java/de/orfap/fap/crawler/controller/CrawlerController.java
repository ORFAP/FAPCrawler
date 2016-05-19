package de.orfap.fap.crawler.controller;

import de.orfap.fap.crawler.crawler.Crawler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Arne on 15.05.2016.
 */
@RestController
public class CrawlerController {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    Crawler crawler;

    @RequestMapping(value = "/crawlIntoBackend", method = RequestMethod.GET)
    public void crawlIntoBackend(@Param("year") String year) throws Exception {
        int usedYear;
        try {
            usedYear = Integer.parseInt(year);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("year must be a numerical value");
        }
        crawler.getAirlines("http://transtats.bts.gov/Download_Lookup.asp?Lookup=L_AIRLINE_ID");
        crawler.getMarkets("http://www.transtats.bts.gov/Download_Lookup.asp?Lookup=L_CITY_MARKET_ID");
        crawler.getRoutes("http://transtats.bts.gov/DownLoad_Table.asp?Table_ID=311&Has_Group=3&Is_Zipped=0", usedYear);
//        crawler.getFlights("http://transtats.bts.gov/DownLoad_Table.asp?Table_ID=236&Has_Group=3&Is_Zipped=0", usedYear);
        crawler.sendDataToBackend();
    }
}
