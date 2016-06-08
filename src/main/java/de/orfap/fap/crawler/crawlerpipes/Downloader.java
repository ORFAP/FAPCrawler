package de.orfap.fap.crawler.crawlerpipes;

import edu.hm.obreitwi.arch.lab08.BaseProducer;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
    private Object object;
    private String url;
    private int year;
    private int month;

    public Downloader(String url, int year, int month, Object object) {
        this.url = url;
        this.year = year;
        this.month=month;
        this.object = object;
    }

    @Override
    public T deliver() {
        T output = null;
        String filename;
        try {
            URL urlToRead = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlToRead.openConnection();
            if (object instanceof File) {
                conn.setRequestMethod("GET");
                filename = conn.getHeaderField("Content-Disposition").split("filename=")[1].replace("\"", "");
                Files.copy(conn.getInputStream(), Paths.get(filename));
                //noinspection unchecked
                output = (T) new File(filename);
            }else if (object instanceof ZipFile){
                conn.setRequestMethod("POST");
                setReqPropONTIME(conn, year, month);
                Files.copy(conn.getInputStream(), Paths.get(((ZipFile)object).getName()));
                output = (T) new ZipFile(((ZipFile)object).getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
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
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("sqlstr", charset), URLEncoder.encode(" SELECT DAY_OF_WEEK,FL_DATE,AIRLINE_ID,ORIGIN_CITY_MARKET_ID,DEST_CITY_MARKET_ID,ARR_DELAY_NEW,CANCELLED FROM  T_ONTIME WHERE Month =" + month + " AND YEAR=" + year + " AND ORIGIN_CITY_MARKET_ID=31703", charset)));
            postHTTPform.append(String.format("%s=%s&", URLEncoder.encode("varlist", charset), URLEncoder.encode("DAY_OF_WEEK,FL_DATE,AIRLINE_ID,ORIGIN_CITY_MARKET_ID,DEST_CITY_MARKET_ID,ARR_DELAY_NEW,CANCELLED", charset)));
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
}
