package de.orfap.fap.crawler.crawlerpipes;

import de.orfap.fap.crawler.domain.Route;
import edu.hm.obreitwi.arch.lab08.BaseFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by o4 on 09.06.16.
 */
public class Collector<T> extends BaseFilter<T, List<T>> {
    @Override
    public List<T> transform(T data) {
        return null;
    }

    @Override
    public List<T> deliver() {
        List<T> output = null;
        T temp = getIncoming().pull();
        if (temp != null) {
            output = new ArrayList<T>();
            output.add(temp);
            for (int i = 0; i < 1000; i++) {
                temp = getIncoming().pull();
                if (temp == null) {
                    return output;
                }
                output.add(temp);
            }
        }
        return output;
    }

    @Override
    public void accept(T datum) {

    }
}
