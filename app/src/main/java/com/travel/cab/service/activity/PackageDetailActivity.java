package com.travel.cab.service.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.travel.cab.service.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

public class PackageDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "PackageDetailActivity";
    private String pickupAddress, dropAddress, distanceBetweenLoc, rideFare, startDate, goingTime, comingTime, numberOfDays;
    private TextView tvPickupAddress,tvDropAddress,tvDistanceBetweenLoc,tvRideFare,tvServiceDays
            ,tvStartDate,tvGoingTime,tvComingTime;
    private Calendar myCalendar;
    private  DatePickerDialog.OnDateSetListener date;
    private  TimePickerDialog mTimePicker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_detail);
        getValueFromIntent();
        inItId();
        setListener();
        setGettedValue();
    }

    private void setListener() {
        tvStartDate.setOnClickListener(this);
        tvGoingTime.setOnClickListener(this);
        tvComingTime.setOnClickListener(this);
        //initialize the date object first and set it with blank value
        setDateSelected();
    }

    private void setGettedValue() {
        tvPickupAddress.setText(pickupAddress);
        tvDropAddress.setText(dropAddress);
        tvDistanceBetweenLoc.setText(distanceBetweenLoc);
        tvRideFare.setText(rideFare);
        tvServiceDays.setText(numberOfDays);
    }

    private void inItId() {
        tvPickupAddress = findViewById(R.id.tv_pickup_point);
        tvDropAddress = findViewById(R.id.tv_drop_point);
        tvDistanceBetweenLoc = findViewById(R.id.tv_location_distance);
        tvRideFare = findViewById(R.id.tv_service_fare);
        tvStartDate = findViewById(R.id.tv_date);
        tvGoingTime = findViewById(R.id.tv_going_timing);
        tvComingTime = findViewById(R.id.tv_coming_timing);
        tvServiceDays = findViewById(R.id.tv_service_days);
        myCalendar = Calendar.getInstance();
    }

    private void getValueFromIntent() {

        Intent intent = getIntent();
        HashMap<String, String> hashMap = (HashMap<String, String>) intent.getSerializableExtra("packageInfoMap");
        if (hashMap != null) {
            pickupAddress = hashMap.get("fromLocation");
            dropAddress = hashMap.get("toLocation");
            distanceBetweenLoc = hashMap.get("distanceDiff");
            rideFare = hashMap.get("fare");
            numberOfDays = hashMap.get("serviceDays");
        }

       // Log.v("HashMapTest", hashMap.get("toLoc"));

    }


    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.tv_date:
                getDateFromUser();
            break;
            case R.id.tv_going_timing:
                getGoingTime();
            break;
            case R.id.tv_coming_timing:
                getComingTime();
            break;
            default:
                break;
        }
    }

    private void getGoingTime() {


                // TODO Auto-generated method stub

                int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
                int minute = myCalendar.get(Calendar.MINUTE);
                mTimePicker = new TimePickerDialog(PackageDetailActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        if(selectedHour>=12)
                        {
                            tvGoingTime.setText( selectedHour + ":" + selectedMinute+"AM"+"(going)");
                        }
                        else
                        {
                            tvGoingTime.setText( selectedHour + ":" + selectedMinute+"PM"+"(going)");

                        }
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
            private void getComingTime() {


                // TODO Auto-generated method stub
                int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
                int minute = myCalendar.get(Calendar.MINUTE);
                mTimePicker = new TimePickerDialog(PackageDetailActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        if(selectedHour>=12)
                        {
                            tvComingTime.setText( selectedHour + ":" + selectedMinute+"AM"+"(coming)");
                        }
                        else
                        {
                            tvComingTime.setText( selectedHour + ":" + selectedMinute+"PM+(coming)");

                        }

                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }



    //initialize the date object first and set it with blank value
    private void setDateSelected() {
        date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                // and set on a textView
                String myFormat = "dd-MM-yyyy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                tvStartDate.setText(sdf.format(myCalendar.getTime()));



            }

        };
    }

    private void getDateFromUser() {
        DatePickerDialog datePickerDialog =  new DatePickerDialog(PackageDetailActivity.this, date, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }
}
