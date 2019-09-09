package com.travel.cab.service.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.travel.cab.service.MainActivity;
import com.travel.cab.service.R;
import com.travel.cab.service.utils.preference.SharedPreference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class PackageDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "PackageDetailActivity";
    private String pickupAddress, dropAddress, distanceBetweenLoc, rideFare,serviceType ,startDate, goingTime, comingTime, numberOfDays;
    private TextView tvPickupAddress,tvDropAddress,tvDistanceBetweenLoc,tvRideFare,tvServiceDays
            ,tvStartDate,tvGoingTime,tvComingTime;
    private Calendar myCalendar;
    private  DatePickerDialog.OnDateSetListener date;
    private  TimePickerDialog mTimePicker;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private Button btnSubmit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_detail);
        getValueFromIntent();
        inItId();
        setListener();
        setGettedValue();
        setUpToStoreValueInDb();
    }

    private void setUpToStoreValueInDb() {
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference().child("applyForService").child(SharedPreference.getInstance().getUserId());

    }

    private void setListener() {
        tvStartDate.setOnClickListener(this);
        tvGoingTime.setOnClickListener(this);
        tvComingTime.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
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
        btnSubmit = findViewById(R.id.btn_submit);
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
            serviceType = hashMap.get("serviceType");
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
            case R.id.btn_submit:
                storeServiceRequiredDetail();
            break;
            default:
                break;
        }
    }

    private void storeServiceRequiredDetail() {
        if(!tvStartDate.getText().toString().equals(""))
        {
           if(!tvGoingTime.getText().toString().equals(""))
           {
               if(!tvComingTime.getText().toString().equals(""))
               {

                   Map<String, String> serviceDetailMap = new HashMap<>();
                   serviceDetailMap.put("pickup_location", pickupAddress);
                   serviceDetailMap.put("drop_location", dropAddress);
                   serviceDetailMap.put("distance_home_office", distanceBetweenLoc);
                   serviceDetailMap.put("service_days", numberOfDays);
                   serviceDetailMap.put("service_fare", rideFare);
                   serviceDetailMap.put("service_starting_date", startDate);
                   serviceDetailMap.put("service_type", serviceType);
                   serviceDetailMap.put("going_time", tvGoingTime.getText().toString());
                   serviceDetailMap.put("coming_time", tvComingTime.getText().toString());
                   serviceDetailMap.put("service_created_at_time", Calendar.getInstance().getTime().toString());
                   mDatabaseReference.push().setValue(serviceDetailMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                       @Override
                       public void onSuccess(Void aVoid) {
                           Toast.makeText(PackageDetailActivity.this, "Data Save Successfully", Toast.LENGTH_SHORT).show();
                           Intent intent = new Intent(PackageDetailActivity.this,MainActivity.class);
                           startActivity(intent);
                           Log.i(TAG, "onSuccess: ");
                       }

                   }).addOnFailureListener(new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception e) {
                           Toast.makeText(PackageDetailActivity.this, "" + e, Toast.LENGTH_SHORT).show();
                           Log.i(TAG, "onSuccess: " + e);
                       }
                   });

               }
               else
               {
                   Toast.makeText(this, getString(R.string.coming_time), Toast.LENGTH_SHORT).show();
               }
           }
           else
           {
               Toast.makeText(this, getString(R.string.going_time), Toast.LENGTH_SHORT).show();
           }
        }
        else
        {
            Toast.makeText(this, getString(R.string.select_date_field), Toast.LENGTH_SHORT).show();
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
