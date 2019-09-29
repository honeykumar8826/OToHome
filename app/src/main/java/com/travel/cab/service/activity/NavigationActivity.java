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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.travel.cab.service.R;
import com.travel.cab.service.database.MyAppDatabase;
import com.travel.cab.service.database.User;
import com.travel.cab.service.fragment.FareCalculatorFragment;
import com.travel.cab.service.fragment.MyPlanFragment;
import com.travel.cab.service.fragment.ProfileFragment;
import com.travel.cab.service.fragment.ShowPackageFragment;
import com.travel.cab.service.fragment.VIewProfileFragment;
import com.travel.cab.service.interfaces.CheckPosition;
import com.travel.cab.service.utils.preference.SharedPreference;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,CheckPosition {
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private Fragment fragment;
    private FirebaseAuth mAuth;
    private BottomNavigationView navigation;
    private static final String TAG = "NavigationActivity";
    private MyAppDatabase myAppDatabase;
    private ImageView imgProfile;
    private TextView tvProfileNumber, tvProfileName;
    private final String[] permissionList = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        setInItId();
        mainScreenLoad();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        checkPermission();


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    private void mainScreenLoad() {
        displaySelectedScreen(R.id.navigation_home);
    }

    private void setInItId() {
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigation = findViewById(R.id.navigation);
        View headerView = navigationView.getHeaderView(0);
        tvProfileName = headerView.findViewById(R.id.tv_nav_profile_name);
        tvProfileNumber = headerView.findViewById(R.id.tv_nav_profile_mobile);
        imgProfile = headerView.findViewById(R.id.nav_img_profile);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mAuth = FirebaseAuth.getInstance();
        myAppDatabase = Room.databaseBuilder(this, MyAppDatabase.class, "userdb").allowMainThreadQueries().build();
        getDataFromDatabase();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logout:
                mAuth.signOut();
                Intent loginIntent = new Intent(this, PhoneLogin.class);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
//        int id = item.getItemId();

        /* if (id == R.id.nav_camera) {
         */
        switch (item.getItemId()) {
            case R.id.navigation_home:
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                setBottomNavigationVisibility(1);
                displaySelectedScreen(R.id.navigation_home);
                navigation.getMenu().findItem(R.id.navigation_home).setChecked(true);
                return true;
            case R.id.navigation_fare_calci:
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                setBottomNavigationVisibility(1);
                displaySelectedScreen(R.id.navigation_fare_calci);
                navigation.getMenu().findItem(R.id.navigation_fare_calci).setChecked(true);
                //  setupFareCalculatorFragment();
                return true;
            case R.id.navigation_profile:
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                setBottomNavigationVisibility(1);
                displaySelectedScreen(R.id.navigation_profile);
                navigation.getMenu().findItem(R.id.navigation_profile).setChecked(true);
                // setupHomeFragment();
                return true;
                case R.id.navigation_my_plan:
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                    // set the visibility of the bottom navigation view
                    setBottomNavigationVisibility(0);
                displaySelectedScreen(R.id.navigation_my_plan);

                // setupHomeFragment();
                return true;
        }
        return true;
    }

    private void setBottomNavigationVisibility(int status) {
        if(status==0)
        {
            navigation.setVisibility(View.GONE);
        }
        else
        {
            navigation.setVisibility(View.VISIBLE);
        }
    }

    private void displaySelectedScreen(int itemViewId) {
        switch (itemViewId) {
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
                case R.id.navigation_my_plan:
                    toolbar.setTitle(R.string.my_plans);
                fragment = new MyPlanFragment();

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

    private void clearPreference() {
        SharedPreference.getInstance().clearPreference();
    }

    public void checkPermission() {
//        int count = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(this, permissionList)) {
//            Log.i(TAG, "checkPermission: " + count++);
                ActivityCompat.requestPermissions(this, permissionList, 10);
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

                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
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
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
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
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
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

    @Override
    public void setClickedPosition(String itemPosition) {
        if (itemPosition != null) {
            navigation.getMenu().findItem(R.id.navigation_profile).setChecked(true);
        }
    }

    private void getDataFromDatabase() {
        // getting all values from the database
        List<User> users = myAppDatabase.myDao().getUser();
        String name = null, mobile = null, email = null, address = null, company = null, image = null;
        if(users !=null)
        {
            for (User user : users) {
                name = user.getUserName();
                mobile = user.getMobileNumber();
                email = user.getUserEmail();
                image = user.getUserImage();
            }
        }



        //tvName.setText(userProfileDetail.getName());
        if (image != null && name != null && mobile != null && email != null ) {
            Glide.with(this)
                    .load(image)
                    .centerCrop()
                    .apply(RequestOptions.circleCropTransform())
                    .into(imgProfile);
            //tvName.setText("Name            :   "+userProfileDetail.getName());
            tvProfileName.setText(name);
            tvProfileNumber.setText(mobile);

        }

    }
}
