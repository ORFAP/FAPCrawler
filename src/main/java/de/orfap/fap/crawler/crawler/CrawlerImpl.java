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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        BufferedReader rd = getReader(urlToRead, "GET");
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
        BufferedReader rd = getReader(urlToRead, "GET");
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
                if (parts.length == 3 && parts[2].replaceAll("\"", "").trim().matches("[A-Z][A-Z]")) {
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
        BufferedReader rd = getReader(urlToRead, "POST");
        String line;
/*        while ((line = rd.readLine()) != null) {
            System.out.println(line);
        }*/
        rd.close();

        /*Route route = new Route();
        route.setDate(new Date());
        route.setCancelled(0);
        route.setDelays(0);
        route.setPassengerCount(0);
        route.setFlightCount(0);
        route.setAirline(airlines.get(0).getId().getHref());
        route.setSource(cities.get(0).getId().getHref());
        route.setDestination(cities.get(1).getId().getHref());
        Resource<Route> result = sendRoutesToBackend(route);
        System.out.println(result);*/
    }

    @Override
    public Resource<Route> sendRoutesToBackend(Route route) {
        return routeClient.create(route);
    }

    @Override
    public Resource<Airline> sendAirlineToBackend(Airline airline) {
        return airlineClient.create(airline);
    }

    private BufferedReader getReader(String urlToRead, String method) throws IOException {
        String postHTTPform = setReqProp();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        if (method.equals("POST")) {
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Referer", "http://transtats.bts.gov/DL_SelectFields.asp?Table_ID=311");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", "2194");
            conn.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            //wr.writeBytes("/DownLoad_Table.asp?Table_ID=311&Has_Group=3&Is_Zipped=0 HTTP/1.1");
            wr.writeBytes(postHTTPform);
            wr.flush();
            wr.close();
            int responseCode = conn.getResponseCode();
            System.out.println("Responsecode: " + responseCode);
            for (Map.Entry<String, List<String>> header : conn.getHeaderFields().entrySet()) {
                System.out.println(header.getKey() + "=" + header.getValue());
            }
        }
        return new BufferedReader(new InputStreamReader(conn.getInputStream()));
    }

    private String setReqProp() {
        String charset = "UTF-8";
        StringBuilder result = new StringBuilder();
        try {
            result.append(String.format("%s=%s&", URLEncoder.encode("UserTableName", charset), URLEncoder.encode("T_100_Domestic_Segment__All_Carriers", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("DBShortName", charset), URLEncoder.encode("", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("RawDataTable", charset), URLEncoder.encode("T_T100D_SEGMENT_ALL_CARRIER", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("sqlstr", charset), URLEncoder.encode("SELECT DEPARTURES_SCHEDULED,DEPARTURES_PERFORMED,PASSENGERS,AIRLINE_ID,ORIGIN_CITY_MARKET_ID,DEST_CITY_MARKET_ID,MONTH FROM  T_T100D_SEGMENT_ALL_CARRIER WHERE YEAR=2015", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("varlist", charset), URLEncoder.encode("DEPARTURES_SCHEDULED,DEPARTURES_PERFORMED,PASSENGERS,AIRLINE_ID,ORIGIN_CITY_MARKET_ID,DEST_CITY_MARKET_ID,MONTH", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("grouplist", charset), URLEncoder.encode("", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("suml", charset), URLEncoder.encode("", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("sumRegion", charset), URLEncoder.encode("", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("filter1", charset), URLEncoder.encode("title=", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("filter2", charset), URLEncoder.encode("title=", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("geo", charset), URLEncoder.encode("All\\240", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("time", charset), URLEncoder.encode("All\\240Months", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("timename", charset), URLEncoder.encode("Month", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("GEOGRAPHY", charset), URLEncoder.encode("All", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("XYEAR", charset), URLEncoder.encode("2015", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("FREQUENCY", charset), URLEncoder.encode("All", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarName", charset), URLEncoder.encode("DEPARTURES_SCHEDULED", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DepScheduled", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarName", charset), URLEncoder.encode("DEPARTURES_PERFORMED", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DepPerformed", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Payload", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Seats", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarName", charset), URLEncoder.encode("PASSENGERS", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Passengers", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Freight", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Mail", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Distance", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("RampTime", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("AirTime", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("UniqueCarrier", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarName", charset), URLEncoder.encode("AIRLINE_ID", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("AirlineID", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("UniqueCarrierName", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("UniqCarrierEntity", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("CarrierRegion", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Carrier", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("CarrierName", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("CarrierGroup", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("CarrierGroupNew", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("OriginAirportID", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("OriginAirportSeqID", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarName", charset), URLEncoder.encode("ORIGIN_CITY_MARKET_ID", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("OriginCityMarketID", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Origin", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("OriginCityName", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("OriginState", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("OriginStateFips", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("OriginStateName", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("OriginWac", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DestAirportID", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DestAirportSeqID", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarName", charset), URLEncoder.encode("DEST_CITY_MARKET_ID", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DestCityMarketID", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Dest", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DestCityName", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DestState", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DestStateFips", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DestStateName", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DestWac", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("AircraftGroup", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("AircraftType", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("AircraftConfig", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Year", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Quarter", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarName", charset), URLEncoder.encode("MONTH", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Month", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DistanceGroup", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Class", charset)));
            result.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
