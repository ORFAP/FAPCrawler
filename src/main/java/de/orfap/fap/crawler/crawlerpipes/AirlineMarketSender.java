package de.orfap.fap.crawler.crawlerpipes;

import de.orfap.fap.crawler.domain.Airline;
import de.orfap.fap.crawler.domain.Market;
import de.orfap.fap.crawler.domain.Route;
import de.orfap.fap.crawler.feign.AirlineClient;
import de.orfap.fap.crawler.feign.MarketClient;
import edu.hm.obreitwi.arch.lab08.BaseFilter;

import java.util.HashMap;

/**
 * Created by ifw13017 on 20.06.2016.
 */
public class AirlineMarketSender<T> extends BaseFilter<T,T> {
    private HashMap airlines;
    private HashMap usedAirlines;
    private HashMap markets;
    private HashMap usedMarkets;
    private AirlineClient airlineClient;
    private MarketClient marketClient;

    public AirlineMarketSender(final HashMap airlines, final HashMap usedAirlines, final HashMap markets, final HashMap usedMarkets, final AirlineClient airlineClient, final MarketClient marketClient) {
        this.airlines = airlines;
        this.usedAirlines = usedAirlines;
        this.markets = markets;
        this.usedMarkets = usedMarkets;
        this.airlineClient=airlineClient;
        this.marketClient=marketClient;
    }

    @Override
    public T transform(T data) {
        if (data instanceof Route) {
            String keyAirline = ((Route) data).getAirline();
            if (!usedAirlines.containsKey(keyAirline)) {
                synchronized(airlines) {
                    airlineClient.create((Airline)airlines.get(keyAirline));
                    usedAirlines.put(keyAirline, airlines.get(keyAirline));
                }
            }
            String keySource = ((Route) data).getSource();
            if (!usedMarkets.containsKey(keySource)) {
                synchronized(markets) {
                    marketClient.create((Market)markets.get(keySource));
                    usedMarkets.put(keySource, markets.get(keySource));
                }
            }
            String keyDestination = ((Route) data).getDestination();
            if (!usedMarkets.containsKey(keyDestination)) {
                synchronized(markets) {
                    marketClient.create((Market)markets.get(keyDestination));
                    usedMarkets.put(keyDestination, markets.get(keyDestination));
                }
            }
            return data;
        }
        return null;
    }

    @Override
    public T deliver() {
        return null;
    }

    @Override
    public void accept(T datum) {
        T result;
        if (datum == null) {
            result = null;
        } else {
            result = transform(datum);
        }
        getOutgoing().push(result);
    }
}
