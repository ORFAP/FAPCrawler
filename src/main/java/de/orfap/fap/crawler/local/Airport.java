package de.orfap.fap.crawler.local;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.hateoas.ResourceSupport;

/**
 * Created by Arne on 13.04.2016.
 */
public class Airport extends ResourceSupport{

    @NotNull
    @Size(min = 3)
    String name;

    public Airport() {}

    public Airport(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o){
        if(this == o)
            return true;
        if(this.getClass() != o.getClass())
            return false;
        Airport other = (Airport) o;
        if(!this.getName().equals(other.getName()))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Airport{" +
                "name='" + name + '\'' +
                '}';
    }
}
