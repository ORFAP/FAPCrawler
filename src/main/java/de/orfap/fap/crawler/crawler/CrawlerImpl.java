package de.orfap.fap.crawler.crawler;

import de.orfap.fap.crawler.domain.Airline;
import de.orfap.fap.crawler.domain.City;
import de.orfap.fap.crawler.domain.Route;
import de.orfap.fap.crawler.feign.AirlineClient;
import de.orfap.fap.crawler.feign.CityClient;
import de.orfap.fap.crawler.feign.RouteClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
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

    @Autowired
    private RouteClient routeClient;

    private ArrayList<Resource<City>> cities = new ArrayList();

    private ArrayList<Resource<Airline>> airlines = new ArrayList();

    @Override
    public void getAirlines(String urlToRead) throws Exception {
        System.out.println("STARTING CREATION AIRLINES");
        BufferedReader rd = getReader(urlToRead);
        String line;
        Airline next;
        while ((line = rd.readLine()) != null) {
            if (line.startsWith("\"")) {
                String[] parts = line.split(",");
                next = new Airline(parts[1].replaceAll("(\"|\\([1-9]\\))", "").trim(), parts[0].replaceAll("\"", ""));
//          airlines.add(next);
                airlines.add(sendAirlineToBackend(next));
            }
        }
        rd.close();

        System.out.println("CREATION DONE");
        List<Airline> resources = airlineClient.findAll()
                .getContent().stream()
                .map(Resource::getContent)
                .collect(Collectors.toList());

//    System.out.println(resources);

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
                //Checks if City lies in the USA
                if (parts.length==3&&parts[2].replaceAll("\"", "").trim().matches("[A-Z][A-Z]")) {
                    cities.add(sendCityToBackend(next));
                }
            }
        }
        rd.close();

        System.out.println("CREATION DONE");
    }

    @Override
    public Resource<City> sendCityToBackend(City city) {
        return cityClient.create(city);
    }

    @Override
    public void getRoutes(String urlToRead) throws Exception {
        Route route = new Route();
        route.setDate(new Date());
        route.setCancelled(0);
        route.setDelays(0);
        route.setPassengerCount(0);
        route.setFlightCount(0);
        route.setAirline(airlines.get(0).getId().getHref());
        route.setSource(cities.get(0).getId().getHref());
        route.setDestination(cities.get(1).getId().getHref());
        Resource<Route> result = sendRoutesToBackend(route);
        System.out.println(result);
    }

    @Override
    public Resource<Route> sendRoutesToBackend(Route route) {
        return routeClient.create(route);
    }

    @Override
    public Resource<Airline> sendAirlineToBackend(Airline airline) {
        return airlineClient.create(airline);
    }

    private BufferedReader getReader(String urlToRead) throws IOException {
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        return new BufferedReader(new InputStreamReader(conn.getInputStream()));

    }
}
