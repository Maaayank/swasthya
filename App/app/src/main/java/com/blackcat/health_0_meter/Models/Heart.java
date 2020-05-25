package com.blackcat.health_0_meter.Models;


public class Heart{

    private int rate;
    private String  timestamp;
    private String date ;

    public Heart() {
    }

    public Heart(int rate, String timestamp, String date) {
        this.rate = rate;
        this.timestamp = timestamp;
        this.date = date;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}