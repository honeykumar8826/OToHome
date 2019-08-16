package com.travel.cab.service.utils.preference;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPreference {
    private static final String TAG = "SharedPreference";
    private static final String IS_FIRST_TIME_LAUNCH="IS_FIRST_TIME_LAUNCH" ;
    private static final String IS_FIRST_TIME_LAUNCH_FOR_PHONE="IS_FIRST_TIME_LAUNCH_FOR_PHONE" ;
    private static final String IS_PERMISSION_GRANT="IS_PERMISSION_GRANT" ;
    private static final String USER_ID="USER_ID" ;
    private static SharedPreference instance;
    private SharedPreferences sharedPreferences,sharedPreferenceIntroSlider;
    private SharedPreferences.Editor editor;

    private SharedPreference(Context context) {

        sharedPreferences = context.getSharedPreferences("Login", Context.MODE_PRIVATE);
        sharedPreferenceIntroSlider = context.getSharedPreferences("IntroSlider", Context.MODE_PRIVATE);
    }

    public static void initShared(Context context) {
        if (instance == null) {
            instance = new SharedPreference(context);
        }
    }

    public static SharedPreference getInstance() {
        return instance;
    }

    public Boolean isFirstTime() {
       Boolean result = sharedPreferenceIntroSlider.getBoolean(IS_FIRST_TIME_LAUNCH,true);
        return  result;
            }


    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor = sharedPreferenceIntroSlider.edit();
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.apply();
    }
    public Boolean isFirstTimeForPhone() {
        Boolean result = sharedPreferences.getBoolean(IS_FIRST_TIME_LAUNCH_FOR_PHONE,true);
        return  result;
    }

    public void setFirstTimeForPhone(boolean isFirstTimeForPhone) {
        editor = sharedPreferences.edit();
        editor.putBoolean(IS_FIRST_TIME_LAUNCH_FOR_PHONE, isFirstTimeForPhone);
        editor.apply();
    }

    public String getUserId()
    {
        String userId = sharedPreferences.getString(USER_ID,"");
        return userId;
    }
    public void setUserId(String userId) {
        editor = sharedPreferences.edit();
        editor.putString(USER_ID, userId);
        editor.apply();
    }
    public String getGrantPermission()
    {
        String permissionNum = sharedPreferences.getString(IS_PERMISSION_GRANT,"1");
        return permissionNum;
    }
    public void setGrantPermission(String permissionCount) {
        editor = sharedPreferences.edit();
        editor.putString(IS_PERMISSION_GRANT, permissionCount);
        editor.apply();
    }
    public void clearPreference()
    {
        editor =sharedPreferences.edit();
        editor.clear().apply();
    }
}
