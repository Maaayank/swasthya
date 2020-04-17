package com.example.mcan_miniproject.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mcan_miniproject.R;

public class StepsFragment extends Fragment implements SensorEventListener , NumberPicker.OnValueChangeListener{

    //Sensor related variables
    private SensorManager sensorManager;
    private Sensor stepDetectorSensor;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] accelValues;
    private float[] magnetValues;

    //Variables used in calculations
    private int stepCount = 0;
    private long stepTimestamp = 0;
    private long startTime = 0;
    long timeInMilliseconds = 0;
    long elapsedTime = 0;
    long updatedTime = 0;
    private double distance = 0;

    //Activity Views
    private TextView dayRecordText;
    private TextView stepText;
    private TextView timeText;
    private TextView orientationText;
    private TextView distanceText;
    private TextView speedText;

    private boolean active = false; //Used to checked if the counter is running
    private Handler handler = new Handler(); //Used to update the time in the UI

    //Preferences are used to remember the step record of the day
    private SharedPreferences sharedPreferences;
    private int dayStepRecord;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sharedPreferences = getActivity().getSharedPreferences("persistentStore",Context.MODE_PRIVATE);
//        if (stepDetectorSensor == null)
//            showErrorDialog();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_steps, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initialize views
        dayRecordText = (TextView) view.findViewById(R.id.dayRecordText);
        stepText = (TextView) view.findViewById(R.id.stepText);
        timeText = (TextView) view.findViewById(R.id.timeText);
        speedText = (TextView) view.findViewById(R.id.speedText);
        distanceText = (TextView) view.findViewById(R.id.distanceText);
        orientationText = (TextView) view.findViewById(R.id.orientationText);
        setViewDefaultValues();

        //Step counting and other calculations start when user presses "start" button
        final Button startButton = view.findViewById(R.id.startButton);
        if (startButton != null) {
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!active) {
                        startButton.setText("Pause");
                        startButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.darkGray));
                        sensorManager.registerListener(StepsFragment.this, stepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);
                        sensorManager.registerListener(StepsFragment.this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                        sensorManager.registerListener(StepsFragment.this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
                        startTime = SystemClock.uptimeMillis();
                        handler.postDelayed(timerRunnable, 0);
                        active = true;

                    } else {
                        startButton.setText("Start!");
                        startButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.lightGray));
                        sensorManager.unregisterListener(StepsFragment.this, stepDetectorSensor);
                        sensorManager.unregisterListener(StepsFragment.this, accelerometer);
                        sensorManager.unregisterListener(StepsFragment.this, magnetometer);
                        elapsedTime += timeInMilliseconds;
                        handler.removeCallbacks(timerRunnable);
                        active = false;
                    }
                }
            });
        }


        final Button setGoal = view.findViewById(R.id.setgoal);

        setGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
        //Reset all calculations to 0
        Button resetButton = (Button) view.findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stepCount = 0;
                distance = 0;
                elapsedTime = 0;
                setViewDefaultValues();
            }
        });

        
    }

    @Override
    public void onResume() {
        super.onResume();
        dayStepRecord = Integer.parseInt(sharedPreferences.getString("DAY_STEP_RECORD", "3000"));
        dayRecordText.setText(String.format(getResources().getString(R.string.record), dayStepRecord));
    }

    @Override
    public void onPause() {
        super.onPause();
        //Unregister for all sensors
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, magnetometer);
        sensorManager.unregisterListener(this, stepDetectorSensor);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //Get sensor values
        switch (event.sensor.getType()) {
            case (Sensor.TYPE_ACCELEROMETER):
                accelValues = event.values;
                break;
            case (Sensor.TYPE_MAGNETIC_FIELD):
                magnetValues = event.values;
                break;
            case (Sensor.TYPE_STEP_DETECTOR):
                countSteps(event.values[0]);
                calculateSpeed(event.timestamp);
                break;
        }

        if (accelValues != null && magnetValues != null) {
            float rotation[] = new float[9];
            float orientation[] = new float[3];
            if (SensorManager.getRotationMatrix(rotation, null, accelValues, magnetValues)) {
                SensorManager.getOrientation(rotation, orientation);
                float azimuthDegree = (float) (Math.toDegrees(orientation[0]) + 360) % 360;
                float orientationDegree = Math.round(azimuthDegree);
                getOrientation(orientationDegree);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //Calculates the number of steps and the other calculations related to them
    private void countSteps(float step) {

        //Step count
        stepCount += (int) step;
        stepText.setText(String.format(getResources().getString(R.string.steps), stepCount));

        //Distance calculation
        distance = stepCount * 0.8; //Average step length in an average adult
        String distanceString = String.format("%.2f", distance);
        distanceText.setText(String.format(getResources().getString(R.string.distance), distanceString));

    }


    //Calculated the amount of steps taken per minute at the current rate
    private void calculateSpeed(long eventTimeStamp) {
        long timestampDifference = eventTimeStamp - stepTimestamp;
        stepTimestamp = eventTimeStamp;
        double stepTime = timestampDifference / 1000000000.0;
        int speed = (int) (60 / stepTime);
        speedText.setText(String.format(getResources().getString(R.string.speed), speed));
    }


    //Show cardinal point (compass orientation) according to degree
    private void getOrientation(float orientationDegree) {
        String compassOrientation;
        if (orientationDegree >= 0 && orientationDegree < 90) {
            compassOrientation = "North";
        } else if (orientationDegree >= 90 && orientationDegree < 180) {
            compassOrientation = "East";
        } else if (orientationDegree >= 180 && orientationDegree < 270) {
            compassOrientation = "South";
        } else {
            compassOrientation = "West";
        }
        orientationText.setText(String.format(getResources().getString(R.string.orientation), compassOrientation));
    }

    //Runnable that calculates the elapsed time since the user presses the "start" button
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

            updatedTime = elapsedTime + timeInMilliseconds;

            int seconds = (int) (updatedTime / 1000);
            int minutes = seconds / 60;
            int hours = minutes / 60;
            seconds = seconds % 60;
            minutes = minutes % 60;

            String timeString = String.format("%d:%s:%s", hours, String.format("%02d", minutes), String.format("%02d", seconds));

            if (isAdded()) {
                timeText.setText(String.format(getResources().getString(R.string.time), timeString));
            }
            handler.postDelayed(this, 0);
        }
    };

    //Set all views to their initial value
    private void setViewDefaultValues() {
        stepText.setText(String.format(getResources().getString(R.string.steps), 0));
        timeText.setText(String.format(getResources().getString(R.string.time), "0:00:00"));
        speedText.setText(String.format(getResources().getString(R.string.speed), 0));
        distanceText.setText(String.format(getResources().getString(R.string.distance),"0"));
        orientationText.setText(String.format(getResources().getString(R.string.orientation), ""));
    }

    private void showErrorDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage("Necessary step sensors not available!");

        alertDialogBuilder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void showDialog()
    {

        final Dialog d = new Dialog(getActivity());
        d.setTitle("NumberPicker");
        d.setContentView(R.layout.dialog_set_goal);
        Button b1 = (Button) d.findViewById(R.id.button1);
        Button b2 = (Button) d.findViewById(R.id.button2);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker);

        //Display the number picker values in thousands
        final String[] displayedValues = new String[19];

        //Starting from 2000
        for (int i = 0; i < 19; i++)
            displayedValues [i] = String.valueOf((i + 2) * 1000);

        np.setMinValue(2);
        np.setMaxValue(20);
        np.setDisplayedValues(displayedValues);
        np.setWrapSelectorWheel(false);
        np.setOnValueChangedListener(this);
        b1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                dayStepRecord  = Integer.parseInt(displayedValues[np.getValue() - 2]);
                dayRecordText.setText(String.format(getResources().getString(R.string.record), dayStepRecord));
                sharedPreferences.edit().putString("DAY_STEP_RECORD",displayedValues[np.getValue() - 2]).apply();
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();

    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

    }
}