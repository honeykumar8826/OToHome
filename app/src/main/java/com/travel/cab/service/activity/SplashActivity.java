package com.travel.cab.service.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.travel.cab.service.R;

public class SplashActivity extends AppCompatActivity {
    private static final int DELAY_TIME=1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this,IntroSliderActivity.class);
                startActivity(intent);
                finish();
            }
        },DELAY_TIME);
    }
}


