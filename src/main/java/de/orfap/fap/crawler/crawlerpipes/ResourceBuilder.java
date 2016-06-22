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

//    @Override
//    public U transform(T data) {
//        if (s instanceof String) {
//            String workingdata = (String) data;
//            String[] columns;
//            if (object instanceof Airline) {
//                final Airline output;
//                columns = workingdata.split(",");
//                if (columns[0].matches("\"[0-9]{1,}\"")) {
//                    String name = columns[1].replaceAll("(\"|\\([1-9]\\))", "").trim();
//                    //Remove parts with information about merge
//                    if (name.contains("(Merged")) {
//                        name = name.substring(0, name.indexOf("(Merged")).trim();
//                    }
//                    output = new Airline(name, columns[0].replaceAll("\"", ""));
//                } else {
//                    output = new Airline("", "");
//                }
//                //noinspection unchecked
//                return (U) output;
//            } else if (object instanceof Market) {
//                final Market output;
//                columns = workingdata.split("\",\"");
//                if (columns[0].matches("\"[0-9]{1,}") && columns[1].matches(".*, [A-Z]{2}.*\"")) {
//                    output = new Market(columns[1].trim().replaceAll("\"", ""), columns[0].replaceAll("\"", ""));
//                } else {
//                    output = new Market("", "");
//                }
//                //noinspection unchecked
//                return (U) output;
//            } else if (object instanceof Route) {
//                final Route output = new Route();
//                columns = workingdata.split(",");
//                if (isRoute) {
//                    // "YEAR","MONTH","DEPARTURES_SCHEDULED","DEPARTURES_PERFORMED",
//                    // "PASSENGERS","AIRLINE_ID","ORIGIN_CITY_MARKET_ID",
//                    // "DEST_CITY_MARKET_ID"
//                    GregorianCalendar gregorianCalendar = new GregorianCalendar(, -1, 1,0,0,0);
//                    output.setDate(Integer.parseInt(columns[0])+"-"+String.format("%02d",Integer.parseInt(columns[1]))+"-"+"01");
//                    output.setCancelled(0);
//                    output.setDelays(0);
//                    output.setPassengerCount(Double.parseDouble(columns[4]));
//                    output.setFlightCount(0);
//                    if(listAble) {
//                        output.setAirline(columns[5]);
//                        output.setSource(columns[6]);
//                        output.setDestination(columns[7]);
//                    }else{
//                        output.setAirline(basePath + "airlines/" + columns[5]);
//                        output.setSource(basePath + "markets/" + columns[6]);
//                        output.setDestination(basePath + "markets/" + columns[7]);
//                    }
//                    if (output.getAirline() == null || output.getDate() == null || output.getDestination() == null || output.getSource() == null) {
//                        throw new AssertionError("This is bad");
//                    }
//                } else {
//                    double delay = 0;
//                    // "DAY_OF_WEEK","FL_DATE","AIRLINE_ID","ORIGIN_CITY_MARKET_ID"
//                    // "DEST_CITY_MARKET_ID","ARR_DELAY_NEW","CANCELLED"
//                    GregorianCalendar gregorianCalendar = new GregorianCalendar(Integer.parseInt(columns[1].substring(0, 4)), Integer.parseInt(columns[1].substring(5, 7)) - 1, Integer.parseInt(columns[1].substring(8, 10)),0,0,0);
//                    output.setDate(gregorianCalendar.getTime());
//                    output.setCancelled(Double.parseDouble(columns[6]));
//                    //Cancelled Flights have empty arr delay fields
//                    if (Double.parseDouble(columns[6]) == 0) {
//                        //Some arr delay fields are empty
//                        if (!columns[5].isEmpty()) {
//                            delay = Double.parseDouble(columns[5]);
//                        }
//                        output.setDelays(delay);
//                    }
//                    output.setPassengerCount(0);
//                    output.setFlightCount(1);
//                    if (listAble) {
//                        output.setAirline(columns[2]);
//                        output.setSource(columns[3]);
//                        output.setDestination(columns[4]);
//                    } else {
//                        output.setAirline(basePath + "airlines/" + columns[2]);
//                        output.setSource(basePath + "markets/" + columns[3]);
//                        output.setDestination(basePath + "markets/" + columns[4]);
//                    }
//                }
//                return (U) output;
//            }
//        }
//        return null;
//    }
//
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
