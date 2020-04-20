package com.blackcat.health_0_meter.Activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import com.blackcat.health_0_meter.R;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreenActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mAuth = FirebaseAuth.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        123);
            }else{
                startApp();
            }
        }else{
            startApp();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 123: {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startApp();
                }else if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 123);
                }
            }
        }
    }

    private void startApp(){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (CheckLoggedIn()) {

                    startActivity(new Intent(SplashScreenActivity.this, HomeScreenActivity.class));
                } else {
                    startActivity(new Intent(SplashScreenActivity.this,LoginActivity.class));
                }
                finish();
            }

        },1500);

    }

    private boolean CheckLoggedIn(){
        return (mAuth.getCurrentUser() != null);
    }
}
