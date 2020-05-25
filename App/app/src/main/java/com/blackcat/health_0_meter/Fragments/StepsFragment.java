package com.blackcat.health_0_meter.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.blackcat.health_0_meter.Models.Steps;
import com.blackcat.health_0_meter.R;
import com.blackcat.health_0_meter.Services.StepService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class StepsFragment extends Fragment implements NumberPicker.OnValueChangeListener{

    //Activity Views
    private TextView dayRecordText;
    private TextView stepText;
    private TextView timeText;
    private TextView orientationText;
    private TextView distanceText;
    private TextView speedText;
    private TextView notices ;
    private Button startButton;

    private Handler handler = new Handler();

    private SharedPreferences user;
    private int dayStepRecord;

    private FirebaseDatabase mdb;
    private DatabaseReference step_ref;

    private StepService service = null;
    private final String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
    private boolean isBound ;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = getActivity().getSharedPreferences("user",Context.MODE_PRIVATE);
        mdb = FirebaseDatabase.getInstance();
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
        orientationText = (TextView) view.findViewById(R.id.orientationText);
        notices = (TextView)view.findViewById(R.id.accuracy_alert);
        startButton = view.findViewById(R.id.startButton);

        try{
            String address = user.getString("address","");
            step_ref = mdb.getReference(address).child("stepstat");

        }catch (Exception e){

        }

        if(!checkSensors())
            startButton.setEnabled(false);


        //Step counting and other calculations start when user presses "start" button
        if (startButton != null) {
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(!isBound){
                        Snackbar.make(view,"Binding to the Step Counting Service , wait ... ",Snackbar.LENGTH_LONG).show();
                    }
                    else if (!service.isActive()) {
                        startButton.setText("Stop");
                        notices.setText(" The sensor has a Latency of 10 seconds . ");
                        service.startForegroundService();
                        startButton.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.color3));
                        startButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.color1));

                    } else {
                        startButton.setText("Start!");
                        service.stopForegroundService(true);
                        startButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.color2));
                        startButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.color1));
                        checkSensors();
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
                if(isBound && service.isActive())
                    service.resetCount();
            }
        });
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder_service) {
            StepService.MyBinder myBinder = (StepService.MyBinder) binder_service;
            service = myBinder.getService();
            isBound = true;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        dayStepRecord = Integer.parseInt(user.getString("DAY_STEP_RECORD", "2000"));
        dayRecordText.setText(String.format(getResources().getString(R.string.record), dayStepRecord));

        Intent intent = new Intent(getActivity(), StepService.class);
        getActivity().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        handler.postDelayed(timerRunnable, 0);

    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unbindService(mServiceConnection);
        handler.removeCallbacks(timerRunnable);
    }

    private boolean  checkSensors(){

        SensorManager sensorManager;
        Sensor stepDetectorSensor;
        Sensor accelerometer;
        Sensor magnetometer;
        Sensor stepCounter;

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

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

    //Runnable that calculates the elapsed time since the user presses the "start" button
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            HashMap<String,String> data ;

            if(isBound){

                if(isAdded()) {
                    data = service.getData();

                    stepText.setText(data.get("steps"));
                    timeText.setText(data.get("duration"));
                    speedText.setText(data.get("speed"));
                    distanceText.setText(data.get("distance"));
                    orientationText.setText(data.get("orientation"));

                    if(service.isActive()){
                        startButton.setText("Stop");
                        startButton.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.color3));
                        startButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.color1));
                    }else {
                        startButton.setText("Start!");
                        startButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.color2));
                        startButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.color1));
                    }

                }

                if(service.getStepCount() >= dayStepRecord && user.getBoolean(todayDate + "_step",true)){
                    showPopUp();
                }
            }
            handler.postDelayed(this, 1000);
        }
    };

    private void showPopUp(){

        user.edit().putBoolean(todayDate + "_step",false).apply();
        final Dialog goalDialog = new Dialog(getContext());

        goalDialog.setContentView(R.layout.dialog_congrats);
        TextView goalText = goalDialog.findViewById(R.id.goalSteps);
        goalText.setText(dayStepRecord + "");
        ImageView closePopUp = (ImageView)goalDialog.findViewById(R.id.closePopUp);
        Button sharebtn = (Button)goalDialog.findViewById(R.id.sharebutton);

        closePopUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goalDialog.dismiss();
            }
        });

        sharebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LinearLayout screenshotLayout = goalDialog.findViewById(R.id.screenshotlayout);
                Bitmap bitmap = takeScreenShot(screenshotLayout);
                File imagePath = saveScreenshot(bitmap);
                if(imagePath != null){
                    shareIt(1 , imagePath , dayStepRecord);
                }else{
                    shareIt(2 , null ,dayStepRecord);
                }

            }
        });
        Window window = goalDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        goalDialog.show();
    }

    private void shareIt(int s, File imagePath,int dayStepRecord){

        switch (s){

            case 1 : Uri uri = FileProvider.getUriForFile(getActivity(),"com.blackcat.fileprovider",imagePath);
                    getActivity().grantUriPermission(getActivity().getPackageName(),uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/*");
                    String sharebody = "Hey , I just acheived my goal of walking "+dayStepRecord+" steps .\n Join me on Swasthya app to stay fit and healthy .Let's be health competitors ! \n  See you on the Swasthya ground !";
                    String shareSubject  = "Goal Reached !! ";
                    intent.putExtra(Intent.EXTRA_SUBJECT,shareSubject);
                    intent.putExtra(Intent.EXTRA_TEXT,sharebody);
                    intent.putExtra(Intent.EXTRA_STREAM,uri);
                    intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    getActivity().startActivity(Intent.createChooser(intent,"Share via "));
                    break;
            default: Intent myIntent= new Intent(Intent.ACTION_SEND);
                    myIntent.setType("text/plain");
                    String shareBody = "Hey , I just acheived my goal of walking "+dayStepRecord+" steps .\n Join me on Swasthya app to stay fit and healthy .Let's be health competitors ! \n See you on the Swasthya ground !";
                    String shareSub = "Goal Reached !! ";
                    myIntent.putExtra(Intent.EXTRA_SUBJECT,shareSub);
                    myIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
                    startActivity(Intent.createChooser(myIntent, "Share via "));

        }
    }
    private Bitmap takeScreenShot(LinearLayout ss){
        ss.setDrawingCacheEnabled(true);
        return ss.getDrawingCache();
    }

    private File saveScreenshot(Bitmap ss){
        File directory = new File(getActivity().getFilesDir() + "/goalReached");
        if(!directory.exists())
            if(!directory.mkdir())
                Toast.makeText(getActivity(),"Error while creating directory",Toast.LENGTH_SHORT).show();
        File imagePath = new File(getActivity().getFilesDir() + "/goalReached/screenshot.jpg");
        FileOutputStream fos ;
        try{
            fos = new FileOutputStream(imagePath);
            ss.compress(Bitmap.CompressFormat.JPEG,100,fos);
            fos.flush();
            fos.close();
            return imagePath;
        }catch (FileNotFoundException e){
            Log.e("FNFE",e.getMessage(),e);
        }catch (IOException e){
            Log.e("IOE",e.getMessage(),e);
        }

        return  null;
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
                user.edit().putBoolean(todayDate + "_step",true).apply();
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