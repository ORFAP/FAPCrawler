package de.orfap.fap.crawler.feign;

import de.orfap.fap.crawler.domain.Airline;
import feign.Headers;
import feign.RequestLine;
import org.jboss.logging.Param;
import org.springframework.cloud.netflix.feign.FeignClient;

import java.util.List;

/**
 * Created by Arne on 14.04.2016.
 */
@FeignClient
public interface AirlineClient {


    @Headers("Content-Type: application/json")
    @RequestLine("POST /airlines/")
    Airline create(Airline airline);

//    @RequestLine("GET /")


}
