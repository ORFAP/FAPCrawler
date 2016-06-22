package de.orfap.fap.crawler.crawlerpipes;

import de.orfap.fap.crawler.domain.Airline;
import edu.hm.obreitwi.arch.lab08.BaseFilter;

/**
 * Created by o4 on 03.06.16.
 */
public class AirlineResourceBuilder extends BaseFilter<String, Airline> {

    @Override
    public Airline transform(String data) {
            String[] columns;
            final Airline output;

            columns = data.split(",");
            if (columns[0].matches("\"[0-9]{1,}\"")) {
                String name = columns[1].replaceAll("(\"|\\([1-9]\\))", "").trim();
                //Remove parts with information about merge
                if (name.contains("(Merged")) {
                    name = name.substring(0, name.indexOf("(Merged")).trim();
                }
                output = new Airline(name, columns[0].replaceAll("\"", ""));
            } else {
                output = new Airline("", "");
            }

            //noinspection unchecked
            return output;

    }

    @Override
    public Airline deliver() {
        return null;
    }

    @Override
    public void accept(String datum) {
        Airline result;
        if (datum == null) {
            result = null;
        } else {
            result = transform(datum);
        }
        getOutgoing().push(result);
    }
}
