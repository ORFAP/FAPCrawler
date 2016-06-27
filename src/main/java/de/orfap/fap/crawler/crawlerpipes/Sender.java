package de.orfap.fap.crawler.crawlerpipes;

import de.orfap.fap.crawler.domain.Airline;
import de.orfap.fap.crawler.domain.Market;
import de.orfap.fap.crawler.domain.Route;
import de.orfap.fap.crawler.feign.AirlineClient;
import de.orfap.fap.crawler.feign.MarketClient;
import de.orfap.fap.crawler.feign.RouteClient;
import edu.hm.obreitwi.arch.lab08.BaseConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by o4 on 03.06.16.
 */
public class Sender<T> extends BaseConsumer<T> {
    private int numberSendOperations;
    private final AirlineClient airlineClient;
    private final MarketClient marketClient;
    private final RouteClient routeClient;
    private final Logger LOG = LoggerFactory.getLogger(Sender.class);

    public Sender(AirlineClient airlineClient, MarketClient marketClient, RouteClient routeClient) {
        this.airlineClient = airlineClient;
        this.marketClient = marketClient;
        this.routeClient = routeClient;
    }

    @Override
    public void accept(T data) {
        if (data == null) {
            LOG.info("Sent " + numberSendOperations + " Flights to Backend");
            return;
        }
        if (data instanceof Airline && !(((Airline) data).getId().isEmpty() || ((Airline) data).getName().isEmpty())) {
            airlineClient.create((Airline) data);
            numberSendOperations++;
        } else if (data instanceof Market && !(((Market) data).getId().isEmpty() || ((Market) data).getName().isEmpty())) {
            marketClient.create((Market) data);
            numberSendOperations++;
        } else if (data instanceof Route) {
            routeClient.create((Route) data);
            numberSendOperations++;
        } else if (data instanceof List) {
            routeClient.create((List) data);
            numberSendOperations += ((List) data).size();
        }
    }
}
