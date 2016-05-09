package de.orfap.fap.crawler.crawler;

import de.orfap.fap.crawler.domain.Airline;
import de.orfap.fap.crawler.domain.City;
import de.orfap.fap.crawler.domain.Route;
import org.springframework.hateoas.Resource;

/**
 * Created by Arne on 13.04.2016.
 */
public interface Crawler {
    void getAirlines(String urlToRead) throws Exception;

    void getCities(String urlToRead) throws Exception;

    Resource<Airline> sendAirlineToBackend(Airline airline);

    Resource<City> sendCityToBackend(City city);

    void getRoutes(String urlToRead) throws Exception;

    Resource<Route> sendRoutesToBackend(Route route);
}
