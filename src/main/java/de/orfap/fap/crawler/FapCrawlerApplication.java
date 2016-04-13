package de.orfap.fap.crawler;

//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//@SpringBootApplication
public class FapCrawlerApplication {

    public static String getHTML(String urlToRead) throws Exception {
		//SpringApplication.run(FapCrawlerApplication.class, args);
		StringBuilder result = new StringBuilder();
		URL url = new URL(urlToRead);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = rd.readLine()) != null) {
            if(line.startsWith("\"")) {
                sendToBackend("Airlines",line.split(",")[1].replaceAll("(\"|\\([1-9]\\))","").trim());
                result.append(line + "\n");
            }
		}
		rd.close();
		return result.toString();
	}

    private static void sendToBackend(String tablename, String s) {
        System.out.println(s);
    }

    public static void main(String[] args) throws Exception
	{
		//System.out.println(
                getHTML("http://www.transtats.bts.gov/Download_Lookup.asp?Lookup=L_UNIQUE_CARRIERS");
        //);
	}

}
