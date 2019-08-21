package com.travel.cab.service.utils.validation;

import android.content.Context;


public class CustomCheck {
    private static CustomCheck instance;

    private CustomCheck(Context context) {
    }

    public static void initCustomCheck(Context context) {
        if (instance == null) {
            instance = new CustomCheck(context);
        }
    }

    public static CustomCheck getInstance() {
        return instance;
    }

    public boolean checkPhoneNumber(String number)
    {
        return !number.isEmpty() && number.length() == 10;
    }
    public boolean checkNormalStringCase(String text)
    {
        return !text.isEmpty() && text.length() >= 6;
    }
}
