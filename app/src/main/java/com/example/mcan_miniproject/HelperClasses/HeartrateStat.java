package com.example.mcan_miniproject.HelperClasses;

import java.sql.Timestamp;

public class HeartrateStat
{
    public int timestamp;
    public int rate;
    HeartrateStat( int rate,int timestamp )
    {
        this.rate = rate;
        this.timestamp = timestamp;
    }
    HeartrateStat()
    {

    }

    public int getRate()
    {
        return rate;
    }

    public void setRate(int rate)
    {
        this.rate = rate;
    }

    public int getTimestamp()
    {
        return timestamp;
    }
    public void setTimestamp(int timestamp)
    {
        this.timestamp = timestamp;
    }
}
