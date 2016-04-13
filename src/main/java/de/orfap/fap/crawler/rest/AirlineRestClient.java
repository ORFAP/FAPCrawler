package de.orfap.fap.crawler.rest;

import de.orfap.fap.crawler.local.Airline;
import org.springframework.hateoas.Link;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by Arne on 13.04.2016.
 */
public interface AirlineRestClient {

    public static final String AIRLINES = "airlines";

    List<Airline> findAll();

    List<Airline> findAll(Link relation);

    List<Airline> findFullTextFuzzy(String filter);

    Optional<Airline> findOne(Link link);

    void setRelations(Link endpoint, Collection<Link> links);

    void setRelation(Link endpoint, Link relation);

    Airline create(Airline airline);

    Airline update (Airline airline);

    void delete(Link id);
}
