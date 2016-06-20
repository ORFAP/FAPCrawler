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
import java.util.*;
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
    private final HashMap<Integer, Market> markets = new HashMap<>();
    private final HashMap<Integer, Market> usedMarkets = new HashMap<>();
    private final HashMap<Integer, Airline> airlines = new HashMap<>();
    private final HashMap<Integer, Airline> usedAirlines = new HashMap<>();
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
                if (next.getId() == null || next.getName() == null) {
                    throw new AssertionError("This is bad");
                }
                airlines.put(Integer.parseInt(next.getId()), next);
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
        Market next;
        while ((line = rd.readLine()) != null) {
            String[] parts = line.split("\",\"");
            if (parts[0].matches("\"[0-9]{1,}") && parts[1].matches(".*, [A-Z]{2}.*\"")) {
                next = new Market(parts[1].trim().replaceAll("\"", ""), parts[0].replaceAll("\"", ""));
                if (next.getId() == null || next.getName() == null) {
                    throw new AssertionError("This is bad");
                }
                markets.put(Integer.parseInt(next.getId()), next);
            }
        }
        rd.close();
        LOG.info("CRAWLING MARKETS DONE: " + markets.size() + " markets crawled.");
    }

    @Override
    public void getRoutes(String urlToRead, int year, int month) throws Exception {
        LOG.info("STARTED CRAWLING ROUTES");
        String filename = "temp-t100d.zip";
        InputStream rd = openConnection(urlToRead, "T100D", year, month);
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
                if (!line.contains(",,") && Double.parseDouble(columns[2]) > 0) {
                    // "DEPARTURES_SCHEDULED","DEPARTURES_PERFORMED",
                    // "PASSENGERS","AIRLINE_ID","ORIGIN_CITY_MARKET_ID",
                    // "DEST_CITY_MARKET_ID","MONTH"
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
                    if (route.getAirline() == null || route.getDate() == null || route.getDestination() == null || route.getSource() == null) {
                        throw new AssertionError("This is bad");
                    }
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
        LOG.info("STARTED SENDING Airlines, Markets & Routes to Backend");
        Set<Airline> usedAirlines = new HashSet<>();
        Collection<Airline> existingAirlines = airlineClient.findAll().getContent().stream().map(Resource::getContent).collect(Collectors.toList());
        Airline airline;
        for (int i = 0; i < routes.size(); i++) {
            if (airlines.containsValue(routes.get(i).getAirline().split("/")[2])) {
                airline = airlines.get(routes.get(i).getAirline().split("/")[2]);
                if (!existingAirlines.contains(airline)) {
                    usedAirlines.add(airline);
                    i = routes.size();
                }
            }
        }
        airlines.clear();
        usedAirlines.forEach(this::sendAirlineToBackend);
        LOG.info("SENT " + usedAirlines.size() + " Airlines to Backend, ignored " + existingAirlines.size() + " already existing");
        Set<Market> usedMarkets = new HashSet<>();
        Collection<Market> existingMarkets = marketClient.findAll().getContent().stream().map(Resource::getContent).collect(Collectors.toList());
        Market market;
        for (int i = 0; i < routes.size(); i++) {
            if (!markets.containsValue(routes.get(i).getDestination().split("/")[2])) {
                market = markets.get(routes.get(i).getDestination().split("/")[2]);
                if (!existingMarkets.contains(market)) {
                    usedMarkets.add(market);
                    i = routes.size();
                }
            }
        }
        markets.clear();
        usedMarkets.forEach(this::sendMarketToBackend);
        LOG.info("SENT " + usedMarkets.size() + " Markets to Backend, ignored " + existingMarkets.size() + " already existing");
        routes.stream().forEach(this::sendRoutesToBackend);
        LOG.info("SENT " + (routes.stream()).count() + " Routes to Backend");
        routes.clear();
        LOG.info("SENDING Airlines, Markets & Routes to Backend DONE");
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
            setReqPropT100D(conn, year, month);
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
     * @param conn  the Connection to be configured
     * @param year  the year to be crawled (no offset, e.g. 2015 translates to 2015)
     * @param month the month to be crawled. Range 1-12.
     * @throws IOException
     */
    private void setReqPropT100D(HttpURLConnection conn, int year, int month) throws IOException {
        conn.setDoOutput(true);
        String charset = "UTF-8";
        StringBuilder postHTTPform = new StringBuilder();
        try {
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("sqlstr", charset), URLEncoder.encode("SELECT DEPARTURES_SCHEDULED,DEPARTURES_PERFORMED,PASSENGERS,AIRLINE_ID,ORIGIN_CITY_MARKET_ID,DEST_CITY_MARKET_ID,MONTH FROM  T_T100D_SEGMENT_ALL_CARRIER WHERE YEAR=" + year + " AND MONTH=" + month + " AND ORIGIN_CITY_MARKET_ID=31703", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("varlist", charset), URLEncoder.encode("DEPARTURES_SCHEDULED,DEPARTURES_PERFORMED,PASSENGERS,AIRLINE_ID,ORIGIN_CITY_MARKET_ID,DEST_CITY_MARKET_ID,MONTH", charset)));
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
        conn.setDoOutput(true);
        String charset = "UTF-8";
        StringBuilder postHTTPform = new StringBuilder();
        try {
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("sqlstr", charset), URLEncoder.encode(" SELECT DAY_OF_WEEK,FL_DATE,AIRLINE_ID,ORIGIN_CITY_MARKET_ID,DEST_CITY_MARKET_ID,ARR_DELAY_NEW,CANCELLED FROM  T_ONTIME WHERE Month =" + month + " AND YEAR=" + year + " AND ORIGIN_CITY_MARKET_ID=31703", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("varlist", charset), URLEncoder.encode("DAY_OF_WEEK,FL_DATE,AIRLINE_ID,ORIGIN_CITY_MARKET_ID,DEST_CITY_MARKET_ID,ARR_DELAY_NEW,CANCELLED", charset)));
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
