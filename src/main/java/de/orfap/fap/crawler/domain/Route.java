package de.orfap.fap.crawler.domain;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

import java.util.Date;

/**
 * Created by Arne on 13.04.2016.
 */
public class Route {

  @NotNull
  @Past
  private Date date;

  @Min(0)
  private int delays;

  @Min(0)
  private int cancelled;

  @Min(0)
  private int passengerCount;

  @Min(0)
  private int flightCount;

  @NotNull
  private String airline;

  @NotNull
  private String source;

  @NotNull
  private String destination;

  public Route() {
  }

  public Route(Date date, int delays, int cancelled, int passengerCount, int flightCount, String airline, String source, String destination) {
    this.date = date;
    this.delays = delays;
    this.cancelled = cancelled;
    this.passengerCount = passengerCount;
    this.flightCount = flightCount;
    this.airline = airline;
    this.source = source;
    this.destination = destination;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public int getDelays() {
    return delays;
  }

  public void setDelays(int delays) {
    this.delays = delays;
  }

  public int getCancelled() {
    return cancelled;
  }

  public void setCancelled(int cancelled) {
    this.cancelled = cancelled;
  }

  public int getPassengerCount() {
    return passengerCount;
  }

  public void setPassengerCount(int passengerCount) {
    this.passengerCount = passengerCount;
  }

  public int getFlightCount() {
    return flightCount;
  }

  public void setFlightCount(int flightCount) {
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
    if (!this.getSource().equals(other.getSource()))
      return false;
    if (!this.getDestination().equals(other.getDestination()))
      return false;
    return true;
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
