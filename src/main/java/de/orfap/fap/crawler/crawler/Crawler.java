package de.orfap.fap.crawler.crawler;

import de.orfap.fap.crawler.domain.Airline;
import de.orfap.fap.crawler.domain.Market;
import de.orfap.fap.crawler.domain.Route;
import org.springframework.hateoas.Resource;

/**
 * Created by Arne on 13.04.2016.
 */
public interface Crawler {
    void getAirlines(String urlToRead) throws Exception;

    void getMarkets(String urlToRead) throws Exception;

    Resource<Airline> sendAirlineToBackend(Airline airline);

    Resource<Market> sendCityToBackend(Market market);

    void getRoutes(String urlToRead) throws Exception;

    Resource<Route> sendRoutesToBackend(Route route);
}
