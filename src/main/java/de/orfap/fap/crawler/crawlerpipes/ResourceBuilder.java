package de.orfap.fap.crawler.crawlerpipes;

import de.orfap.fap.crawler.domain.Airline;
import edu.hm.obreitwi.arch.lab08.BaseFilter;

/**
 * Created by o4 on 03.06.16.
 */
public class ResourceBuilder<T, U> extends BaseFilter<T, U> {
    private Object object;
    private String s;

    public ResourceBuilder(String s, Object object) {
        this.s = s;
        this.object = object;
    }

    @Override
    public U transform(T data) {
        if (s instanceof String && object instanceof Airline) {
            final Airline output;
            String workingdata = (String) data;
            String[] parts = workingdata.split(",");
            if (parts[0].matches("\"[0-9]{1,}\"")) {
                output = new Airline(parts[1].replaceAll("(\"|\\([1-9]\\))", "").trim(), parts[0].replaceAll("\"", ""));
            } else {
                output = new Airline("", "");
            }
            return (U) output;
        }
        return null;
    }

    @Override
    public U deliver() {
        return null;
    }

    @Override
    public void accept(T datum) {
        U result = transform(datum);
        getOutgoing().push(result);
    }
}
