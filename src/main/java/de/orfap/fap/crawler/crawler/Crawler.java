package de.orfap.fap.crawler.crawler;

import de.orfap.fap.crawler.feign.RouteClient;

/**
 * Created by Arne on 13.04.2016.
 */
@SuppressWarnings("DefaultFileTemplate")
public interface Crawler {
    /**
     * Crawlsings the airlines, as in crawling & parsing.
     *
     * @throws Exception
     */
    Thread getAirlines() throws Exception;

    /**
     * Crawlsings the markets, as in crawling & parsing.
     *
     * @throws Exception
     */
    Thread getMarkets() throws Exception;

    /**
     * Crawlsings the routes, as in crawling & parsing.
     *
     * @param usedYear the year to be crawled
     * @param month    month to crawl
     * @throws Exception
     */
    Thread getRoutes(final int usedYear, final int month) throws Exception;

    /**
     * Crawlsings the flights, as in crawling & parsing.
     *
     * @param usedYear the year to be crawled
     * @param month    month to crawl
     * @throws Exception
     */
    Thread getFlights(final int usedYear, final int month) throws Exception;

    RouteClient getRouteClient();
}
