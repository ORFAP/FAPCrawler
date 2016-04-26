package de.orfap.fap.crawler.crawler;

import de.orfap.fap.crawler.domain.Airline;
import de.orfap.fap.crawler.domain.City;
import de.orfap.fap.crawler.feign.AirlineClient;
import de.orfap.fap.crawler.feign.CityClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Arne on 13.04.2016.
 */
@Service
public class CrawlerImpl implements Crawler {
  @Autowired
  private AirlineClient airlineClient;

  @Autowired
  private CityClient cityClient;

  ArrayList<City> cities = new ArrayList();

  @Override
  public void getAirlines(String urlToRead) throws Exception {
    System.out.println("STARTING CREATION AIRLINES");
    BufferedReader rd = getReader(urlToRead);
    String line;
    while ((line = rd.readLine()) != null) {
      if (line.startsWith("\"")) {
        String[] parts = line.split(",");
        sendAirlineToBackend(parts[0].replaceAll("\"", ""), parts[1].replaceAll("(\"|\\([1-9]\\))", "").trim());
      }
    }
    rd.close();

    System.out.println("CREATION DONE");
    List<Airline> resources = airlineClient.findAll()
        .getContent().stream()
        .map(Resource::getContent)
        .collect(Collectors.toList());

    System.out.println(resources);

  }

  @Override
  public void getCities(String urlToRead) throws Exception {
    System.out.println("STARTING CREATION CITIES");
    BufferedReader rd = getReader(urlToRead);
    String line;
    while ((line = rd.readLine()) != null) {
      if (line.startsWith("\"")) {
        //Kann noch geiler gemacht werden
        String[] parts = line.split(",");
        String id = parts[0].replaceAll("\"", "").trim();
        parts[0] = "";
        String name = String.join("", parts).replaceAll("\"", "").trim();
        City next = new City(name, id);
        cities.add(next);
        if (next.getName().contains("Germany")) {
          sendCityToBackend(next.getId(), next.getName());
        }
      }
    }
    rd.close();

    System.out.println("CREATION DONE");
    List<City> resources = cityClient.findAll()
        .getContent().stream()
        .map(Resource::getContent)
        .collect(Collectors.toList());

    System.out.println(resources);
  }

  @Override
  public void sendCityToBackend(String id, String name) {
    cityClient.create(new City(name,id));
  }

  @Override
  public void sendAirlineToBackend(String id, String name) {
    airlineClient.create(new Airline(name, id));
  }

  private BufferedReader getReader(String urlToRead) throws IOException {
    URL url = new URL(urlToRead);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    return new BufferedReader(new InputStreamReader(conn.getInputStream()));

  }
}
