package de.orfap.fap.crawler.rest;

import de.orfap.fap.crawler.domain.Airport;
import org.springframework.hateoas.Link;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by Arne on 13.04.2016.
 */
public interface AirportRestClient {

    public static final String AIRPORTS = "airports";

    List<Airport> findAll();

    List<Airport> findAll(Link relation);

    List<Airport> findFullTextFuzzy(String filter);

    Optional<Airport> findOne(Link link);

    void setRelations(Link endpoint, Collection<Link> links);

    void setRelation(Link endpoint, Link relation);

    Airport create(Airport airport);

    Airport update(Airport airport);

    void delete(Link id);
}
