package de.orfap.fap.crawler.rest;

import de.orfap.fap.crawler.domain.City;
import de.orfap.fap.crawler.feign.CityClient;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by Arne on 13.04.2016.
 */
@Service
public class CityRestClientImpl implements CityRestClient {

    public static final String FIND_FULL_TEXT_FUZZY = "findFullTextFuzzy";

    public static final String SEARCH = "search";

    CityClient backend;


    @Autowired
    public CityRestClientImpl(@Value("${fap.backend.basePath}")
                                         String basePath) {

        backend = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .target(CityClient.class, basePath);

        /*
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new Jackson2HalModule());

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(MediaType.parseMediaTypes("application/hal+json"));
        converter.setObjectMapper(mapper);*/
    }

    @Override
    public List<City> findAll() {
        return null;
    }

    @Override
    public List<City> findAll(Link relation) {
        return null;
    }

    @Override
    public List<City> findFullTextFuzzy(String filter) {
        return null;
    }

    @Override
    public Optional<City> findOne(Link link) {
        return null;
    }

    @Override
    public void setRelations(Link endpoint, Collection<Link> links) {

    }

    @Override
    public void setRelation(Link endpoint, Link relation) {

    }

    @Override
    public City create(City city) {
       return backend.create(city);
    }

    @Override
    public City update(City city) {
        return null;
    }

    @Override
    public void delete(Link id) {

    }
}
