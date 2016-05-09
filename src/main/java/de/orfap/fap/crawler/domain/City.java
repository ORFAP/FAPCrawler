package de.orfap.fap.crawler.domain;

/**
 * Created by Arne on 13.04.2016.
 */
public class City {

    private String name;

    private String id;

    public City() {}

    public City(String name, String id) {
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
        City other = (City) o;
        if(!this.getName().equals(other.getName()))
            return false;
        if(!this.getId().equals(other.getId()))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "City{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
