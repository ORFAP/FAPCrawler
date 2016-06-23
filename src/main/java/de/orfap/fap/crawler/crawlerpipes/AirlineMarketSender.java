package de.orfap.fap.crawler.crawlerpipes;

import de.orfap.fap.crawler.domain.Airline;
import de.orfap.fap.crawler.domain.Market;
import de.orfap.fap.crawler.domain.Route;
import de.orfap.fap.crawler.feign.AirlineClient;
import de.orfap.fap.crawler.feign.MarketClient;
import edu.hm.obreitwi.arch.lab08.BaseFilter;

import java.util.HashMap;

/**
 * Created by o4 on 20.06.2016.
 */
@SuppressWarnings("Duplicates")
public class AirlineMarketSender<T> extends BaseFilter<T,T> {
    private final HashMap<String, Airline> airlines;
    private final HashMap<String, Airline> usedAirlines;
    private final HashMap<String, Market> markets;
    private final HashMap<String, Market> usedMarkets;
    private AirlineClient airlineClient;
    private MarketClient marketClient;

    public AirlineMarketSender(final HashMap<String, Airline> airlines, final HashMap<String, Airline> usedAirlines, final HashMap<String, Market> markets, final HashMap<String, Market> usedMarkets, final AirlineClient airlineClient, final MarketClient marketClient) {
        this.airlines = airlines;
        this.usedAirlines = usedAirlines;
        this.markets = markets;
        this.usedMarkets = usedMarkets;
        this.airlineClient = airlineClient;
        this.marketClient = marketClient;
    }

    @Override
    public T transform(T data) {
        if (data instanceof Route) {
            String keyAirline = ((Route) data).getAirline();
            if (!usedAirlines.containsKey(keyAirline)) {
                synchronized (airlines) {
                    if (!usedAirlines.containsKey(keyAirline)) {
                        airlineClient.create(airlines.get(keyAirline));
                        usedAirlines.put(keyAirline, airlines.get(keyAirline));
                    }
                }
            }
            String keySource = ((Route) data).getSource();
            checkAndCreateMarket(keySource);

            String keyDestination = ((Route) data).getDestination();
            checkAndCreateMarket(keyDestination);
            return data;
        }
        return null;
    }

    private void checkAndCreateMarket(String key){
        if (!usedMarkets.containsKey(key)) {
            synchronized (markets) {
                if (!usedMarkets.containsKey(key)) {
                    marketClient.create(markets.get(key));
                    usedMarkets.put(key, markets.get(key));
                }
            }
        }
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