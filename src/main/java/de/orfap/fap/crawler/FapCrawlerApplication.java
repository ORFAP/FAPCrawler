package de.orfap.fap.crawler;

import de.orfap.fap.crawler.domain.Airline;
import de.orfap.fap.crawler.feign.AirlineClient;
import feign.Headers;
import feign.RequestLine;
//import javax.inject.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@SpringBootApplication
@EnableFeignClients
public class FapCrawlerApplication implements CommandLineRunner{
    @Autowired
    Crawler crawler;


    public static void main(String[] args) throws Exception
	{
        SpringApplication.run(FapCrawlerApplication.class, args);
	}

    @Override
    public void run(String... args) throws Exception {
//        client.create(new Airline("Hallo","Vögelein"));
        crawler.getAirlines("http://www.transtats.bts.gov/Download_Lookup.asp?Lookup=L_UNIQUE_CARRIERS");
        crawler.getCities("http://www.transtats.bts.gov/Download_Lookup.asp?Lookup=L_AIRPORT_ID");
    }

    @FeignClient("store")
    interface MyClient{
        @Headers("Content-Type: application/json")
        @RequestLine("POST /airlines/")
        Airline create(Airline airline);

    }
}
