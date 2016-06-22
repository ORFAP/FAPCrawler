package de.orfap.fap.crawler.crawlerpipes;

import de.orfap.fap.crawler.domain.Route;
import edu.hm.obreitwi.arch.lab08.BaseFilter;

/**
 * Created by o4 on 03.06.16.
 */
public class FlightResourceBuilder extends BaseFilter<String, Route> {
    private final boolean listAble;
    private final String basePath;

    public FlightResourceBuilder(final boolean listAble, final String basePath) {
        this.listAble = listAble;
        this.basePath = basePath;
    }

    @Override
    public Route transform(String data) {
            String[] columns;
            final Route output = new Route();
            columns = data.split(",");

            double delay = 0;
            // "DAY_OF_WEEK","FL_DATE","AIRLINE_ID","ORIGIN_CITY_MARKET_ID"
            // "DEST_CITY_MARKET_ID","ARR_DELAY_NEW","CANCELLED"
            output.setDate(columns[1]);
            output.setCancelled(Double.parseDouble(columns[6]));
            //Cancelled Flights have empty arr delay fields
            if (Double.parseDouble(columns[6]) == 0) {
                //Some arr delay fields are empty
                if (!columns[5].isEmpty()) {
                    delay = Double.parseDouble(columns[5]);
                }
                output.setDelays(delay);
            }
            output.setPassengerCount(0);
            output.setFlightCount(1);
            if (listAble) {
                output.setAirline(columns[2]);
                output.setSource(columns[3]);
                output.setDestination(columns[4]);
            } else {
                output.setAirline(basePath + "airlines/" + columns[2]);
                output.setSource(basePath + "markets/" + columns[3]);
                output.setDestination(basePath + "markets/" + columns[4]);
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
