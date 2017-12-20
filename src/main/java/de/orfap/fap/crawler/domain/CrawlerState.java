package de.orfap.fap.crawler.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CrawlerState {

    private final String year;
    private final String month;
    private final LocalDateTime startTime;
    private boolean finished = false;

    public CrawlerState(String year, String month, LocalDateTime startTime) {
        this.year = year;
        this.month = month;
        this.startTime = startTime;
    }

    public String getYear() {
        return year;
    }

    public String getMonth() {
        return month;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
