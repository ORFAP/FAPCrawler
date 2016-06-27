package de.orfap.fap.crawler;

import de.orfap.fap.crawler.crawlerpipes.AirlineBuilder;
import de.orfap.fap.crawler.crawlerpipes.CsvFileStringExtractor;
import de.orfap.fap.crawler.crawlerpipes.FlightBuilder;
import de.orfap.fap.crawler.crawlerpipes.MarketBuilder;
import de.orfap.fap.crawler.crawlerpipes.RouteBuilder;
import de.orfap.fap.crawler.crawlerpipes.ZipFileStringExtractor;
import de.orfap.fap.crawler.domain.Airline;
import de.orfap.fap.crawler.domain.Market;
import de.orfap.fap.crawler.domain.Route;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Arne on 27.06.2016.
 */
public class ResourceBuilderTest {

    @Test
    public void airlineBuilderTest(){
        CsvFileStringExtractor extractor = new CsvFileStringExtractor("src/test/resources/AirlineTest.csv");
        AirlineBuilder airlineBuilder = new AirlineBuilder(true, null);
        String nextLine = extractor.deliver();
        Airline nextAirline;
        while(nextLine != null){
            nextAirline = airlineBuilder.transform(nextLine);
            Assert.assertNotNull(nextAirline);
            Assert.assertNotNull(nextAirline.getId());
            Assert.assertNotNull(nextAirline.getName());
            nextLine = extractor.deliver();
        }
    }

    @Test
    public void marketBuilderTest(){
        CsvFileStringExtractor extractor = new CsvFileStringExtractor("src/test/resources/MarketTest.csv");
        MarketBuilder marketBuilder = new MarketBuilder(true, null);
        String nextLine = extractor.deliver();
        Market nextMarket;
        while(nextLine != null){
            nextMarket = marketBuilder.transform(nextLine);
            Assert.assertNotNull(nextMarket);
            Assert.assertNotNull(nextMarket.getId());
            Assert.assertNotNull(nextMarket.getName());
            nextLine = extractor.deliver();
        }
    }

    @Test
    public void routeBuilderTest(){
        ZipFileStringExtractor extractor = new ZipFileStringExtractor("src/test/resources/RoutesTest.zip");
        RouteBuilder routeBuilder = new RouteBuilder(true, null);
        String nextLine = extractor.deliver();
        Route nextRoute;
        while(nextLine != null){
            nextRoute = routeBuilder.transform(nextLine);
            Assert.assertNotNull(nextRoute);
            Assert.assertNotNull(nextRoute.getAirline());
            Assert.assertNotNull(nextRoute.getDestination());
            Assert.assertNotEquals("", nextRoute.getDestination());
            Assert.assertNotNull(nextRoute.getSource());
            Assert.assertNotEquals("", nextRoute.getSource());
            Assert.assertNotNull(nextRoute.getDate());
            Assert.assertTrue(nextRoute.getCancelled() == 0.0 || nextRoute.getCancelled() == 1.0);
            Assert.assertTrue(0.0 == nextRoute.getDelays());
            Assert.assertTrue(0.0 == nextRoute.getFlightCount());
            Assert.assertTrue(nextRoute.getPassengerCount() >= 0.0);
            nextLine = extractor.deliver();
        }
    }

    @Test
    public void flightBuilderTest(){
        ZipFileStringExtractor extractor = new ZipFileStringExtractor("src/test/resources/FlightsTest.zip");
        FlightBuilder flightBuilder = new FlightBuilder(true, null);
        String nextLine = extractor.deliver();
        Route nextRoute;
        while(nextLine != null){
            nextRoute = flightBuilder.transform(nextLine);
            Assert.assertNotNull(nextRoute);
            Assert.assertNotNull(nextRoute.getAirline());
            Assert.assertNotNull(nextRoute.getDestination());
            Assert.assertNotEquals("", nextRoute.getDestination());
            Assert.assertNotNull(nextRoute.getSource());
            Assert.assertNotEquals("", nextRoute.getSource());
            Assert.assertNotNull(nextRoute.getDate());
            if (nextRoute.getCancelled() == 1.0){
                Assert.assertTrue(0.0 == nextRoute.getFlightCount());
            } else if (nextRoute.getCancelled() == 0.0) {
                Assert.assertTrue(1.0 == nextRoute.getFlightCount());
            } else {
                Assert.fail("Route cancelled invalid value.");
            }
            Assert.assertTrue(nextRoute.getDelays() >= 0.0);
            Assert.assertTrue(nextRoute.getPassengerCount() == 0.0);
            nextLine = extractor.deliver();
        }
    }
}
