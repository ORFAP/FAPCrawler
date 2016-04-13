package de.orfap.fap.crawler.rest;

import de.orfap.fap.crawler.domain.Airline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by Arne on 13.04.2016.
 */
@Service
public class AirlineRestClientImpl implements AirlineRestClient{

    public static final String FIND_FULL_TEXT_FUZZY = "findFullTextFuzzy";

    public static final String SEARCH = "search";

    /*private final Traverson traverson;

    private final RestTemplate restTemplate;*/

    /*@Autowired
    public AirlineRestClientImpl(RestTemplate restTemplate, final URI basePath) {
        this.restTemplate = restTemplate;
        traverson = new Traverson(basePath, MediaTypes.HAL_JSON);
        traverson.setRestOperations(restTemplate);
    }*/

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
        /*URI uri = URI.create(
                traverson.follow(AIRLINES).asLink().getHref());*/


//       restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(airline));
        return null;



    }

    @Override
    public Airline update(Airline airline) {
        return null;
    }

    @Override
    public void delete(Link id) {

    }
}
