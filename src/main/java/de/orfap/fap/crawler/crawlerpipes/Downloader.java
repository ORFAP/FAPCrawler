package de.orfap.fap.crawler.crawlerpipes;

import edu.hm.obreitwi.arch.lab08.BaseProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by o4 on 03.06.16.
 */
public class Downloader<T> extends BaseProducer<T> {
    private final Logger LOG = LoggerFactory.getLogger(Downloader.class);
    private T outputFile;

    public Downloader(final String url, final int year, final int month, final String downloadfileType, final String filename) {


        this.outputFile = null;
        try {
            Files.deleteIfExists(Paths.get(filename));
            URL urlToRead = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlToRead.openConnection();
            if (downloadfileType.equals("csv")) {
                conn.setRequestMethod("GET");
                Files.copy(conn.getInputStream(), Paths.get(filename));
            } else if (downloadfileType.equals("zip")) {
                conn.setRequestMethod("POST");
                if (filename.contains("route")) {
                    setReqPropT100D(conn, year, month);
                } else if (filename.contains("flight")) {
                    setReqPropONTIME(conn, year, month);
                } else {
                    LOG.error("Unknown filename");
                    throw new IllegalArgumentException("Unknown filename");
                }
                handleRedirectAndDownloadZIP(conn, filename);
            }
            LOG.info("File " + filename + " downloaded");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRedirectAndDownloadZIP(HttpURLConnection conn, String filename) throws IOException {
        boolean redirect = false;
        // normally, 3xx is redirect
        int status = conn.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            if (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER)
                redirect = true;
        }
        if (redirect) {
            // get redirect url from "location" header field
            String newUrl = conn.getHeaderField("Location");
            // get the cookie if need, for login
            String cookies = conn.getHeaderField("Set-Cookie");

            // open the new connnection again
            conn = (HttpURLConnection) new URL(newUrl).openConnection();
            conn.setRequestProperty("Cookie", cookies);

            System.out.println("Redirect to URL : " + newUrl);

        }

        LOG.info("downloading zip-file...");
        Files.copy(conn.getInputStream(), Paths.get(filename));
    }

    @Override
    public T deliver() {
        return outputFile;
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
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("sqlstr", charset), URLEncoder.encode("SELECT YEAR,MONTH,DEPARTURES_SCHEDULED,DEPARTURES_PERFORMED,PASSENGERS,AIRLINE_ID,ORIGIN_CITY_MARKET_ID,DEST_CITY_MARKET_ID FROM  T_T100D_SEGMENT_ALL_CARRIER WHERE YEAR=" + year + " AND MONTH=" + month + " AND ORIGIN_CITY_MARKET_ID=31703", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("varlist", charset), URLEncoder.encode("YEAR,MONTH,DEPARTURES_SCHEDULED,DEPARTURES_PERFORMED,PASSENGERS,AIRLINE_ID,ORIGIN_CITY_MARKET_ID,DEST_CITY_MARKET_ID", charset)));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(postHTTPform.toString());
        wr.flush();
        wr.close();
    }
}
