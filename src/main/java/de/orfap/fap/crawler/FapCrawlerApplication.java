package de.orfap.fap.crawler;

//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@EnableFeignClient
public class FapCrawlerApplication implements CommandLineRunner{
    @Autowired
    Crawler crawler;

    public static void main(String[] args) throws Exception
	{
        SpringApplication.run(FapCrawlerApplication.class, args);
	}

    @Override
    public void run(String... args) throws Exception {
        crawler.getAirlines("http://www.transtats.bts.gov/Download_Lookup.asp?Lookup=L_UNIQUE_CARRIERS");
        crawler.getAirports("http://www.transtats.bts.gov/Download_Lookup.asp?Lookup=L_AIRPORT_ID");
    }
}
