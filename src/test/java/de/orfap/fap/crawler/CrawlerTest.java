package de.orfap.fap.crawler;

import de.orfap.fap.crawler.crawler.Crawler;
import de.orfap.fap.crawler.crawler.CrawlerImpl;
import de.orfap.fap.crawler.domain.Airline;
import de.orfap.fap.crawler.domain.Market;
import de.orfap.fap.crawler.domain.Route;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

/**
 * Created by Arne on 13.06.2016.
 */
public class CrawlerTest {

    private static Crawler crawler;


    private static List<Market> markets;
    private static List<Airline> airlines;
    private static List<Route> routes;

    @BeforeClass
    public static void before() throws Exception {
        crawler = new CrawlerImpl();
        airlines = crawler.getAirlines("http://transtats.bts.gov/Download_Lookup.asp?Lookup=L_AIRLINE_ID");
        markets = crawler.getMarkets("http://www.transtats.bts.gov/Download_Lookup.asp?Lookup=L_CITY_MARKET_ID");
        routes = crawler.getRoutes("http://transtats.bts.gov/DownLoad_Table.asp?Table_ID=311&Has_Group=3&Is_Zipped=0", 2015);
    }

    @Test
    public void parseAirlinesTest(){
        for (Airline airline : airlines){
            Assert.assertNotNull(airline);
            Assert.assertNotNull(airline.getId());
            Assert.assertNotNull(airline.getName());
        }
    }

    @Test
    public void parseMarketsTest(){
        for (Market market : markets){
            Assert.assertNotNull(market);
            Assert.assertNotNull(market.getId());
            Assert.assertNotNull(market.getName());
        }
    }

    @Test
    public void parseRoutessTest(){
        for (Route route : routes){
            Assert.assertNotNull(route);
            Assert.assertNotNull(route.getAirline());
            Assert.assertNotNull(route.getSource());
            Assert.assertNotNull(route.getDestination());
            Assert.assertNotNull(route.getDate());
            Assert.assertTrue(route.getFlightCount()>0);
            Assert.assertTrue(route.getDelays()>=0);
            Assert.assertTrue(route.getPassengerCount()>=0);
            Assert.assertTrue(route.getCancelled()>=0);
            Assert.assertTrue(route.getCancelled()<=route.getFlightCount());
        }
    }
}
