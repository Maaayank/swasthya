package com.blackcat.health_0_meter.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.blackcat.health_0_meter.Models.Steps;
import com.blackcat.health_0_meter.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class StepsFragment extends Fragment implements SensorEventListener , NumberPicker.OnValueChangeListener{

    //Sensor related variables
    private SensorManager sensorManager;
    private Sensor stepDetectorSensor;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private Sensor stepCounter;
    private float[] accelValues;
    private float[] magnetValues;

    //Variables used in calculations
    private int stepCount = 0;
    private int lastSteps = 0;
    private double lastDistance = 0;
    private int prevStepCount = 0;
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
    private TextView stepText1;
    private TextView timeText1;
    private TextView distanceText1;
    private TextView speedText1;
    private TextView notices ;
    private Button startButton;

    private boolean active = false;
    private Handler handler = new Handler();

    private SharedPreferences user;
    private int dayStepRecord;

    private FirebaseDatabase mdb;
    private DatabaseReference step_ref;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_steps, container, false);
        return root;
    }


    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initialize views
        dayRecordText = (TextView) view.findViewById(R.id.dayRecordText);
        stepText = (TextView) view.findViewById(R.id.stepText);
        timeText = (TextView) view.findViewById(R.id.timeText);
        speedText = (TextView) view.findViewById(R.id.speedText);
        distanceText = (TextView) view.findViewById(R.id.distanceText);
        stepText1 = (TextView) view.findViewById(R.id.stepText1);
        timeText1 = (TextView) view.findViewById(R.id.timeText1);
        speedText1 = (TextView) view.findViewById(R.id.speedText1);
        distanceText1 = (TextView) view.findViewById(R.id.distanceText1);
        orientationText = (TextView) view.findViewById(R.id.orientationText);
        notices = (TextView)view.findViewById(R.id.accuracy_alert);
        startButton = view.findViewById(R.id.startButton);
        startButton.setEnabled(false);
        user = getActivity().getSharedPreferences("user",Context.MODE_PRIVATE);

        mdb = FirebaseDatabase.getInstance();
        try{
            String address = user.getString("address","");
            step_ref = mdb.getReference(address).child("stepstat");

        }catch (Exception e){

        }

        setViewDefaultValues();

        if(!checkSensors())
            startButton.setEnabled(false);

        //Step counting and other calculations start when user presses "start" button

        if (startButton != null) {
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!active) {
                        startButton.setText("Stop");
                        registerSensors();
                        notices.setText(" The sensor has a Latency of 10 seconds . ");
                        startTime = SystemClock.uptimeMillis();
                        handler.postDelayed(timerRunnable, 0);
                        active = true;
                        startButton.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.color1));
                        startButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.color2));
                    } else {
                        startButton.setText("Start!");
                        startButton.setEnabled(false);
                        startButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.color2));
                        startButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.color1));
                        unregisterSensors();
                        checkSensors();
                        elapsedTime += timeInMilliseconds;
                        persistSteps();
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

        Button resetButton = (Button) view.findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stepCount = 0;
                distance = 0;
                elapsedTime = 0;
                startTime = 0;
                startButton.setText("Start!");
                unregisterSensors();
                checkSensors();
                handler.removeCallbacks(timerRunnable);
                active = false;
                setViewDefaultValues();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        dayStepRecord = Integer.parseInt(user.getString("DAY_STEP_RECORD", "3000"));
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
            case (Sensor.TYPE_STEP_COUNTER) :
                if(prevStepCount < 1){
                    prevStepCount = (int)event.values[0];
                }
                calculateSpeed(event.timestamp,(int)event.values[0] - prevStepCount - stepCount);
                countSteps((int)event.values[0] - prevStepCount - stepCount);
                break;
            case (Sensor.TYPE_STEP_DETECTOR):
                if(stepCounter == null ) {
                    countSteps((int)event.values[0]);
                    calculateSpeed(event.timestamp,1);
                }
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


    private void registerSensors(){

        if(stepDetectorSensor != null)
            sensorManager.registerListener(StepsFragment.this, stepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);

        if(accelerometer != null)
            sensorManager.registerListener(StepsFragment.this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        if(magnetometer != null)
            sensorManager.registerListener(StepsFragment.this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);

        if(stepCounter != null)
            sensorManager.registerListener(StepsFragment.this, stepCounter, SensorManager.SENSOR_DELAY_FASTEST);

    }

    private void unregisterSensors(){

        if(stepDetectorSensor != null)
            sensorManager.unregisterListener(StepsFragment.this, stepDetectorSensor);

        if(accelerometer != null)
            sensorManager.unregisterListener(StepsFragment.this, accelerometer);

        if(magnetometer != null)
            sensorManager.unregisterListener(StepsFragment.this, magnetometer);

        if(stepCounter != null)
            sensorManager.unregisterListener(StepsFragment.this , stepCounter);

    }

    private boolean checkSensors(){

        if( stepCounter != null ){
            notices.setText(" Step Counter Sensor available . ");

            if( magnetometer == null || accelerometer == null ){
                notices.setText( notices.getText().toString() + "\n Magnetometer or Accelerometer not available cannot calculate Direction . ");
            }else{
                notices.setText( notices.getText().toString() + "\n Rest All necessary sensors available .");
            }

            return true;

        }else if( stepDetectorSensor != null){
            notices.setText("Step Detector Sensor available . ");
            if( magnetometer == null || accelerometer == null ){
                notices.setText( notices.getText().toString() + "\n Magnetometer or Accelerometer not available cannot calculate Direction . ");
            }else{
                notices.setText( notices.getText().toString() + "\n Rest All necessary sensors available .");
            }

            return true;

        }else{
            notices.setText(" Step Counter and Step Detector Sensor not available \n cannot calculate Steps , Sorry . ");
            return false;
        }
    }
    //Calculates the number of steps and the other calculations related to them
    private void countSteps(int step) {

        //Step count
        stepCount += step;
        final String tp=new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        if(stepCount+lastSteps>dayStepRecord&&user.getBoolean(tp+"_step",true))
        {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
            builder1.setTitle("Congratulations");
            builder1.setMessage("Goal Achieved");
            AlertDialog alertDialog=builder1.create();
            alertDialog.show();
            user.edit().putBoolean(tp+"_step",false).apply();
        }
        stepText.setText(String.format(getResources().getString(R.string.steps), lastSteps + stepCount));

        //todo yaha pe dal if  stepcount > goal  to display  ,
        //Distance calculation
        distance = stepCount * 0.8; //Average step length in an average adult
        String distanceString = String.format("%.2f",lastDistance + distance);
        distanceText.setText(String.format(getResources().getString(R.string.distance), distanceString));

    }

    //Calculated the amount of steps taken per minute at the current rate
    private void calculateSpeed(long eventTimeStamp, int steps) {

        long timestampDifference = eventTimeStamp - stepTimestamp;
        stepTimestamp = eventTimeStamp;
        double stepTime = timestampDifference /1000000000.0;
        int speed = (int) (60 / stepTime);
        speedText.setText(String.format(getResources().getString(R.string.speed),speed*steps));
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


        final String timestamp = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

        try {
            step_ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    try {
                        if (dataSnapshot.child(timestamp).exists()) {
                            startButton.setEnabled(true);
                            Steps stepstat = dataSnapshot.child(timestamp).getValue(Steps.class);

                            lastDistance = stepstat.getDistance();
                            elapsedTime = stepstat.getDuration();
                            lastSteps = (int) stepstat.getSteps();
                            stepCount = 0;
                            distance = 0;
                            startTime = 0;
                            int seconds = (int) (stepstat.getDuration() / 1000);
                            int minutes = seconds / 60;
                            int hours = minutes / 60;
                            seconds = seconds % 60;
                            minutes = minutes % 60;

                            String timeString = String.format("%d:%s:%s", hours, String.format("%02d", minutes), String.format("%02d", seconds));
                            stepText1.setText(String.format(getResources().getString(R.string.steps1), stepstat.getSteps()));
                            timeText1.setText(String.format(getResources().getString(R.string.time), timeString));
                            speedText1.setText(String.format(getResources().getString(R.string.speed), (int) ((stepstat.getDistance() * 60000) / stepstat.getDuration())));
                            distanceText1.setText(String.format(getResources().getString(R.string.distance), String.format("%.2f", stepstat.getDistance())));

                            stepText.setText(String.format(getResources().getString(R.string.steps), 0));
                            timeText.setText(String.format(getResources().getString(R.string.time), "0:00:00"));
                            speedText.setText(String.format(getResources().getString(R.string.speed), 0));
                            distanceText.setText(String.format(getResources().getString(R.string.distance), "0"));
                            orientationText.setText(String.format(getResources().getString(R.string.orientation), ""));

                            Log.d("healtherror", "responsed");

                        } else {
                            startButton.setEnabled(true);
                            Log.d("healtherror", "inelse");
                            elapsedTime = 0;
                            lastSteps = 0;
                            stepText1.setText(String.format(getResources().getString(R.string.steps1), 0));
                            timeText1.setText(String.format(getResources().getString(R.string.time), "0:00:00"));
                            speedText1.setText(String.format(getResources().getString(R.string.speed), 0));
                            distanceText1.setText(String.format(getResources().getString(R.string.distance), "0"));

                            stepText.setText(String.format(getResources().getString(R.string.steps), 0));
                            timeText.setText(String.format(getResources().getString(R.string.time), "0:00:00"));
                            speedText.setText(String.format(getResources().getString(R.string.speed), 0));
                            distanceText.setText(String.format(getResources().getString(R.string.distance), "0"));
                            orientationText.setText(String.format(getResources().getString(R.string.orientation), ""));
                        }
                    }catch(Exception e){

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d("healtherror", databaseError.getMessage());
                    Toast.makeText(getActivity(), "Some Error in fetching Previous Records ", Toast.LENGTH_LONG).show();
                }
            });
        }catch (Exception e){

        }

    }

    private void persistSteps(){

        try {
            final String timestamp = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
            Steps steps = new Steps(stepCount + lastSteps, elapsedTime, (stepCount + lastSteps) * 0.8, timestamp);
            step_ref.child(timestamp).setValue(steps).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("healtherror", e.getMessage());
                }
            });
        }catch (Exception e){

        }
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
                user.edit().putString("DAY_STEP_RECORD",displayedValues[np.getValue() - 2]).apply();
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