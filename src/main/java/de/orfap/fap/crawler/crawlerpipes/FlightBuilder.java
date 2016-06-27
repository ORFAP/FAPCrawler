package de.orfap.fap.crawler.crawlerpipes;

import de.orfap.fap.crawler.domain.Route;

/**
 * Created by o4 on 22.06.2016.
 */
public class FlightBuilder extends ResourceBuilder<String, Route> {

    public FlightBuilder(final boolean listAble, final String basePath) {
        super(listAble, basePath);
    }

    @Override
    public Route transform(String data) {
        String workingdata = data;
        String[] columns;
        final Route output = new Route();
        columns = workingdata.split(",");
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
        if (isListAble()) {
            output.setAirline(columns[2]);
            output.setSource(columns[3]);
            output.setDestination(columns[4]);
        } else {
            output.setAirline(getBasePath() + "airlines/" + columns[2]);
            output.setSource(getBasePath() + "markets/" + columns[3]);
            output.setDestination(getBasePath() + "markets/" + columns[4]);
        }
        return output;
    }
}
