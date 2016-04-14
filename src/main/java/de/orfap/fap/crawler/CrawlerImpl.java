package de.orfap.fap.crawler;

import de.orfap.fap.crawler.domain.Airline;
import de.orfap.fap.crawler.rest.AirlineRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Arne on 13.04.2016.
 */
@Service
public class CrawlerImpl implements Crawler {
    @Autowired
    private AirlineRestClient airlineRestClient;

    @Override
    public void getAirlines(String urlToRead) throws Exception{

        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            if(line.startsWith("\"")) {
                String[] parts = line.split(",");
                sendAirlineToBackend(parts[0].replaceAll("\"",""),parts[1].replaceAll("(\"|\\([1-9]\\))","").trim());
            }
        }
        rd.close();
    }

    @Override
    public void sendAirlineToBackend(String id, String s) {
        airlineRestClient.create(new Airline(s,id));
    }
}
