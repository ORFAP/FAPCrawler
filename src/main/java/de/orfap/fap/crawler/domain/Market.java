package de.orfap.fap.crawler.domain;

import org.springframework.data.annotation.Id;

/**
 * Created by Arne on 13.04.2016.
 */
@SuppressWarnings("DefaultFileTemplate")
public class Market {

    private String name;

    @Id
    private String id;

    public Market() {
    }

    public Market(String name, String id) {
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
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (this.getClass() != o.getClass())
            return false;
        Market other = (Market) o;
        return this.getName().equals(other.getName()) && this.getId().equals(other.getId());
    }

    @Override
    public String toString() {
        return "Market{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
