package com.travel.cab.service.application;

import android.app.Application;

import com.travel.cab.service.utils.preference.SharedPreference;


public class CustomApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreference.initShared(this);
    }
}
