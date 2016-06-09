package de.orfap.fap.crawler.crawlerpipes;

import de.orfap.fap.crawler.domain.Airline;
import de.orfap.fap.crawler.domain.Market;
import de.orfap.fap.crawler.domain.Route;
import edu.hm.obreitwi.arch.lab08.BaseFilter;
import org.springframework.beans.factory.annotation.Value;

import java.util.GregorianCalendar;

/**
 * Created by o4 on 03.06.16.
 */
public class ResourceBuilder<T, U> extends BaseFilter<T, U> {
    private Object object;
    private String s;
    @Value("${fap.backend.basePath}")
    private String basepath;

    public ResourceBuilder(final String s, final Object object) {
        this.s = s;
        this.object = object;
    }

    @Override
    public U transform(T data) {
        if (s instanceof String) {
            String workingdata = (String) data;
            String[] columns;
            if (object instanceof Airline) {
                final Airline output;
                columns = workingdata.split(",");
                if (columns[0].matches("\"[0-9]{1,}\"")) {
                    output = new Airline(columns[1].replaceAll("(\"|\\([1-9]\\))", "").trim(), columns[0].replaceAll("\"", ""));
                } else {
                    output = new Airline("", "");
                }
                //noinspection unchecked
                return (U) output;
            } else if (object instanceof Market) {
                final Market output;
                columns = workingdata.split("\",\"");
                if (columns[0].matches("\"[0-9]{1,}") && columns[1].matches(".*, [A-Z]{2}.*\"")) {
                    output = new Market(columns[1].trim().replaceAll("\"", ""), columns[0].replaceAll("\"", ""));
                } else {
                    output = new Market("", "");
                }
                //noinspection unchecked
                return (U) output;
            } else if (object instanceof Route) {
                final Route output;
                double delay = 0;
                columns = workingdata.split(",");
                // "DAY_OF_WEEK","FL_DATE","AIRLINE_ID","ORIGIN_CITY_MARKET_ID"
                // "DEST_CITY_MARKET_ID","ARR_DELAY_NEW","CANCELLED"
                output = new Route();
                GregorianCalendar gregorianCalendar = new GregorianCalendar(Integer.parseInt(columns[1].substring(0, 4)), Integer.parseInt(columns[1].substring(5, 7)) - 1, Integer.parseInt(columns[1].substring(8, 10)));
                output.setDate(gregorianCalendar.getTime());
                output.setCancelled(Double.parseDouble(columns[6]));
                //Cancelled Flights have empty arr delay fields
                if (Double.parseDouble(columns[6]) == 0) {
                    //Some arr delay fields are empty
                    if (!columns[5].isEmpty()) {
                        delay = Double.parseDouble(columns[5]);
                    }
                    output.setDelays(delay);
                    delay = 0;
                }
                output.setPassengerCount(0);
                output.setFlightCount(1);
                output.setAirline(basepath + "airlines/" + columns[2]);
                output.setSource(basepath + "markets/" + columns[3]);
                output.setDestination(basepath + "markets/" + columns[4]);
                return (U) output;
            }
        }
        return null;
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
}
