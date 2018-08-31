package com.mkanchwala.loggers.gmap;

public class DistanceElements {

    private Distance distance;
    private Duration duration;
    private String status;

    public DistanceElements(Distance distance, Duration duration, String status) {
        this.distance = distance;
        this.duration = duration;

        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public DistanceElements() {
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Distance getDistance() {
        return distance;
    }

    public void setDistance(Distance distance) {
        this.distance = distance;
    }

}

