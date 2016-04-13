package de.orfap.fap.crawler.rest;

import de.orfap.fap.crawler.local.Airline;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
/**
 * Created by Arne on 13.04.2016.
 */
public class AirlineRestClientImpl implements AirlineRestClient{

    @Override
    public List<Airline> findAll() {
        return null;
    }

    @Override
    public List<Airline> findAll(Link relation) {
        return null;
    }

    @Override
    public List<Airline> findFullTextFuzzy(String filter) {
        return null;
    }

    @Override
    public Optional<Airline> findOne(Link link) {
        return null;
    }

    @Override
    public void setRelations(Link endpoint, Collection<Link> links) {

    }

    @Override
    public void setRelation(Link endpoint, Link relation) {

    }

    @Override
    public Airline create(Airline airline) {
        URI uri = URI.create(
                traverson.follow(AIRLINES).asLink().getHref());
        AirlineDTO airlineDTO = airlineAssembler.toResource(airline).getContent();
        AirlineResource resource = restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(airlineDTO), ViewComponent_Resource.class).getBody();
        return airlineAssembler.toBean(resource);



    }

    @Override
    public Airline update(Airline airline) {
        return null;
    }

    @Override
    public void delete(Link id) {

    }
}
