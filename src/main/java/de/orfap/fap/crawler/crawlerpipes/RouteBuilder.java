package de.orfap.fap.crawler.crawlerpipes;

import de.orfap.fap.crawler.domain.Route;

/**
 * Created by o4 on 22.06.2016.
 */
public class RouteBuilder extends ResourceBuilder<String, Route> {

    public RouteBuilder(final boolean listAble, final String basePath) {
        super(listAble, basePath);
    }

    @Override
    public Route transform(String data) {
        String workingdata = data;
        String[] columns;
        final Route output = new Route();
        columns = workingdata.split(",");
        // "YEAR","MONTH","DEPARTURES_SCHEDULED","DEPARTURES_PERFORMED",
        // "PASSENGERS","AIRLINE_ID","ORIGIN_CITY_MARKET_ID",
        // "DEST_CITY_MARKET_ID"
        if (columns[0] != null || columns[1] != null || columns[4] != null) {
            output.setDate(columns[0] + "-" + String.format("%02d", Integer.parseInt(columns[1])) + "-" + "01");
            output.setPassengerCount(Double.parseDouble(columns[4]));
        }
        if (isListAble()) {
            output.setAirline(columns[5]);
            output.setSource(columns[6]);
            output.setDestination(columns[7]);
        } else {
            output.setAirline(getBasePath() + "airlines/" + columns[5]);
            output.setSource(getBasePath() + "markets/" + columns[6]);
            output.setDestination(getBasePath() + "markets/" + columns[7]);
        }
        if (output.getAirline() == null || output.getDate() == null || output.getDestination() == null || output.getSource() == null) {
            throw new AssertionError("This is bad");
        }
        return output;
    }
}