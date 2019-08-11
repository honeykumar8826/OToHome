package com.travel.cab.service.application;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.travel.cab.service.ui.IntentFilterCondition;
import com.travel.cab.service.utils.preference.SharedPreference;
import io.fabric.sdk.android.Fabric;


public class CustomApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        SharedPreference.initShared(this);
        IntentFilterCondition.initialiseInstance(this);
    }
}
