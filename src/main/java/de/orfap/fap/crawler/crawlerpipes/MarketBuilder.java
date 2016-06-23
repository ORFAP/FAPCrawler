package de.orfap.fap.crawler.crawlerpipes;

import de.orfap.fap.crawler.domain.Market;

/**
 * Created by o4 on 22.06.2016.
 */
public class MarketBuilder extends ResourceBuilder<String, Market> {

    public MarketBuilder(final boolean listAble, final String basePath) {
        super(listAble, basePath);
    }

    @Override
    public Market transform(String data) {
        String workingdata = data;
        String[] columns;
        final Market output;
        columns = workingdata.split("\",\"");
        if (columns[0].matches("\"[0-9]{1,}") && columns[1].matches(".*, [A-Z]{2}.*\"")) {
            output = new Market(columns[1].trim().replaceAll("\"", ""), columns[0].replaceAll("\"", ""));
        } else {
            output = new Market("", "");
        }
        //noinspection unchecked
        return output;
    }
}
