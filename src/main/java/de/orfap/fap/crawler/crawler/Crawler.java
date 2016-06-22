package de.orfap.fap.crawler.crawler;

import java.util.List;

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
     * @param usedYear      the year to be crawled
     * @param startMonth month to crawl
     * @param endMonth
     * @throws Exception
     */
    List<Thread> getRoutes(int usedYear, int startMonth, int endMonth) throws Exception;

    /**
     * Crawlsings the flights, as in crawling & parsing.
     *
     * @param usedYear      the year to be crawled
     * @param startMonth month to crawl
     * @param endMonth
     * @throws Exception
     */
    List<Thread> getFlights(int usedYear, int startMonth, int endMonth) throws Exception;
}
