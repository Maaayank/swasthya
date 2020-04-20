package com.blackcat.health_0_meter.Models;

public class Steps {

    private long steps;
    private long duration;
    private double distance;

    public Steps(long steps, long duration, double distance) {
        this.steps = steps;
        this.duration = duration;
        this.distance = distance;
    }

    public Steps(){

    }

    public long getSteps() {
        return steps;
    }

    public void setSteps(long steps) {
        this.steps = steps;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }
}
