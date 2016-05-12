package de.orfap.fap.crawler.crawler;

import de.orfap.fap.crawler.domain.Airline;
import de.orfap.fap.crawler.domain.Market;
import de.orfap.fap.crawler.domain.Route;
import org.springframework.hateoas.Resource;

/**
 * Created by Arne on 13.04.2016.
 */
public interface Crawler {
    /**
     * Crawlsings the airlines, as in crawling & parsing.
     * @param urlToRead where to read
     * @throws Exception
     */
    void getAirlines(String urlToRead) throws Exception;

    /**
     * Crawlsing the markets, as in crawling & parsing.
     * @param urlToRead where to read
     * @throws Exception
     */
    void getMarkets(String urlToRead) throws Exception;

    /**
     * Crawlsing the routes, as in crawling & parsing.
     * @param urlToRead where to read
     * @throws Exception
     */
    void getRoutes(String urlToRead) throws Exception;

    /**
     * Sends the crawsinged (as in crawled & parsed) airlines, markets & routes to the backend.
     * Airlines & markets not mentioned in the routes are not sent to the backend.
     */
    public void sendDataToBackend();
    /**
     * Sends an airline to the backend.
     * @param airline
     * @return
     */
    Resource<Airline> sendAirlineToBackend(Airline airline);

    /**
     * Sends a market to the backend.
     * @param market
     * @return
     */
    Resource<Market> sendMarketToBackend(Market market);

    /**
     * Sends a route to the backend.
     * @param route
     * @return
     */
    Resource<Route> sendRoutesToBackend(Route route);
}
