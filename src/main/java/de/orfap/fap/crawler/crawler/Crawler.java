package de.orfap.fap.crawler.crawler;

import de.orfap.fap.crawler.domain.Airline;
import de.orfap.fap.crawler.domain.Market;
import de.orfap.fap.crawler.domain.Route;
import org.springframework.hateoas.Resource;

/**
 * Created by Arne on 13.04.2016.
 */
@SuppressWarnings("DefaultFileTemplate")
public interface Crawler {
    /**
     * Crawlsings the airlines, as in crawling & parsing.
     *
     * @param urlToRead where to read
     * @throws Exception
     */
    void getAirlines(@SuppressWarnings("SameParameterValue") String urlToRead) throws Exception;

    /**
     * Crawlsings the markets, as in crawling & parsing.
     *
     * @param urlToRead where to read
     * @throws Exception
     */
    void getMarkets(@SuppressWarnings("SameParameterValue") String urlToRead) throws Exception;

    /**
     * Crawlsings the routes, as in crawling & parsing.
     *
     * @param urlToRead where to read
     * @param year      the year to be crawled
     * @throws Exception
     */
    void getRoutes(@SuppressWarnings("SameParameterValue") String urlToRead, int year) throws Exception;

    /**
     * Crawlsings the Flights, as in crawling & parsing.
     *
     * @param urlToRead where to read
     * @param year      the year to be crawled
     * @throws Exception
     */
    void getFlights(String urlToRead, int year) throws Exception;

    /**
     * Sends the crawlsinged (as in crawled & parsed) airlines, markets & routes to the backend.
     * Airlines & markets not mentioned in the routes are not sent to the backend.
     */
    void sendDataToBackend();

    /**
     * Sends an airline to the backend.
     *
     * @param airline to be sent
     * @return link to created airline
     */
    Resource<Airline> sendAirlineToBackend(Airline airline);

    /**
     * Sends a market to the backend.
     *
     * @param market to be sent
     * @return link to the created market
     */
    Resource<Market> sendMarketToBackend(Market market);

    /**
     * Sends a route to the backend.
     *
     * @param route to be sent
     * @return link to the created route
     */
    Resource<Route> sendRoutesToBackend(Route route);
}
