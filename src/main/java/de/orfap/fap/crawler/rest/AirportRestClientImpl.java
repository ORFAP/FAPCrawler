package de.orfap.fap.crawler.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.orfap.fap.crawler.domain.Airport;
import de.orfap.fap.crawler.feign.AirlineClient;
import de.orfap.fap.crawler.feign.AirportClient;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
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
public class AirportRestClientImpl implements AirportRestClient{

    public static final String FIND_FULL_TEXT_FUZZY = "findFullTextFuzzy";

    public static final String SEARCH = "search";

    AirportClient backend;


    @Autowired
    public AirportRestClientImpl(@Value("${fap.backend.basePath}")
                                         String basePath) {

        backend = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .target(AirportClient.class, basePath);

        /*
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new Jackson2HalModule());

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(MediaType.parseMediaTypes("application/hal+json"));
        converter.setObjectMapper(mapper);*/
    }

    @Override
    public List<Airport> findAll() {
        return null;
    }

    @Override
    public List<Airport> findAll(Link relation) {
        return null;
    }

    @Override
    public List<Airport> findFullTextFuzzy(String filter) {
        return null;
    }

    @Override
    public Optional<Airport> findOne(Link link) {
        return null;
    }

    @Override
    public void setRelations(Link endpoint, Collection<Link> links) {

    }

    @Override
    public void setRelation(Link endpoint, Link relation) {

    }

    @Override
    public Airport create(Airport airport) {
       return backend.create(airport);
    }

    @Override
    public Airport update(Airport airport) {
        return null;
    }

    @Override
    public void delete(Link id) {

    }
}
