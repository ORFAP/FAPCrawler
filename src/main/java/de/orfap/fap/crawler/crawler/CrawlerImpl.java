package de.orfap.fap.crawler.crawler;

import de.orfap.fap.crawler.domain.Airline;
import de.orfap.fap.crawler.domain.Market;
import de.orfap.fap.crawler.domain.Route;
import de.orfap.fap.crawler.feign.AirlineClient;
import de.orfap.fap.crawler.feign.MarketClient;
import de.orfap.fap.crawler.feign.RouteClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Arne on 13.04.2016.
 */
@Service
public class CrawlerImpl implements Crawler {
    @Autowired
    private AirlineClient airlineClient;

    @Autowired
    private MarketClient marketClient;

    @Autowired
    private RouteClient routeClient;

    @Value("${fap.backend.basePath}")
    private String basepath;

    private ArrayList<Resource<Market>> markets = new ArrayList();

    private ArrayList<Resource<Airline>> airlines = new ArrayList();

    @Override
    public void getAirlines(String urlToRead) throws Exception {
        System.out.println("STARTING CREATION AIRLINES");
        Resources<Resource<Airline>> exsisting = airlineClient.findAll();
        BufferedReader rd = (BufferedReader) getReader(urlToRead, "GET");
        String line;
        Airline next;
        while ((line = rd.readLine()) != null) {
            if (line.startsWith("\"")) {
                String[] parts = line.split(",");
                next = new Airline(parts[1].replaceAll("(\"|\\([1-9]\\))", "").trim(), parts[0].replaceAll("\"", ""));
                if(!exsisting.getContent().contains(next)) {
                    airlines.add(next);
                }
            }
        }
        rd.close();
        System.out.println("CREATION DONE");
    }

    @Override
    public void getMarkets(String urlToRead) throws Exception {
        System.out.println("STARTING CREATION MARKETS");
        Resources<Resource<Market>> exsisting = marketClient.findAll();
        BufferedReader rd = (BufferedReader) getReader(urlToRead, "GET");
        String line;
        while ((line = rd.readLine()) != null) {
            if (line.startsWith("\"")) {
                String[] parts = line.split(",");
                String id = parts[0].replaceAll("\"", "").trim();
                parts[0] = "";
                String name = String.join("", parts).replaceAll("\"", "").trim();
                Market next = new Market(name, id);
                //Checks if Market lies in the USA
                if(!exsisting.getContent().contains(next)) {
                    if (parts.length == 3 && (parts[2].replaceAll("\"", "").trim().matches("[A-Z][A-Z]")
                            || parts[2].replaceAll("\"", "").trim().contains("[A-Z][A-Z] [\\(]")
                            || parts[2].contains("Metropolitan Area"))) {
                        markets.add(next);
                    }
                }
            }
        }
        rd.close();
        System.out.println("CRAWSING MARKETS DONE");
    }

    @Override
    public void getRoutes(String urlToRead) throws Exception {
        System.out.println("STARTED CRAWSING ROUTES, as in crawl & parse");
        Resources<Resource<Route>> exsisting = routeClient.findAll();
//        InputStream rd = (InputStream) getReader(urlToRead, "POST");
//        Files.copy(rd, Paths.get("temp.zip"));
        ZipFile zipFile = new ZipFile("temp.zip");

        Enumeration entries = zipFile.entries();
        ZipEntry zE = (ZipEntry) entries.nextElement();
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zE)));
        String line = br.readLine();
        try {
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
                if (!line.contains(",,") && columns[4].equals("31703")) {
                    // DEPARTURES_SCHEDULED","DEPARTURES_PERFORMED",
                    // "PASSENGERS","AIRLINE_ID","ORIGIN_CITY_MARKET_ID",
                    // "DEST_CITY_MARKET_ID","MONTH
                    Route route = new Route();
                    GregorianCalendar gregorianCalendar = new GregorianCalendar(2015, Integer.parseInt(columns[6]), 1);
                    route.setDate(gregorianCalendar.getTime());
                    route.setCancelled(0);
                    route.setDelays(0);
                    route.setPassengerCount((int) Double.parseDouble(columns[2]));
                    route.setFlightCount((int) Double.parseDouble(columns[1]));
                    route.setAirline(basepath + "airlines/" + columns[3]);
                    route.setSource(basepath + "markets/" + columns[4]);
                    route.setDestination(basepath + "markets/" + columns[5]);
                    if (!exsisting.getContent().contains(route)) {
                        routes.add(route);
                    }
                }
            }
        } finally {
            zipFile.close();
//            File file = new File("temp.zip");
//            file.delete();
        }
        System.out.println("CRAWSING ROUTES DONE");
    }

    public void sendDataToBackend() {
        System.out.println("STARTED SENDING Airlines, Markets & Routes to Backend");
        for (Airline airline : airlines) {
            for (int i = 0; i < routes.size(); i++) {
                if (routes.get(i).getAirline().equals(airline.getId())) {
                    sendAirlineToBackend(airline);
                    i = routes.size();
                }
            }
        }
        for (Market market : markets) {
            for (int i = 0; i < routes.size(); i++) {
                if (routes.get(i).getDestination().equals(market.getId())) {
                    sendMarketToBackend(market);
                    i = routes.size();
                }
            }
        }
        for (Route route : routes) {
            sendRoutesToBackend(route);
        }
        System.out.println("SENDING Airlines, Markets & Routes to Backend DONE");
    }

    /**
     * Gives a reader to an URL
     *
     * @param urlToRead the URL
     * @param method    the Http method
     * @return postHTTPforming Reader
     * @throws IOException
     */
    private Object getReader(String urlToRead, String method) throws IOException {
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        if (method.equals("POST")) {
            setReqPropT100D(conn);
            return conn.getInputStream();
        }
        return new BufferedReader(new InputStreamReader(conn.getInputStream()));
    }

    /**
     * Configures the HttpURLConnection for the T100D table.
     *
     * @param conn the Connection to be configured
     * @throws IOException
     */
    private void setReqPropT100D(HttpURLConnection conn) throws IOException {
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Referer", "http://transtats.bts.gov/DL_SelectFields.asp?Table_ID=311");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", "2194");
        conn.setDoOutput(true);
        String charset = "UTF-8";
        StringBuilder postHTTPform = new StringBuilder();
        try {
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("UserTableName", charset), URLEncoder.encode("T_100_Domestic_Segment__All_Carriers", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("DBShortName", charset), URLEncoder.encode("", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("RawDataTable", charset), URLEncoder.encode("T_T100D_SEGMENT_ALL_CARRIER", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("sqlstr", charset), URLEncoder.encode("SELECT DEPARTURES_SCHEDULED,DEPARTURES_PERFORMED,PASSENGERS,AIRLINE_ID,ORIGIN_CITY_MARKET_ID,DEST_CITY_MARKET_ID,MONTH FROM  T_T100D_SEGMENT_ALL_CARRIER WHERE YEAR=2015", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("varlist", charset), URLEncoder.encode("DEPARTURES_SCHEDULED,DEPARTURES_PERFORMED,PASSENGERS,AIRLINE_ID,ORIGIN_CITY_MARKET_ID,DEST_CITY_MARKET_ID,MONTH", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("grouplist", charset), URLEncoder.encode("", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("suml", charset), URLEncoder.encode("", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("sumRegion", charset), URLEncoder.encode("", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("filter1", charset), URLEncoder.encode("title=", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("filter2", charset), URLEncoder.encode("title=", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("geo", charset), URLEncoder.encode("All\\240", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("time", charset), URLEncoder.encode("All\\240Months", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("timename", charset), URLEncoder.encode("Month", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("GEOGRAPHY", charset), URLEncoder.encode("All", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("XYEAR", charset), URLEncoder.encode("2015", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("FREQUENCY", charset), URLEncoder.encode("All", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarName", charset), URLEncoder.encode("DEPARTURES_SCHEDULED", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DepScheduled", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarName", charset), URLEncoder.encode("DEPARTURES_PERFORMED", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DepPerformed", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Payload", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Seats", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarName", charset), URLEncoder.encode("PASSENGERS", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Passengers", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Freight", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Mail", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Distance", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("RampTime", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("AirTime", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("UniqueCarrier", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarName", charset), URLEncoder.encode("AIRLINE_ID", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("AirlineID", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("UniqueCarrierName", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("UniqCarrierEntity", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("CarrierRegion", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Carrier", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("CarrierName", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("CarrierGroup", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("CarrierGroupNew", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("OriginAirportID", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("OriginAirportSeqID", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarName", charset), URLEncoder.encode("ORIGIN_CITY_MARKET_ID", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("OriginCityMarketID", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Origin", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("OriginCityName", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("OriginState", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("OriginStateFips", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("OriginStateName", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("OriginWac", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DestAirportID", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DestAirportSeqID", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarName", charset), URLEncoder.encode("DEST_CITY_MARKET_ID", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DestCityMarketID", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Dest", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DestCityName", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DestState", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DestStateFips", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DestStateName", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DestWac", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("AircraftGroup", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("AircraftType", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("AircraftConfig", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Year", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Quarter", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarName", charset), URLEncoder.encode("MONTH", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Month", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DistanceGroup", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Class", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(postHTTPform.toString());
        wr.flush();
        wr.close();
    }

    /**
     * Configures the HttpURLConnection for the T100D table.
     *
     * @param conn the Connection to be configured
     * @throws IOException
     */
    private void setReqPropONTIME(HttpURLConnection conn) throws IOException {
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Referer", "http://transtats.bts.gov/DL_SelectFields.asp?Table_ID=236");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", "4339");
        conn.setDoOutput(true);
        String charset = "UTF-8";
        StringBuilder postHTTPform = new StringBuilder();
        try {
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("UserTableName",charset),URLEncoder.encode("On_Time_Performance",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("DBShortName",charset),URLEncoder.encode("",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("RawDataTable",charset),URLEncoder.encode("T_ONTIME",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("sqlstr",charset),URLEncoder.encode(" SELECT DAY_OF_WEEK,FL_DATE,AIRLINE_ID,ORIGIN_CITY_MARKET_ID,DEST_CITY_MARKET_ID,DEP_DELAY_NEW,ARR_DELAY_NEW,CANCELLED FROM  T_ONTIME WHERE Month =1 AND YEAR=2015",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("varlist",charset),URLEncoder.encode("DAY_OF_WEEK,FL_DATE,AIRLINE_ID,ORIGIN_CITY_MARKET_ID,DEST_CITY_MARKET_ID,DEP_DELAY_NEW,ARR_DELAY_NEW,CANCELLED",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("grouplist",charset),URLEncoder.encode("",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("suml",charset),URLEncoder.encode("",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("sumRegion",charset),URLEncoder.encode("",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("filter1",charset),URLEncoder.encode("title=",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("filter2",charset),URLEncoder.encode("title=",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("geo",charset),URLEncoder.encode("All ",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("time",charset),URLEncoder.encode("January",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("timename",charset),URLEncoder.encode("Month",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("GEOGRAPHY",charset),URLEncoder.encode("All",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("XYEAR",charset),URLEncoder.encode("2015",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("FREQUENCY",charset),URLEncoder.encode("1",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Year",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Quarter",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Month",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("DayofMonth",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarName",charset),URLEncoder.encode("DAY_OF_WEEK",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("DayOfWeek",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarName",charset),URLEncoder.encode("FL_DATE",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("FlightDate",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("UniqueCarrier",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarName",charset),URLEncoder.encode("AIRLINE_ID",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("AirlineID",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Carrier",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("TailNum",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("FlightNum",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("OriginAirportID",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("OriginAirportSeqID",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarName",charset),URLEncoder.encode("ORIGIN_CITY_MARKET_ID",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("OriginCityMarketID",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Origin",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("OriginCityName",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("OriginState",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("OriginStateFips",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("OriginStateName",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("OriginWac",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("DestAirportID",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("DestAirportSeqID",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarName",charset),URLEncoder.encode("DEST_CITY_MARKET_ID",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("DestCityMarketID",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Dest",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("DestCityName",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("DestState",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("DestStateFips",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("DestStateName",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("DestWac",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("CRSDepTime",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("DepTime",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("DepDelay",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarName",charset),URLEncoder.encode("DEP_DELAY_NEW",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("DepDelayMinutes",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("DepDel15",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("DepartureDelayGroups",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("DepTimeBlk",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("TaxiOut",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("WheelsOff",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("WheelsOn",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("TaxiIn",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("CRSArrTime",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("ArrTime",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("ArrDelay",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarName",charset),URLEncoder.encode("ARR_DELAY_NEW",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("ArrDelayMinutes",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("ArrDel15",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("ArrivalDelayGroups",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("ArrTimeBlk",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarName",charset),URLEncoder.encode("CANCELLED",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Cancelled",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("CancellationCode",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Diverted",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("CRSElapsedTime",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("ActualElapsedTime",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("AirTime",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Flights",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Distance",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("DistanceGroup",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("CarrierDelay",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("WeatherDelay",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("NASDelay",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("SecurityDelay",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("LateAircraftDelay",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("FirstDepTime",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("TotalAddGTime",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("LongestAddGTime",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("DivAirportLandings",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("DivReachedDest",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("DivActualElapsedTime",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("DivArrDelay",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("DivDistance",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div1Airport",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div1AirportID",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div1AirportSeqID",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div1WheelsOn",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div1TotalGTime",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div1LongestGTime",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div1WheelsOff",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div1TailNum",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div2Airport",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div2AirportID",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div2AirportSeqID",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div2WheelsOn",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div2TotalGTime",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div2LongestGTime",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div2WheelsOff",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div2TailNum",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div3Airport",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div3AirportID",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div3AirportSeqID",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div3WheelsOn",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div3TotalGTime",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div3LongestGTime",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div3WheelsOff",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div3TailNum",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div4Airport",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div4AirportID",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div4AirportSeqID",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div4WheelsOn",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div4TotalGTime",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div4LongestGTime",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div4WheelsOff",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div4TailNum",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div5Airport",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div5AirportID",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div5AirportSeqID",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div5WheelsOn",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div5TotalGTime",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div5LongestGTime",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Num",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div5WheelsOff",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarType",charset),URLEncoder.encode("Char",charset)));
            postHTTPform.append(String.format("%s=%s&",URLEncoder.encode("VarDesc",charset),URLEncoder.encode("Div5TailNum",charset)));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(postHTTPform.toString());
        wr.flush();
        wr.close();
    }

    @Override
    public Resource<Market> sendMarketToBackend(Market market) {
        return marketClient.create(market);
    }

    @Override
    public Resource<Route> sendRoutesToBackend(Route route) {
        return routeClient.create(route);
    }

    @Override
    public Resource<Airline> sendAirlineToBackend(Airline airline) {
        return airlineClient.create(airline);
    }
}
