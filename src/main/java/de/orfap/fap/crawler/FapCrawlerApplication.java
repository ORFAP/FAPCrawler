package de.orfap.fap.crawler;

import de.orfap.fap.crawler.crawler.Crawler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FapCrawlerApplication implements CommandLineRunner {

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Autowired
  Crawler crawler;


  public static void main(String[] args) {
    SpringApplication.run(FapCrawlerApplication.class, args);
  }


  @Override
  public void run(String... args) throws Exception {
    crawler.getAirlines("http://www.transtats.bts.gov/Download_Lookup.asp?Lookup=L_UNIQUE_CARRIERS");
    crawler.getCities("http://www.transtats.bts.gov/Download_Lookup.asp?Lookup=L_AIRPORT_ID");
    crawler.getRoutes("idc");
  }
}
