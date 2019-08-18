package com.travel.cab.service.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.travel.cab.service.R;
import com.travel.cab.service.fragment.FareCalculatorFragment;
import com.travel.cab.service.fragment.ProfileFragment;
import com.travel.cab.service.fragment.ShowPackageFragment;
import com.travel.cab.service.fragment.VIewProfileFragment;
import com.travel.cab.service.utils.baseactivity.BaseActivity;
import com.travel.cab.service.utils.preference.SharedPreference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private final String[] permissionList = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private FragmentManager fragmentManager;
    private TextView mTextMessage;
    private Fragment fragment;
    private FirebaseAuth mAuth;
    private Toolbar toolbar;
    BaseActivity baseActivity = new BaseActivity();
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    displaySelectedScreen(R.id.navigation_home);
                    return true;
                case R.id.navigation_fare_calci:
                    displaySelectedScreen(R.id.navigation_fare_calci);
                  //  setupFareCalculatorFragment();
                    return true;
                case R.id.navigation_profile:
                    displaySelectedScreen(R.id.navigation_profile);
                   // setupHomeFragment();
                    return true;
            }
            return false;
        }
    };

    private void setupFareCalculatorFragment() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.framLayout_container, new FareCalculatorFragment()).commit();
    }

    private void setupHomeFragment() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.framLayout_container, new ProfileFragment()).commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        inItId();
        //baseActivity.checkPermission(HomeActivity.this);
        checkPermission();
        displaySelectedScreen(R.id.navigation_home);

    }

    private void displaySelectedScreen(int itemViewId) {
        switch (itemViewId)
        {
            case R.id.navigation_home:
                fragment = new ShowPackageFragment();
                toolbar.setTitle("Home");
                break;
            case R.id.navigation_fare_calci:
                fragment = new FareCalculatorFragment();
                toolbar.setTitle("Fare Calculator");
                break;
            case R.id.navigation_profile:
                fragment = new ProfileFragment();
                break;
            case R.id.view_profile:
                fragment = new VIewProfileFragment();
                break;

            default:
                break;
        }
        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.slide_up, 0);
            ft.replace(R.id.framLayout_container, fragment);
            ft.commit();
        }
    }
    private void inItId() {
        fragmentManager = getSupportFragmentManager();
        mTextMessage = findViewById(R.id.message);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mAuth = FirebaseAuth.getInstance();
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options, menu);
       return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logout:
                mAuth.signOut();
                Intent loginIntent = new Intent(this,PhoneLogin.class);
                startActivity(loginIntent);
                clearPreference();
                finish();
                return true;
            case R.id.view_profile:
                toolbar.setTitle(" Profile");
                displaySelectedScreen(R.id.view_profile);
                return true;

            default:
                Toast.makeText(this, "Do Right Selection", Toast.LENGTH_SHORT).show();
                return true;
        }
    }


    private void clearPreference() {
        SharedPreference.getInstance().clearPreference();
    }
    public void checkPermission() {
//        int count = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(HomeActivity.this, permissionList)) {
//            Log.i(TAG, "checkPermission: " + count++);
                ActivityCompat.requestPermissions(HomeActivity.this, permissionList, 10);
            } else {
                Toast.makeText(this, " Permission  granted ", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "permission automatically granted", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean hasPermissions(Context context, String... permissions) {
        int count = 0;
        if (context != null && permissions != null) {
            Log.i(TAG, "hasPermissions: " + permissions.length);
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    count++;
                    Log.i(TAG, "hasPermissions: " + count);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 10) {
            Log.i(TAG, "onRequestPermissionsResult: " + permissions);
            if (grantResults[0] == -1) {

                if (!ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Log.i(TAG, "shouldShowRequestPermissionRationale:");
                    showMessageOkCancel("Location permission is required to access location",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                    // Toast.makeText(this, "shouldShowRequestPermissionRationale", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Location Permission not granted", Toast.LENGTH_SHORT).show();
                }
            } else if (grantResults[1] == -1) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Log.i(TAG, "shouldShowRequestPermissionRationale:");
                    showMessageOkCancel("Storage permission is required to access contact",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                    // Toast.makeText(this, "shouldShowRequestPermissionRationale", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Storage Permission not granted", Toast.LENGTH_SHORT).show();
                }
            } else if (grantResults[2] == -1) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.CAMERA)) {
                    Log.i(TAG, "shouldShowRequestPermissionRationale:");
                    showMessageOkCancel("Camera permission is required to access camera",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                    //  Toast.makeText(this, "shouldShowRequestPermissionRationale", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Camera Permission not granted ", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, " Permissions  granted ", Toast.LENGTH_SHORT).show();

            }
      /*      for (String permissionForFun : permissions) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    boolean showRationale = shouldShowRequestPermissionRationale(permissionForFun);
                    if (!showRationale) {
                        Toast.makeText(this, "inside shown rationale", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "inside the toast", Toast.LENGTH_SHORT).show();
                    }
                }
            }*/

        } else {
            Toast.makeText(this, "Permission granted with Code=" + grantResults[0], Toast.LENGTH_SHORT).show();
        }
    }

    private void showMessageOkCancel(String permissionDetail, DialogInterface.OnClickListener onClickListener) {

        new AlertDialog.Builder(this).setMessage(permissionDetail)
                .setPositiveButton("Ok", onClickListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}

