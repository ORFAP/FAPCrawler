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
//    crawler.getAirlines("http://transtats.bts.gov/Download_Lookup.asp?Lookup=L_AIRLINE_ID");
//    crawler.getMarkets("http://www.transtats.bts.gov/Download_Lookup.asp?Lookup=L_CITY_MARKET_ID");
    crawler.getRoutes("http://transtats.bts.gov/DownLoad_Table.asp?Table_ID=311&Has_Group=3&Is_Zipped=0");
  }
}
