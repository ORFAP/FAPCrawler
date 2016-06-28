package de.orfap.fap.crawler.crawlerpipes;

import de.orfap.fap.crawler.domain.Route;
import edu.hm.obreitwi.arch.lab08.BaseFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by o4 on 23.06.2016.
 */
public class FlightFilter extends BaseFilter<Route, Route> {
    private final Logger LOG = LoggerFactory.getLogger(FlightFilter.class);

    @Override
    public Route transform(Route data) {
        return null;
    }

    @Override
    public Route deliver() {
        return null;
    }

    @Override
    public void accept(Route datum) {
        if (datum != null) {
            if (datum.getAirline() == null || datum.getAirline().isEmpty()) {
                LOG.debug("invalid Route-Object detected & dropped" + datum.toString());
                return;
            }
            if (datum.getDestination() == null || datum.getDestination().isEmpty()) {
                LOG.debug("invalid Route-Object detected & dropped" + datum.toString());
                return;
            }
            if (datum.getSource() == null || datum.getSource().isEmpty()) {
                LOG.debug("invalid Route-Object detected & dropped" + datum.toString());
                return;
            }
        }
        getOutgoing().push(datum);
    }
}
