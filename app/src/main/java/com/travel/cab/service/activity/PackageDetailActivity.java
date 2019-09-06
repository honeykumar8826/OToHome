package com.travel.cab.service.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.travel.cab.service.R;

import java.util.HashMap;

public class PackageDetailActivity extends AppCompatActivity {
    private static final String TAG = "PackageDetailActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_detail);
        getValueFromIntent();
    }

    private void getValueFromIntent() {

        Intent intent = getIntent();
        HashMap<String, String> hashMap = (HashMap<String, String>)intent.getSerializableExtra("packageInfoMap");
        Log.v("HashMapTest", hashMap.get("toLoc"));
    }


}
