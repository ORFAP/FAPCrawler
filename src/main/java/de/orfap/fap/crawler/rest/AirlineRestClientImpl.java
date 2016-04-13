package de.orfap.fap.crawler.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.orfap.fap.crawler.domain.Airline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.client.Traverson;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
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

    private final Traverson traverson;

    private final RestTemplate restTemplate;


    public AirlineRestClientImpl() {


        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new Jackson2HalModule());

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(MediaType.parseMediaTypes("application/hal+json"));
        converter.setObjectMapper(mapper);
        this.restTemplate = new RestTemplate(Arrays.asList(converter));
        traverson = new Traverson(URI.create("http://localhost:8080/"), MediaTypes.HAL_JSON);
        traverson.setRestOperations(restTemplate);
    }

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
//        Airline myAirline = new Airline("myAirlne");


        restTemplate.exchange(URI.create("http://localhost:8080/"+AIRLINES), HttpMethod.POST,new HttpEntity<Airline>(airline),Object.class);
//        restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<Airline>(airline),Resource<Airline>.class);
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
