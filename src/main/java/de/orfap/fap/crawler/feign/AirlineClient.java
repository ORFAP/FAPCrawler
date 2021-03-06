package de.orfap.fap.crawler.feign;

import de.orfap.fap.crawler.domain.Airline;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Organization: HM FK07.
 * Project: fapcrawler, de.orfap.fap.crawler.feign
 * Author(s): Rene Zarwel
 * Date: 21.04.16
 * OS: MacOS 10.11
 * Java-Version: 1.8
 * System: 2,3 GHz Intel Core i7, 16 GB 1600 MHz DDR3
 */
@FeignClient(url = "${fap.backend.basePath}", name = "airlines")
public interface AirlineClient {

    @RequestMapping(method = RequestMethod.POST, value = "/airlines", consumes = "application/json")
    Resource<Airline> create(Airline airline);

    @RequestMapping(method = RequestMethod.GET, value = "/airlines/{id}", consumes = "application/json")
    Resource<Airline> findOne(@RequestParam("id") String id);

    @RequestMapping(method = RequestMethod.GET, value = "/airlines", consumes = "application/json")
    Resources<Resource<Airline>> findAll();

//  @RequestMapping(method = RequestMethod.GET, value = "/airlines", consumes = "application/json")
//  Resource<Airline> findById(@RequestParam String id);
}
