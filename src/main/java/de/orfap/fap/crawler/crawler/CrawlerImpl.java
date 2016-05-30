package de.orfap.fap.crawler.crawler;

import de.orfap.fap.crawler.domain.Airline;
import de.orfap.fap.crawler.domain.Market;
import de.orfap.fap.crawler.domain.Route;
import de.orfap.fap.crawler.feign.AirlineClient;
import de.orfap.fap.crawler.feign.MarketClient;
import de.orfap.fap.crawler.feign.RouteClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Resource;
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
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Arne on 13.04.2016.
 */
@SuppressWarnings({"ALL", "DefaultFileTemplate"})
@Service
public class CrawlerImpl implements Crawler {
    private final Logger LOG = LoggerFactory.getLogger(CrawlerImpl.class);
    private final ArrayList<Market> markets = new ArrayList<>();
    private final ArrayList<Airline> airlines = new ArrayList<>();
    private final ArrayList<Route> routes = new ArrayList<>();
    private final ArrayList<Route> flights = new ArrayList<>();
    //Warnings suppressed because of: No beans needed
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private AirlineClient airlineClient;
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private MarketClient marketClient;
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private RouteClient routeClient;
    @Value("${fap.backend.basePath}")
    private String basepath;

    @Override
    public void getAirlines(String urlToRead) throws Exception {
        LOG.info("STARTED CRAWLING AIRLINES");
        BufferedReader rd = new BufferedReader(new InputStreamReader(openConnection(urlToRead, "csv", 0, 0)));
        String line;
        while ((line = rd.readLine()) != null) {
            if (line.startsWith("\"")) {
                String[] parts = line.split(",");
                final Airline next = new Airline(parts[1].replaceAll("(\"|\\([1-9]\\))", "").trim(), parts[0].replaceAll("\"", ""));
                airlines.add(next);
            }
        }
        rd.close();
        LOG.info("CRAWLING AIRLINES DONE: " + airlines.size() + " Airlines crawled");
    }

    @Override
    public void getMarkets(String urlToRead) throws Exception {
        LOG.info("STARTED CRAWLING MARKETS");
        BufferedReader rd = new BufferedReader(new InputStreamReader(openConnection(urlToRead, "csv", 0, 0)));
        String line;
        while ((line = rd.readLine()) != null) {
            if (line.startsWith("\"")) {
                String[] parts = line.split(",");
                String id = parts[0].replaceAll("\"", "").trim();
                parts[0] = "";
                String name = String.join("", parts).replaceAll("\"", "").trim();
                Market next = new Market(name, id);
                //Checks if Market lies in the USA
                if (parts.length == 3 && (parts[2].replaceAll("\"", "").trim().matches("[A-Z][A-Z]")
                        || parts[2].replaceAll("\"", "").trim().contains("[A-Z][A-Z] [\\(]")
                        || parts[2].contains("Metropolitan Area"))) {
                    markets.add(next);
                }
            }
        }
        rd.close();
        LOG.info("CRAWLING MARKETS DONE: " + markets.size() + " markets crawled.");
    }

    @Override
    public void getFlights(String urlToRead, int year) throws Exception {
        LOG.info("STARTED CRAWLING FLIGHTS");
        String filename = "temp-ontime.zip";
        Route flight;
        String[] columns = new String[0];
        double delay=0;
        for (int month = 1; month <= 12; month++) {
            InputStream rd = openConnection(urlToRead, "ONTIME", year, month);
            Files.copy(rd, Paths.get(filename));
            ZipFile zipFile = new ZipFile(filename);
            Enumeration entries = zipFile.entries();
            ZipEntry zE = (ZipEntry) entries.nextElement();
            BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zE)));
            //noinspection UnusedAssignment Needs to be called to erase first line of file
            @SuppressWarnings("UnusedAssignment") String line = br.readLine();
            try {
                while ((line = br.readLine()) != null) {
                    columns = line.split(",");
                    if (columns[3].equals("31703")) {
                        // "DAY_OF_WEEK","FL_DATE","AIRLINE_ID","ORIGIN_CITY_MARKET_ID"
                        // "DEST_CITY_MARKET_ID","DEP_DELAY_NEW","ARR_DELAY_NEW","CANCELLED"
                        flight = new Route();
                        LOG.debug(line);
                        GregorianCalendar gregorianCalendar = new GregorianCalendar(Integer.parseInt(columns[1].substring(0, 4)), Integer.parseInt(columns[1].substring(5, 7)) - 1, Integer.parseInt(columns[1].substring(8, 10)));
                        flight.setDate(gregorianCalendar.getTime());
                        flight.setCancelled(Double.parseDouble(columns[7]));
                        //Cancelled Flights have empty dep and arr delay fields
                        if (flight.getCancelled() == 0) {
                            //Some dep delay fields are empty
                            if (!columns[5].isEmpty()){
                                delay+=Double.parseDouble(columns[5]);
                            }
                            //some arr delay fields are empty, too
                            if(!columns[6].isEmpty()){
                                delay+=Double.parseDouble(columns[6]);
                            }
                            flight.setDelays(delay);
                            delay=0;
                        }
                        flight.setPassengerCount(0);
                        flight.setFlightCount(1);
                        flight.setAirline(basepath + "airlines/" + columns[2]);
                        flight.setSource(basepath + "markets/" + columns[3]);
                        flight.setDestination(basepath + "markets/" + columns[4]);
                        flights.add(flight);
                    }
                }
            } catch (NumberFormatException nfe) {
                for (String s : columns
                        ) {
                    System.out.println(s);
                }
                nfe.printStackTrace();
                return;
            } finally {
                //noinspection ThrowFromFinallyBlock
                zipFile.close();
                File file = new File(filename);
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }
        LOG.info("CRAWLING FLIGHTS DONE " + flights.size() + " Flights of " + year + " crawled.");
    }

    @Override
    public void getRoutes(String urlToRead, int year) throws Exception {
        LOG.info("STARTED CRAWLING ROUTES");
        String filename = "temp-t100d.zip";
        InputStream rd = openConnection(urlToRead, "T100D", year, 0);
        Route route;
        Files.copy(rd, Paths.get(filename));
        ZipFile zipFile = new ZipFile(filename);
        Enumeration entries = zipFile.entries();
        ZipEntry zE = (ZipEntry) entries.nextElement();
        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zE)));
        //noinspection UnusedAssignment Needs to be called to erase first line of file
        @SuppressWarnings("UnusedAssignment") String line = br.readLine();
        try {
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
                if (!line.contains(",,") && columns[4].equals("31703") && Double.parseDouble(columns[2]) > 0) {
                    // DEPARTURES_SCHEDULED","DEPARTURES_PERFORMED",
                    // "PASSENGERS","AIRLINE_ID","ORIGIN_CITY_MARKET_ID",
                    // "DEST_CITY_MARKET_ID","MONTH
                    route = new Route();
                    GregorianCalendar gregorianCalendar = new GregorianCalendar(year, Integer.parseInt(columns[6]) - 1, 1);
                    route.setDate(gregorianCalendar.getTime());
                    route.setCancelled(0);
                    route.setDelays(0);
                    route.setPassengerCount(Double.parseDouble(columns[2]));
                    route.setFlightCount(Double.parseDouble(columns[1]));
                    route.setAirline(basepath + "airlines/" + columns[3]);
                    route.setSource(basepath + "markets/" + columns[4]);
                    route.setDestination(basepath + "markets/" + columns[5]);
                    routes.add(route);
                }
            }
        } finally {
            zipFile.close();
            File file = new File(filename);
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
        LOG.info("CRAWLING ROUTES DONE: " + routes.size() + " Routes of " + year + " crawled.");
    }

    public void sendDataToBackend() {
        LOG.info("STARTED SENDING Airlines, Markets, Routes & Flights to Backend");
        Set<Airline> usedAirlines = new HashSet<>();
        Collection<Airline> existingAirlines = airlineClient.findAll().getContent().stream().map(Resource::getContent).collect(Collectors.toList());
        for (Airline airline : airlines) {
            for (int i = 0; i < routes.size(); i++) {
                if (routes.get(i).getAirline().equals(basepath + "airlines/" + airline.getId())
                        && !existingAirlines.contains(airline)) {
                    usedAirlines.add(airline);
                    i = routes.size();
                }
            }
        }
        airlines.clear();
        usedAirlines.forEach(this::sendAirlineToBackend);
        LOG.debug("SENT " + usedAirlines.size() + " Airlines to Backend, ignored " + existingAirlines.size() + " already existing");
        Set<Market> usedMarkets = new HashSet<>();
        Collection<Market> existingMarkets = marketClient.findAll().getContent().stream().map(Resource::getContent).collect(Collectors.toList());
        for (Market market : markets) {
            for (int i = 0; i < routes.size(); i++) {
                if (!existingMarkets.contains(market) && (routes.get(i).getDestination().equals(basepath + "markets/" + market.getId())
                        || routes.get(i).getSource().equals(basepath + "markets/" + market.getId()))) {
                    usedMarkets.add(market);
                    i = routes.size();
                }
            }
        }
        usedMarkets.forEach(this::sendMarketToBackend);
        LOG.debug("SENT " + usedMarkets.size() + " Markets to Backend, ignored " + existingMarkets.size() + " already existing");
        markets.clear();
        Collection<Route> existingRoutes = routeClient.findAll().getContent().stream().map(Resource::getContent).collect(Collectors.toList());
        routes.stream().filter(route -> !existingRoutes.contains(route)).forEach(this::sendRoutesToBackend);
        LOG.debug("SENT " + (routes.stream().filter(route -> !existingRoutes.contains(route))).count() + " Routes to Backend, ignored " + existingRoutes.size() + " already existing");
        routes.clear();
        flights.stream().filter(flight -> !existingRoutes.contains(flight)).forEach(this::sendRoutesToBackend);
        LOG.debug("SENT " + (flights.stream().filter(flight -> !existingRoutes.contains(flight))).count() + " Flights to Backend, ignored " + existingRoutes.size() + " already existing");
        flights.clear();
        LOG.info("SENDING Airlines, Markets, Routes & Flights to Backend DONE");
    }

    /**
     * Gives an InputStream to an URL
     *
     * @param urlToRead the URL
     * @param method    the requested table
     * @return postHTTPforming InputStream
     * @throws IOException
     */

    private InputStream openConnection(String urlToRead, String method, int year, int month) throws IOException {
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (method.equals("T100D")) {
            conn.setRequestMethod("POST");
            setReqPropT100D(conn, year);
            return conn.getInputStream();
        } else if (method.equals("ONTIME")) {
            conn.setRequestMethod("POST");
            setReqPropONTIME(conn, year, month);
            return conn.getInputStream();
        }
        conn.setRequestMethod("GET");
        return conn.getInputStream();
    }

    /**
     * Configures the HttpURLConnection for the T100D table.
     *
     * @param conn the Connection to be configured
     * @throws IOException
     */
    private void setReqPropT100D(HttpURLConnection conn, int year) throws IOException {
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
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("sqlstr", charset), URLEncoder.encode("SELECT DEPARTURES_SCHEDULED,DEPARTURES_PERFORMED,PASSENGERS,AIRLINE_ID,ORIGIN_CITY_MARKET_ID,DEST_CITY_MARKET_ID,MONTH FROM  T_T100D_SEGMENT_ALL_CARRIER WHERE YEAR=" + year, charset)));
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
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("XYEAR", charset), URLEncoder.encode("" + year, charset)));
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
     * Configures the HttpURLConnection for the on time table.
     *
     * @param conn  the Connection to be configured
     * @param year  the year to be crawled (no offset, e.g. 2015 translates to 2015)
     * @param month the month to be crawled. Range 1-12.
     * @throws IOException
     */
    private void setReqPropONTIME(HttpURLConnection conn, int year, int month) throws IOException {
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
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("UserTableName", charset), URLEncoder.encode("On_Time_Performance", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("DBShortName", charset), URLEncoder.encode("", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("RawDataTable", charset), URLEncoder.encode("T_ONTIME", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("sqlstr", charset), URLEncoder.encode(" SELECT DAY_OF_WEEK,FL_DATE,AIRLINE_ID,ORIGIN_CITY_MARKET_ID,DEST_CITY_MARKET_ID,DEP_DELAY_NEW,ARR_DELAY_NEW,CANCELLED FROM  T_ONTIME WHERE Month =" + month + " AND YEAR=" + year, charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("varlist", charset), URLEncoder.encode("DAY_OF_WEEK,FL_DATE,AIRLINE_ID,ORIGIN_CITY_MARKET_ID,DEST_CITY_MARKET_ID,DEP_DELAY_NEW,ARR_DELAY_NEW,CANCELLED", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("grouplist", charset), URLEncoder.encode("", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("suml", charset), URLEncoder.encode("", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("sumRegion", charset), URLEncoder.encode("", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("filter1", charset), URLEncoder.encode("title=", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("filter2", charset), URLEncoder.encode("title=", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("geo", charset), URLEncoder.encode("All ", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("time", charset), URLEncoder.encode(Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH), charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("timename", charset), URLEncoder.encode("Month", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("GEOGRAPHY", charset), URLEncoder.encode("All", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("XYEAR", charset), URLEncoder.encode("" + year, charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("FREQUENCY", charset), URLEncoder.encode("" + month, charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Year", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Quarter", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Month", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DayofMonth", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarName", charset), URLEncoder.encode("DAY_OF_WEEK", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DayOfWeek", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarName", charset), URLEncoder.encode("FL_DATE", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("FlightDate", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("UniqueCarrier", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarName", charset), URLEncoder.encode("AIRLINE_ID", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("AirlineID", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Carrier", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("TailNum", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("FlightNum", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
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
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("CRSDepTime", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DepTime", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DepDelay", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarName", charset), URLEncoder.encode("DEP_DELAY_NEW", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DepDelayMinutes", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DepDel15", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DepartureDelayGroups", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DepTimeBlk", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("TaxiOut", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("WheelsOff", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("WheelsOn", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("TaxiIn", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("CRSArrTime", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("ArrTime", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("ArrDelay", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarName", charset), URLEncoder.encode("ARR_DELAY_NEW", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("ArrDelayMinutes", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("ArrDel15", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("ArrivalDelayGroups", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("ArrTimeBlk", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarName", charset), URLEncoder.encode("CANCELLED", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Cancelled", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("CancellationCode", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Diverted", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("CRSElapsedTime", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("ActualElapsedTime", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("AirTime", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Flights", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Distance", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DistanceGroup", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("CarrierDelay", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("WeatherDelay", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("NASDelay", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("SecurityDelay", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("LateAircraftDelay", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("FirstDepTime", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("TotalAddGTime", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("LongestAddGTime", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DivAirportLandings", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DivReachedDest", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DivActualElapsedTime", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DivArrDelay", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("DivDistance", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div1Airport", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div1AirportID", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div1AirportSeqID", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div1WheelsOn", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div1TotalGTime", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div1LongestGTime", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div1WheelsOff", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div1TailNum", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div2Airport", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div2AirportID", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div2AirportSeqID", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div2WheelsOn", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div2TotalGTime", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div2LongestGTime", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div2WheelsOff", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div2TailNum", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div3Airport", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div3AirportID", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div3AirportSeqID", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div3WheelsOn", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div3TotalGTime", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div3LongestGTime", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div3WheelsOff", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div3TailNum", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div4Airport", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div4AirportID", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div4AirportSeqID", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div4WheelsOn", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div4TotalGTime", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div4LongestGTime", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div4WheelsOff", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div4TailNum", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div5Airport", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div5AirportID", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div5AirportSeqID", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div5WheelsOn", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div5TotalGTime", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div5LongestGTime", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Num", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div5WheelsOff", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarType", charset), URLEncoder.encode("Char", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("VarDesc", charset), URLEncoder.encode("Div5TailNum", charset)));
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
