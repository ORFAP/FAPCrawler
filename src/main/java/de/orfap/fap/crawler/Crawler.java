package de.orfap.fap.crawler;

/**
 * Created by Arne on 13.04.2016.
 */
public interface Crawler {
    void getAirlines(String urlToRead) throws Exception;

    void getCities(String urlToRead) throws Exception;

    void sendAirlineToBackend(String id, String name);

    void sendCityToBackend(String id, String name);
}
