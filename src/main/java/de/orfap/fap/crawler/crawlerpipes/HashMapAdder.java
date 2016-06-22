package de.orfap.fap.crawler.crawlerpipes;

import de.orfap.fap.crawler.domain.Airline;
import de.orfap.fap.crawler.domain.Market;
import edu.hm.obreitwi.arch.lab08.BaseConsumer;

import java.util.HashMap;

/**
 * Created by o4 on 20.06.2016.
 */
public class HashMapAdder<T> extends BaseConsumer<T> {
    private HashMap<String, T> hashMap;

    public HashMapAdder(final HashMap<String, T> hashMap){
        this.hashMap=hashMap;
    }
    @Override
    public void accept(T data) {
        if(data instanceof Airline && !(((Airline) data).getId().isEmpty() || ((Airline) data).getName().isEmpty())){
            hashMap.put(((Airline)data).getId(),data);
        }
        else if (data instanceof Market && !(((Market) data).getId().isEmpty() || ((Market) data).getName().isEmpty())){
            hashMap.put(((Market)data).getId(),data);
        }
    }
}
