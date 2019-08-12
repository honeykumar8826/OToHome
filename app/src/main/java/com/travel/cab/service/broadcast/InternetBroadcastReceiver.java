package com.travel.cab.service.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

public class InternetBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: " + intent);
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            checkConnectivity(context);
        }
    /*    if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            Log.i(TAG, "onReceive: " + networkInfo.isConnected());
            if (networkInfo.isConnected()) {
                // Wifi is connected
                Toast.makeText(context, "Wifi is connected", Toast.LENGTH_SHORT).show();

                //  Log.d("Identify", "Wifi is connected: " + String.valueOf(networkInfo));
            } else {
                Toast.makeText(context, "Wifi is disconnected", Toast.LENGTH_SHORT).show();
            }
        }*/
/*        if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())) {
            Toast.makeText(context, "Power connected", Toast.LENGTH_SHORT).show();
        }
        if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())) {
            Toast.makeText(context, "Power disconnected", Toast.LENGTH_SHORT).show();
        }*/
    }

    private void checkConnectivity(final Context context) {
        if (!isNetworkInterfaceAvailable(context)) {
            Toast.makeText(context, "You are OFFLINE!", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    //This only checks if the network interface is available, doesn't guarantee a particular network service is available, for example, there could be low signal or server downtime
    private boolean isNetworkInterfaceAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
