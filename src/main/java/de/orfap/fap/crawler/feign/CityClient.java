
package de.orfap.fap.crawler.feign;

import de.orfap.fap.crawler.domain.City;
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
@FeignClient(url = "${fap.backend.basePath}", name = "cities")
public interface CityClient {

  @RequestMapping(method = RequestMethod.POST, value = "/cities", consumes = "application/json")
  Resource<City> create(City city);

  @RequestMapping(method = RequestMethod.GET, value = "/cities/{id}", consumes = "application/json")
  Resource<City> findOne(@RequestParam("id") String id);

  @RequestMapping(method = RequestMethod.GET, value = "/cities", consumes = "application/json")
  Resources<Resource<City>> findAll();

}
