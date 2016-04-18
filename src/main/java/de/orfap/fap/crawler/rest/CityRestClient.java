package de.orfap.fap.crawler.rest;

import de.orfap.fap.crawler.domain.City;
import org.springframework.hateoas.Link;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by Arne on 13.04.2016.
 */
public interface CityRestClient {

    public static final String CITIES = "cities";

    List<City> findAll();

    List<City> findAll(Link relation);

    List<City> findFullTextFuzzy(String filter);

    Optional<City> findOne(Link link);

    void setRelations(Link endpoint, Collection<Link> links);

    void setRelation(Link endpoint, Link relation);

    City create(City city);

    City update(City city);

    void delete(Link id);
}
