package com.blackcat.health_0_meter.Models;

public class Steps {

    private long steps;
    private long duration;
    private double distance;
    private String date ;

    public Steps(long steps, long duration, double distance , String date) {
        this.steps = steps;
        this.duration = duration;
        this.distance = distance;
        this.date = date;
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

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
