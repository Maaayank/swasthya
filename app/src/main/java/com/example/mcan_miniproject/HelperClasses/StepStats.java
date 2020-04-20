package com.example.mcan_miniproject.HelperClasses;

public class StepStats
{
    private int steps;
    private float distance;
    public StepStats(float distance,int steps)
    {
        this.distance = distance;
        this.steps = steps;
    }
    public StepStats()
    {

    }
//    public void setSteps(int number)
//    {
//        steps = number;
//    }
//    public void setDistance(int number)
//    {
//        distance = number;
//    }
    public float getDistance()
    {
        return distance;
    }

    public int getSteps()
    {
        return steps;
    }
}
