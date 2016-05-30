package de.orfap.fap.crawler.domain;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.util.Date;

/**
 * Created by Arne on 13.04.2016.
 */
@SuppressWarnings("DefaultFileTemplate")
public class Route {

    @NotNull
    @Past
    private Date date;

    @Min(0)
    private double delays;

    @Min(0)
    private double cancelled;

    @Min(0)
    private double passengerCount;

    @Min(0)
    private double flightCount;

    @NotNull
    private String airline;

    @NotNull
    private String source;

    @NotNull
    private String destination;

    public Route() {
    }

    public Route(Date date, double delays, double cancelled, double passengerCount, double flightCount, String airline, String source, String destination) {
        this.date = date;
        this.delays = delays;
        this.cancelled = cancelled;
        this.passengerCount = passengerCount;
        this.flightCount = flightCount;
        this.airline = airline;
        this.source = source;
        this.destination = destination;
    }

    private Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    private double getDelays() {
        return delays;
    }

    public void setDelays(double delays) {
        this.delays = delays;
    }

    public double getCancelled() {
        return cancelled;
    }

    public void setCancelled(double cancelled) {
        this.cancelled = cancelled;
    }

    private double getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(double passengerCount) {
        this.passengerCount = passengerCount;
    }

    private double getFlightCount() {
        return flightCount;
    }

    public void setFlightCount(double flightCount) {
        this.flightCount = flightCount;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (this.getClass() != o.getClass())
            return false;
        Route other = (Route) o;
        if (!this.getAirline().equals(other.getAirline()))
            return false;
        if (this.getCancelled() != other.getCancelled())
            return false;
        if (this.getDelays() != other.getDelays())
            return false;
        if (this.getFlightCount() != other.getFlightCount())
            return false;
        if (this.getPassengerCount() != other.getPassengerCount())
            return false;
        if (!this.getDate().equals(other.getDate()))
            return false;
        return this.getSource().equals(other.getSource()) && this.getDestination().equals(other.getDestination());
    }

    @Override
    public String toString() {
        return "Route{" +
                "date=" + date +
                ", delays=" + delays +
                ", cancelled=" + cancelled +
                ", passengerCount=" + passengerCount +
                ", flightCount=" + flightCount +
                ", airline=" + airline +
                ", source=" + source +
                ", destination=" + destination +
                '}';
    }
}
