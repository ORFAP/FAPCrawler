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

    @NotNull
    String id;

    public Airline() {}

    public Airline(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        if(!this.getId().equals(other.getId()))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Airline{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
