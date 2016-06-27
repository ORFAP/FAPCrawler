package de.orfap.fap.crawler;

import de.orfap.fap.crawler.crawler.Crawler;
import de.orfap.fap.crawler.crawler.CrawlerImpl;
import de.orfap.fap.crawler.domain.Airline;
import de.orfap.fap.crawler.domain.Market;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arne on 13.06.2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class CrawlerTest {

//    @Autowired
    private static Crawler crawler;

    private static List<Market> markets;
    private static List<Airline> airlines;

    @BeforeClass
    public static void before() throws Exception {
        /*crawler = new CrawlerImpl(null,null,null,null);
        crawler.getAirlines();
        crawler.getMarkets();
        airlines = new ArrayList<>(crawler.getAirlineList().values());
        markets = new ArrayList<>(crawler.getMarketList().values());*/
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

    /*@Test
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
    }*/
}
