package de.orfap.fap.crawler.crawlerpipes;

import de.orfap.fap.crawler.domain.Airline;
import de.orfap.fap.crawler.domain.Market;
import de.orfap.fap.crawler.domain.Route;
import edu.hm.obreitwi.arch.lab08.BaseFilter;

import java.util.GregorianCalendar;

/**
 * Created by o4 on 03.06.16.
 */
public abstract class ResourceBuilder<T, U> extends BaseFilter<T, U> {
    private final boolean listAble;
    private final String basePath;

    public ResourceBuilder(final boolean listAble, final String basePath) {
        this.listAble = listAble;
        this.basePath = basePath;
    }

    @Override
    public U deliver() {
        return null;
    }

    @Override
    public void accept(T datum) {
        U result;
        if (datum == null) {
            result = null;
        } else {
            result = transform(datum);
        }
        getOutgoing().push(result);
    }

    public boolean isListAble() {
        return listAble;
    }

    public String getBasePath() {
        return basePath;
    }
}
