package de.orfap.fap.crawler;

/**
 * Created by Arne on 13.04.2016.
 */
public interface Crawler {
    void getAirlines(String urlToRead) throws Exception;

    void sendAirlineToBackend(String tablename, String s);

}
