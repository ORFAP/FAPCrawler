package de.orfap.fap.crawler.crawlerpipes;

import de.orfap.fap.crawler.domain.Airline;

/**
 * Created by o4 on 22.06.2016.
 */
public class AirlineBuilder extends ResourceBuilder<String, Airline> {
    public AirlineBuilder(final boolean listAble, final String basePath) {
        super(listAble, basePath);
    }

    @Override
    public Airline transform(String data) {
        String workingdata = data;
        String[] columns;
        final Airline output;
        columns = workingdata.split(",");
        if (columns[0].matches("\"[0-9]{1,}\"")) {
            String name = columns[1].replaceAll("(\"|\\([1-9]\\))", "").trim();
            //Remove parts with information about merge
            if (name.contains("(Merged")) {
                name = name.substring(0, name.indexOf("(Merged")).trim();
            }
            //Remove d/b/a (doing business as, alias names for Airlines)
            if (name.contains("d/b/a")){
                name = name.substring(0, name.indexOf("d/b/a")).trim();
                name=name+":"+columns[1].split(":")[1];
            }
            output = new Airline(name, columns[0].replaceAll("\"", ""));
        } else {
            output = new Airline("", "");
        }
        //noinspection unchecked
        return output;
    }
}


