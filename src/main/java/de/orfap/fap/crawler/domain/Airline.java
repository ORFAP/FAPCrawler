package de.orfap.fap.crawler.domain;

import org.springframework.hateoas.ResourceSupport;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by Arne on 13.04.2016.
 */
public class Airline {

    @NotNull
    @Size(min = 3)
    String name;

    public Airline() {}

    public Airline(String name) {
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
        Airline other = (Airline) o;
        if(!this.getName().equals(other.getName()))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Airline{" +
                "name='" + name + '\'' +
                '}';
    }
}
