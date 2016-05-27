package de.orfap.fap.crawler.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.hal.Jackson2HalModule;

/**
 * Organization: HM FK07.
 * Project: fapcrawler, de.orfap.fap.crawler.config
 * Author(s): Rene Zarwel
 * Date: 21.04.16
 * OS: MacOS 10.11
 * Java-Version: 1.8
 * System: 2,3 GHz Intel Core i7, 16 GB 1600 MHz DDR3
 */
@Configuration
public class FeignConfiguration {

    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new Jackson2HalModule());


    @Bean
    @ConditionalOnMissingBean
    Encoder getFeignEncoder() {

        return new JacksonEncoder(mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    Decoder getFeignDecoder() {

        return new JacksonDecoder(mapper);
    }
}
