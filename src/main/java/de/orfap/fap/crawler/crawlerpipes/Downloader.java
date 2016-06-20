package de.orfap.fap.crawler.crawlerpipes;

import edu.hm.obreitwi.arch.lab08.BaseProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.zip.ZipFile;

/**
 * Created by o4 on 03.06.16.
 */
public class Downloader<T> extends BaseProducer<T> {
    private final Logger LOG = LoggerFactory.getLogger(Downloader.class);
    private File file;
    private ZipFile zipFile;
    private String downloadfileType;
    private String url;
    private int year;
    private int month;
    private String filename;
    private Object outputFile;

    public Downloader(final String url, final int year, final int month, final String downloadfileType, final String filename) {
        this.url = url;
        this.year = year;
        this.month = month;
        this.downloadfileType = downloadfileType;
        this.filename = filename;
        this.outputFile = null;
        try {
            Files.deleteIfExists(Paths.get(filename));
            URL urlToRead = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlToRead.openConnection();
            if (downloadfileType.equals("csv")) {
                conn.setRequestMethod("GET");
                this.filename = conn.getHeaderField("Content-Disposition").split("filename=")[1].replace("\"", "");
                Files.copy(conn.getInputStream(), Paths.get(filename));
                outputFile = new File(filename);
            } else if (downloadfileType.equals("zip")) {
                conn.setRequestMethod("POST");
                setReqPropONTIME(conn, year, month);
                Files.copy(conn.getInputStream(), Paths.get(filename));
            }
            LOG.info("File " + filename + " downloaded");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public T deliver() {
        return (T) outputFile;
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
}
