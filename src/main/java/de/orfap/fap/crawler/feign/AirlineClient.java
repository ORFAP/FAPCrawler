package de.orfap.fap.crawler.feign;

import de.orfap.fap.crawler.domain.Airline;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by Arne on 14.04.2016.
 */
@FeignClient(url = "${fap.backend.basePath}", name = "airlines")
public interface AirlineClient {

    @RequestMapping(method = RequestMethod.POST, value =  "/airlines")
    Airline create(Airline airline);

    @RequestMapping(method = RequestMethod.GET, value = "/airlines/{id}")
    Resource<Airline> findOne(@RequestParam(value = "id") String id);

    @RequestMapping(method = RequestMethod.GET, value = "/airlines")
    Resources<Resource<Airline>> findAll();

}
