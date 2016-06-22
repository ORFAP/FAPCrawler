package de.orfap.fap.crawler.crawlerpipes;

import de.orfap.fap.crawler.domain.Market;
import edu.hm.obreitwi.arch.lab08.BaseFilter;

/**
 * Created by o4 on 03.06.16.
 */
public class MarketResourceBuilder extends BaseFilter<String, Market> {

    @Override
    public Market transform(String data) {
            String[] columns;

            final Market output;
            columns = data.split("\",\"");
            if (columns[0].matches("\"[0-9]{1,}") && columns[1].matches(".*, [A-Z]{2}.*\"")) {
                output = new Market(columns[1].trim().replaceAll("\"", ""), columns[0].replaceAll("\"", ""));
            } else {
                output = new Market("", "");
            }
            //noinspection unchecked
            return output;


    }

    @Override
    public Market deliver() {
        return null;
    }

    @Override
    public void accept(String datum) {
        Market result;
        if (datum == null) {
            result = null;
        } else {
            result = transform(datum);
        }
        getOutgoing().push(result);
    }
}
