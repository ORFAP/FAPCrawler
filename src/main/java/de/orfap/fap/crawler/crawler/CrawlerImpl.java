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
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
        BufferedReader rd = getReader(urlToRead, "POST");
        String line;
        while ((line = rd.readLine()) != null) {
//            System.out.println(line);
        }
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
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        if (method.equals("POST")){
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Referer", "http://transtats.bts.gov/DL_SelectFields.asp?Table_ID=311\\r\\n");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded\\r\\n");
            conn.setRequestProperty("Content-Length","2194\\r\\n");
            conn.setDoOutput(true);
            setReqProp(conn);
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes("/DownLoad_Table.asp?Table_ID=311&Has_Group=3&Is_Zipped=0 HTTP/1.1\\r\\n");
            wr.flush();
            wr.close();
            int responseCode = conn.getResponseCode();
            System.out.println("Responsecode: " + responseCode);
        }
        return new BufferedReader(new InputStreamReader(conn.getInputStream()));
    }

    private void setReqProp(HttpURLConnection conn){
        conn.setRequestProperty("UserTableName","T_100_Domestic_Segment__All_Carriers");
        conn.setRequestProperty("DBShortName","");
        conn.setRequestProperty("RawDataTable","T_T100D_SEGMENT_ALL_CARRIER");
        conn.setRequestProperty("sqlstr"," SELECT DEPARTURES_SCHEDULED,DEPARTURES_PERFORMED,PASSENGERS,AIRLINE_ID,ORIGIN_CITY_MARKET_ID,DEST_CITY_MARKET_ID,MONTH FROM T_T100D_SEGMENT_ALL_CARRIER WHERE YEAR=2015");
        conn.setRequestProperty("varlist","DEPARTURES_SCHEDULED,DEPARTURES_PERFORMED,PASSENGERS,AIRLINE_ID,ORIGIN_CITY_MARKET_ID,DEST_CITY_MARKET_ID,MONTH");
        conn.setRequestProperty("grouplist","");
        conn.setRequestProperty("suml","");
        conn.setRequestProperty("sumRegion","");
        conn.setRequestProperty("filter1","title=");
        conn.setRequestProperty("filter2","title=");
        conn.setRequestProperty("geo","All ");
        conn.setRequestProperty("time","All Months");
        conn.setRequestProperty("timename","Month");
        conn.setRequestProperty("GEOGRAPHY","All");
        conn.setRequestProperty("XYEAR","2015");
        conn.setRequestProperty("FREQUENCY","All");
        conn.setRequestProperty("VarName","DEPARTURES_SCHEDULED");
        conn.setRequestProperty("VarDesc","DepScheduled");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarName","DEPARTURES_PERFORMED");
        conn.setRequestProperty("VarDesc","DepPerformed");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarDesc","Payload");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarDesc","Seats");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarName","PASSENGERS");
        conn.setRequestProperty("VarDesc","Passengers");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarDesc","Freight");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarDesc","Mail");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarDesc","Distance");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarDesc","RampTime");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarDesc","AirTime");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarDesc","UniqueCarrier");
        conn.setRequestProperty("VarType","Char");
        conn.setRequestProperty("VarName","AIRLINE_ID");
        conn.setRequestProperty("VarDesc","AirlineID");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarDesc","UniqueCarrierName");
        conn.setRequestProperty("VarType","Char");
        conn.setRequestProperty("VarDesc","UniqCarrierEntity");
        conn.setRequestProperty("VarType","Char");
        conn.setRequestProperty("VarDesc","CarrierRegion");
        conn.setRequestProperty("VarType","Char");
        conn.setRequestProperty("VarDesc","Carrier");
        conn.setRequestProperty("VarType","Char");
        conn.setRequestProperty("VarDesc","CarrierName");
        conn.setRequestProperty("VarType","Char");
        conn.setRequestProperty("VarDesc","CarrierGroup");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarDesc","CarrierGroupNew");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarDesc","OriginAirportID");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarDesc","OriginAirportSeqID");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarName","ORIGIN_CITY_MARKET_ID");
        conn.setRequestProperty("VarDesc","OriginCityMarketID");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarDesc","Origin");
        conn.setRequestProperty("VarType","Char");
        conn.setRequestProperty("VarDesc","OriginCityName");
        conn.setRequestProperty("VarType","Char");
        conn.setRequestProperty("VarDesc","OriginState");
        conn.setRequestProperty("VarType","Char");
        conn.setRequestProperty("VarDesc","OriginStateFips");
        conn.setRequestProperty("VarType","Char");
        conn.setRequestProperty("VarDesc","OriginStateName");
        conn.setRequestProperty("VarType","Char");
        conn.setRequestProperty("VarDesc","OriginWac");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarDesc","DestAirportID");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarDesc","DestAirportSeqID");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarName","DEST_CITY_MARKET_ID");
        conn.setRequestProperty("VarDesc","DestCityMarketID");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarDesc","Dest");
        conn.setRequestProperty("VarType","Char");
        conn.setRequestProperty("VarDesc","DestCityName");
        conn.setRequestProperty("VarType","Char");
        conn.setRequestProperty("VarDesc","DestState");
        conn.setRequestProperty("VarType","Char");
        conn.setRequestProperty("VarDesc","DestStateFips");
        conn.setRequestProperty("VarType","Char");
        conn.setRequestProperty("VarDesc","DestStateName");
        conn.setRequestProperty("VarType","Char");
        conn.setRequestProperty("VarDesc","DestWac");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarDesc","AircraftGroup");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarDesc","AircraftType");
        conn.setRequestProperty("VarType","Char");
        conn.setRequestProperty("VarDesc","AircraftConfig");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarDesc","Year");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarDesc","Quarter");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarName","MONTH");
        conn.setRequestProperty("VarDesc","Month");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarDesc","DistanceGroup");
        conn.setRequestProperty("VarType","Num");
        conn.setRequestProperty("VarDesc","Class");
        conn.setRequestProperty("VarType","Char");
    }
}
