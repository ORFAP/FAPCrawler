package de.orfap.fap.crawler.crawlerpipes;

import de.orfap.fap.crawler.domain.Route;
import edu.hm.obreitwi.arch.lab08.BaseFilter;

/**
 * Created by ifw13017 on 23.06.2016.
 */
public class FlightFilter extends BaseFilter<Route, Route> {
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
            if (datum.getAirline() == null) {
                return;
            }
            if (datum.getDestination() == null) {
                return;
            }
            if (datum.getSource() == null) {
                return;
            }
        }
        getOutgoing().push(datum);
    }
}
