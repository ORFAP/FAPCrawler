package de.orfap.fap.crawler.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.orfap.fap.crawler.domain.Airline;
import de.orfap.fap.crawler.feign.AirlineClient;
import feign.Feign;
//import feign.jackson.JacksonDecoder;
//import feign.jackson.JacksonEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.hateoas.hal.Jackson2HalModule;
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
//@Component
public class AirlineRestClientImpl implements AirlineRestClient{

    public static final String FIND_FULL_TEXT_FUZZY = "findFullTextFuzzy";

    public static final String SEARCH = "search";

    private AirlineClient backend;

    @Autowired
    public AirlineRestClientImpl(@Value("${fap.backend.basePath}")
                                         String basePath) {

//        backend = Feign.builder()
//                .encoder(new JacksonEncoder())
//                .decoder(new JacksonDecoder())
//                .target(AirlineClient.class, basePath);

        /*
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new Jackson2HalModule());

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(MediaType.parseMediaTypes("application/hal+json"));
        converter.setObjectMapper(mapper);*/
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
        return backend.create(airline);
    }

    @Override
    public Airline update(Airline airline) {
        return null;
    }

    @Override
    public void delete(Link id) {

    }
}
