package de.orfap.fap.crawler.feign;

import de.orfap.fap.crawler.domain.Airport;
import feign.Headers;
import feign.RequestLine;

/**
 * Created by Arne on 14.04.2016.
 */
public interface AirportClient {

    @Headers("Content-Type: application/json")
    @RequestLine("POST /airports/")
    Airport create(Airport airport);

//    @RequestLine("GET /")


}
