package de.orfap.fap.crawler;

/**
 * Created by Arne on 13.04.2016.
 */
public interface Crawler {
    void getHTML(String urlToRead) throws Exception;

    void sendToBackend(String tablename, String s);

}
