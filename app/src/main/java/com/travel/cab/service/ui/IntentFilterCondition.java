package com.travel.cab.service.ui;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;


public class IntentFilterCondition {
    private static IntentFilterCondition intentFilterCondition;
    private IntentFilter intentFilter ;
    private IntentFilterCondition(Context context) {
        intentFilter = new IntentFilter();
    }

    public static IntentFilterCondition getInstance() {
        return intentFilterCondition;
    }

    public static void initialiseInstance(Context context) {
        if (intentFilterCondition == null) {
            intentFilterCondition = new IntentFilterCondition(context);
        }
    }

    public IntentFilter callIntentFilter() {

        intentFilter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        intentFilter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        return intentFilter;
    }
}
