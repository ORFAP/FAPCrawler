package de.orfap.fap.crawler.crawlerpipes;

import de.orfap.fap.crawler.domain.Route;
import edu.hm.obreitwi.arch.lab08.BaseFilter;

/**
 * Created by o4 on 03.06.16.
 */
public class RouteResourceBuilder extends BaseFilter<String, Route> {
    private final boolean listAble;
    private final String basePath;

    public RouteResourceBuilder(final boolean listAble, final String basePath) {
        this.listAble = listAble;
        this.basePath = basePath;
    }

    @Override
    public Route transform(String data) {
            String[] columns;
            final Route output = new Route();
            columns = data.split(",");

            // "YEAR","MONTH","DEPARTURES_SCHEDULED","DEPARTURES_PERFORMED",
            // "PASSENGERS","AIRLINE_ID","ORIGIN_CITY_MARKET_ID",
            // "DEST_CITY_MARKET_ID"
            output.setDate(columns[1]);
            output.setCancelled(0);
            output.setDelays(0);
            output.setPassengerCount(Double.parseDouble(columns[4]));
            output.setFlightCount(0);
            if(listAble) {
                output.setAirline(columns[5]);
                output.setSource(columns[6]);
                output.setDestination(columns[7]);
            }else{
                output.setAirline(basePath + "airlines/" + columns[5]);
                output.setSource(basePath + "markets/" + columns[6]);
                output.setDestination(basePath + "markets/" + columns[7]);
            }
            if (output.getAirline() == null || output.getDate() == null || output.getDestination() == null || output.getSource() == null) {
                throw new AssertionError("This is bad");
            }


            return output;
    }

    @Override
    public Route deliver() {
        return null;
    }

    @Override
    public void accept(String datum) {
        Route result;
        if (datum == null) {
            result = null;
        } else {
            result = transform(datum);
        }
        getOutgoing().push(result);
    }
}
