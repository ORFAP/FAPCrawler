package de.orfap.fap.crawler.rest;

import de.orfap.fap.crawler.local.Route;
import org.springframework.hateoas.Link;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by Arne on 13.04.2016.
 */
public interface RouteRestClient {

    public static final String ROUTES = "routes";

    List<Route> findAll();

    List<Route> findAll(Link relation);

    List<Route> findFullTextFuzzy(String filter);

    Optional<Route> findOne(Link link);

    void setRelations(Link endpoint, Collection<Link> links);

    void setRelation(Link endpoint, Link relation);

    Route create(Route route);

    Route update(Route route);

    void delete(Link id);
}
