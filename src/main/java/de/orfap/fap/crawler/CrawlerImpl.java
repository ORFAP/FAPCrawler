package de.orfap.fap.crawler;

import de.orfap.fap.crawler.domain.Airline;
import de.orfap.fap.crawler.domain.Airport;
import de.orfap.fap.crawler.feign.AirlineClient;
import de.orfap.fap.crawler.rest.AirlineRestClient;
import de.orfap.fap.crawler.rest.AirportRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Arne on 13.04.2016.
 */
@Service
public class CrawlerImpl implements Crawler {
    @Autowired
    private AirlineRestClient airlineRestClient;

    @Autowired
    private AirportRestClient airportRestClient;

//    @Autowired
//    AirlineClient airlineClient;

    ArrayList<Airport> airports = new ArrayList();

    @Override
    public void getAirlines(String urlToRead) throws Exception{

        BufferedReader rd = getReader(urlToRead);
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
    public void getAirports(String urlToRead) throws Exception {
        BufferedReader rd = getReader(urlToRead);
        String line;
        while ((line = rd.readLine()) != null) {
            if (line.startsWith("\"")) {
                //Kann noch geiler gemacht werden
                String[] parts = line.split(",");
                String id = parts[0].replaceAll("\"","").trim();
                parts[0] = "";
                String name = String.join("",parts).replaceAll("\"","").trim();
                Airport next = new Airport(name,id);
                airports.add(next);
                if(next.getName().contains("Germany")){
                    sendAirportToBackend(next.getId(),next.getName());
                }
            }
        }
        rd.close();
    }

    @Override
    public void sendAirportToBackend(String id, String name) {
        airportRestClient.create(new Airport(name,id));
    }

    @Override
    public void sendAirlineToBackend(String id, String name) {
//        airlineClient.create(new Airline(name,id));
//        airlineRestClient.create(new Airline(name,id));
    }

    private BufferedReader getReader(String urlToRead) throws IOException {
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        return new BufferedReader(new InputStreamReader(conn.getInputStream()));

    }
}
