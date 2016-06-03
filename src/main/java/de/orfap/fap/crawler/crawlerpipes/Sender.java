package de.orfap.fap.crawler.crawlerpipes;

import de.orfap.fap.crawler.domain.Airline;
import de.orfap.fap.crawler.feign.AirlineClient;
import de.orfap.fap.crawler.feign.MarketClient;
import de.orfap.fap.crawler.feign.RouteClient;
import edu.hm.obreitwi.arch.lab08.BaseConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by o4 on 03.06.16.
 */
public class Sender<T> extends BaseConsumer<T> {
    //Warnings suppressed because of: No beans needed
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private AirlineClient airlineClient;
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private MarketClient marketClient;
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private RouteClient routeClient;
    @Value("${fap.backend.basePath}")
    private String basepath;

    @Override
    public void accept(T data) {
        if (data instanceof Airline && !(((Airline) data).getId().isEmpty() || ((Airline) data).getName().isEmpty())) {
            airlineClient.create((Airline) data);
        }
    }
}
