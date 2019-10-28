package com.travel.cab.service.activity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.travel.cab.service.BuildConfig;
import com.travel.cab.service.JsonParsing;
import com.travel.cab.service.R;
import com.travel.cab.service.broadcast.InternetBroadcastReceiver;
import com.travel.cab.service.modal.AppliedCouponModel;
import com.travel.cab.service.network.ApiConstant;
import com.travel.cab.service.ui.IntentFilterCondition;
import com.travel.cab.service.utils.preference.SharedPreference;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class DefaultPackageDetail extends AppCompatActivity implements View.OnClickListener
        , AdapterView.OnItemSelectedListener, PaytmPaymentTransactionCallback {
    private static final String TAG = "DefaultPackageDetail";
    private String pickupAddress, dropAddress, distanceBetweenLoc, rideAmount = "", startDate, appliedCoupon, goingTime, comingTime;
    private TextView tvPickupAddress, tvDropAddress, tvDistanceBetweenLoc, tvRideFare, tvServiceDays, tvVehicleType,
            tvStartDate, tvGoingTime, tvComingTime, tvServiceType, tvCoupon;
    private Calendar myCalendar;
    private DatePickerDialog.OnDateSetListener date;
    private TimePickerDialog mTimePicker;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference, mDatabaseReferenceForCoupon;
    private Button btnSubmit;
    private InternetBroadcastReceiver internetBroadcastReceiver;
    private IntentFilter intentFilter;
    private String countryCode = "91";
    // for paytm transaction
    private String mid = BuildConfig.MerchantId;
    private String orderId;
    private String customerId;
    private String mobile_number = "7042226632";
    private ApiConstant apiConstant;
    private Spinner daysDropDown, serviceTypeDropDown, vehicleDropDown;
    private String[] days = {"select service days", "1day", "2days", "3days", "4days", "5days"};
    private String[] serviceType = {"select service type", "one sided", "both sided"};
    private String[] vehicleType = {"select vehicle type", "Bike", "Car"};
    private int numOfDays, typeOfServiceAtPosition, vehicleTypePosition;
    private LinearLayout llMain;
    private String pushKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_package_detail);
        inItId();
        getValueFromIntent();
        setListener();
        setGettedValue();
        setUpToStoreValueInDb();
    }

    private void inItId() {
        tvPickupAddress = findViewById(R.id.tv_pickup_point);
        tvDropAddress = findViewById(R.id.tv_drop_point);
        tvDistanceBetweenLoc = findViewById(R.id.tv_location_distance);
        tvRideFare = findViewById(R.id.tv_service_fare);
        tvStartDate = findViewById(R.id.tv_date);
        tvGoingTime = findViewById(R.id.tv_going_timing);
        tvComingTime = findViewById(R.id.tv_coming_timing);
        tvCoupon = findViewById(R.id.tv_coupon_default);
        daysDropDown = findViewById(R.id.spinner_select_service_days_default);
        vehicleDropDown = findViewById(R.id.spinner_select_vehicle_type_default);
        serviceTypeDropDown = findViewById(R.id.spinner_select_service_type_default);
        llMain = findViewById(R.id.ll_mainLayout);
        btnSubmit = findViewById(R.id.btn_submit);
        myCalendar = Calendar.getInstance();
        internetBroadcastReceiver = new InternetBroadcastReceiver();
        apiConstant = new ApiConstant();

    }

    private void getValueFromIntent() {

        Intent intent = getIntent();
        HashMap<String, String> hashMap = (HashMap<String, String>) intent.getSerializableExtra("packageInfoMap");
        if (hashMap != null) {
            pickupAddress = hashMap.get("fromLocation");
            dropAddress = hashMap.get("toLocation");
            distanceBetweenLoc = hashMap.get("distanceDiff");
        }

        // Log.v("HashMapTest", hashMap.get("toLoc"));

    }

    private void setListener() {
        tvStartDate.setOnClickListener(this);
        tvGoingTime.setOnClickListener(this);
        tvComingTime.setOnClickListener(this);
        tvCoupon.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        tvRideFare.setOnClickListener(this);
        daysDropDown.setOnItemSelectedListener(this);
        serviceTypeDropDown.setOnItemSelectedListener(this);
        vehicleDropDown.setOnItemSelectedListener(this);
        // spinner for service required days
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, days);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        daysDropDown.setAdapter(aa);
        // spinner for service type it may be of one sided or both sided
        ArrayAdapter arayAdapterForServiceType = new ArrayAdapter(this, android.R.layout.simple_spinner_item, serviceType);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        serviceTypeDropDown.setAdapter(arayAdapterForServiceType);
        // spinner for vehicle type
        ArrayAdapter arayAdapterForVehicleType = new ArrayAdapter(this, android.R.layout.simple_spinner_item, vehicleType);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        vehicleDropDown.setAdapter(arayAdapterForVehicleType);
        //initialize the date object first and set it with blank value
        setDateSelected();
    }

    private void setGettedValue() {
        tvPickupAddress.setText(pickupAddress);
        tvDropAddress.setText(dropAddress);
        tvDistanceBetweenLoc.setText(distanceBetweenLoc);
//        tvRideFare.setText(rideFare + "" + R.string.rupees);
       /* tvVehicleType.setText(vehicleType);
        tvServiceType.setText(serviceType);*/
//        btnSubmit.setText(getString(R.string.proceed_to_pay) + "" + Float.parseFloat(rideFare));
    }

    @Override
    protected void onStart() {
        super.onStart();
        intentFilter = IntentFilterCondition.getInstance().callIntentFilter();
        registerReceiver(internetBroadcastReceiver, intentFilter);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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
            case R.id.tv_service_fare:
                checkValueExistForFareCheck();
                break;
            case R.id.tv_coupon_default:
                dialogForCouponApply();
                break;
            default:
                break;
        }

    }

    private void dialogForCouponApply() {
        EditText etApplyCode;
        Button btnCancel, btnApply;
        AlertDialog show;
        View alertLayout = LayoutInflater.from(this).inflate(R.layout.custom_dialog_for_apply_coupon, null);
        final AlertDialog.Builder mAlertBuilder = new AlertDialog.Builder(this);
        etApplyCode = alertLayout.findViewById(R.id.et_coupon_code);
        btnCancel = alertLayout.findViewById(R.id.btn_cancel);
        btnApply = alertLayout.findViewById(R.id.btn_apply);
        mAlertBuilder.setView(alertLayout);
        //progressBar.setVisibility(View.GONE);
        show = mAlertBuilder.show();
        show.setCancelable(false);
        Window window = show.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);

        btnApply.setOnClickListener(view -> {
            appliedCoupon = etApplyCode.getText().toString();
            if (!rideAmount.equals("") && rideAmount != null) {
                if (appliedCoupon != null && !appliedCoupon.equals("")) {
                    if (appliedCoupon.equals("FIRST50") || appliedCoupon.equals("FIRST10")) {
                        // getting all values from the database
                        List<AppliedCouponModel> couponList = new ArrayList<>();
                        mDatabase.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild("appliedCoupon")) {
                                    mDatabaseReferenceForCoupon.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Log.i(TAG, "onDataChange: " + dataSnapshot);
                                            for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                                AppliedCouponModel appliedCouponModel = ds.getValue(AppliedCouponModel.class);
                                                appliedCouponModel.setCoupon_code(appliedCouponModel.getCoupon_code());
                                                int coupon_count = Integer.parseInt(appliedCouponModel.getCoupon_count()) + 1;
                                                appliedCouponModel.setCoupon_count(String.valueOf(coupon_count));
                                                appliedCouponModel.setPush_key_coupon(appliedCouponModel.getPush_key_coupon());
                                                couponList.add(appliedCouponModel);
                                            }
                                            updateCouponCount(couponList);
                                            show.dismiss();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Log.i(TAG, "onCancelled: ");
                                        }
                                    });
                                } else {
                                    String pushKeyForCoupon = mDatabaseReferenceForCoupon.push().getKey();
                                    Map<String, String> couponMap = new HashMap<>();
                                    couponMap.put("coupon_code", appliedCoupon);
                                    couponMap.put("coupon_count", "1");
                                    couponMap.put("push_key_coupon", pushKeyForCoupon);
                                    //default way is given below
                                    //mDatabaseReferenceForCoupon.push().setValue(couponMap)
                                    //pushkey way is given below
                                    mDatabaseReferenceForCoupon.child(pushKeyForCoupon).setValue(couponMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            float getRideAmount = Float.parseFloat(rideAmount);
                                            float finalAmount = getRideAmount - (getRideAmount * 10) / 100;
                                            tvRideFare.setText(String.valueOf(finalAmount));
                                            btnSubmit.setText("Proceed to pay" + "" + String.valueOf(finalAmount) + "Rupees");
                                            rideAmount = String.valueOf(finalAmount);
                                            tvCoupon.setText(appliedCoupon);
                                            tvCoupon.setClickable(false);
                                            show.dismiss();
                                            Toast.makeText(DefaultPackageDetail.this, "Coupon Applied Successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                 /*       mDatabaseReferenceForCoupon.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Log.i(TAG, "onDataChange: " + dataSnapshot);
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                    AppliedCouponModel appliedCouponModel = ds.getValue(AppliedCouponModel.class);
                                    appliedCouponModel.setCoupon_code(appliedCouponModel.getCoupon_code());
                                    int coupon_count = Integer.parseInt(appliedCouponModel.getCoupon_count()) + 1;
                                    appliedCouponModel.setCoupon_count(String.valueOf(coupon_count));
                                    appliedCouponModel.setPush_key_coupon(appliedCouponModel.getPush_key_coupon());
                                    couponList.add(appliedCouponModel);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.i(TAG, "onCancelled: ");
                            }
                        });*/
                   /*     if (couponList.size() > 0) {
                            if (Integer.parseInt(couponList.get(0).getCoupon_count()) <= 3) {
                                mDatabase.getReference().child("appliedCoupon").
                                        child(SharedPreference.getInstance().getUserId()).
                                        child(couponList.get(0).getPush_key_coupon()).child("coupon_count").setValue(couponList.get(0).getCoupon_count());
                            } else {
                                Toast.makeText(this, "Coupon Limit exceed", Toast.LENGTH_SHORT).show();
                            }

                        }*/ /*else {
                            String pushKeyForCoupon = mDatabaseReferenceForCoupon.push().getKey();
                            Map<String, String> couponMap = new HashMap<>();
                            couponMap.put("coupon_code", appliedCoupon);
                            couponMap.put("coupon_count", "1");
                            couponMap.put("push_key_coupon", pushKeyForCoupon);
                            //default way is given below
                            //mDatabaseReferenceForCoupon.push().setValue(couponMap)
                            //pushkey way is given below
                            mDatabaseReferenceForCoupon.child(pushKeyForCoupon).setValue(couponMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    show.dismiss();
                                    Toast.makeText(DefaultPackageDetail.this, "Coupon Applied Successfully", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                        }*/
                    } else {
                        Toast.makeText(this, "Wrong Coupon code", Toast.LENGTH_SHORT).show();
                    }


                } else {

                    Toast.makeText(this, getString(R.string.enter_coupon), Toast.LENGTH_SHORT).show();
                }
            } else {
                show.dismiss();
                Toast.makeText(this, "Select fare first", Toast.LENGTH_SHORT).show();
            }

        });
        btnCancel.setOnClickListener(view -> show.dismiss());
    }

    private void updateCouponCount(List<AppliedCouponModel> couponList) {
        if (Integer.parseInt(couponList.get(0).getCoupon_count()) <= 3) {
            mDatabase.getReference().child("appliedCoupon").
                    child(SharedPreference.getInstance().getUserId()).
                    child(couponList.get(0).getPush_key_coupon()).child("coupon_count").setValue(couponList.get(0).getCoupon_count());
            float getRideAmount = Float.parseFloat(rideAmount);
            float finalAmount = getRideAmount - (getRideAmount * 10) / 100;
            tvRideFare.setText(String.valueOf(finalAmount));
            btnSubmit.setText("Proceed to pay" + "" + String.valueOf(finalAmount) + "Rupees");
            rideAmount = String.valueOf(finalAmount);
            tvCoupon.setText(appliedCoupon);
            tvCoupon.setClickable(false);
        } else {
            Toast.makeText(this, "Coupon Limit exceed", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkValueExistForFareCheck() {
        if (pickupAddress != null
                && dropAddress != null
                && distanceBetweenLoc != null) {
            if (vehicleTypePosition > 0) {
                if (typeOfServiceAtPosition > 0) {
                    if (numOfDays > 0) {
                        float rideFare = calculateFare(distanceBetweenLoc, numOfDays, typeOfServiceAtPosition, vehicleTypePosition);
                        tvRideFare.setText(String.valueOf(rideFare));
                        rideAmount = String.valueOf(rideFare);
                        btnSubmit.setText("Proceed to pay" + "" + rideFare + "Rupees");
                    } else {
                        Toast.makeText(this, getString(R.string.select_days), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.select_service_type), Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, getString(R.string.select_vehicle_type), Toast.LENGTH_SHORT).show();
            }


        }
    }

    // for calculating the fare for both bike and car
    private float calculateFare(String packageDistance, int numOfDays, int serviceType, int vehicleType) {
        String separatedUnit[] = packageDistance.split(" ");
        if (packageDistance.contains("km")) {
            /*---------- when this scenario will come like (1,900)-----------not work------------*/
            if (separatedUnit.length <= 3) {
                float distance = Float.parseFloat(separatedUnit[1]);
                if (vehicleType == 1) {
                    // condition for one sided or both sided
                    if (serviceType == 1) {
                        return (distance * numOfDays) * 4;
                    } else {
                        return (distance * numOfDays) * 4 * 2;
                    }
                } else {
                    // condition for one sided or both sided
                    if (serviceType == 1) {
                        return (distance * numOfDays) * 7;
                    } else {
                        return (distance * numOfDays) * 7 * 2;
                    }
                }


            } else {

                Snackbar snackbar = Snackbar
                        .make(llMain, "Service not available", Snackbar.LENGTH_LONG);
                snackbar.show();

                return 0;
            }

        } else if (packageDistance.equals("m")) {
            return 10;
        } else {
            return 0;
        }

    }

    private void setUpToStoreValueInDb() {
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference().child("applyForService").child(SharedPreference.getInstance().getUserId());
        mDatabaseReferenceForCoupon = mDatabase.getReference().child("appliedCoupon").child(SharedPreference.getInstance().getUserId());


    }

    private void storeServiceRequiredDetail() {
        if (!tvRideFare.getText().toString().equals("")) {
            if (!tvStartDate.getText().toString().equals("")) {
                if (!tvGoingTime.getText().toString().equals("")) {
                    if (!tvComingTime.getText().toString().equals("")) {

                        Map<String, String> serviceDetailMap = new HashMap<>();
                        serviceDetailMap.put("pickup_location", pickupAddress);
                        serviceDetailMap.put("drop_location", dropAddress);
                        serviceDetailMap.put("distance_home_office", distanceBetweenLoc);
                        serviceDetailMap.put("service_days", days[numOfDays]);
                        serviceDetailMap.put("service_fare", rideAmount);
                        serviceDetailMap.put("service_starting_date", startDate);
                        serviceDetailMap.put("service_type", serviceType[typeOfServiceAtPosition]);
                        serviceDetailMap.put("vehicle_type", vehicleType[vehicleTypePosition]);
                        serviceDetailMap.put("going_time", tvGoingTime.getText().toString());
                        serviceDetailMap.put("coming_time", tvComingTime.getText().toString());
                        serviceDetailMap.put("order_number", "");
                        serviceDetailMap.put("service_created_at_time", Calendar.getInstance().getTime().toString());
                        // get push key
                        pushKey = mDatabaseReference.push().getKey();
                        Log.i(TAG, "storeServiceRequiredDetail: " + pushKey);
                        // default
                        //mDatabaseReference.push().setValue(serviceDetailMap)
                        mDatabaseReference.child(pushKey).setValue(serviceDetailMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                generateOrderNumber(countryCode, startDate);

                            }

                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(DefaultPackageDetail.this, "" + e, Toast.LENGTH_SHORT).show();
                                Log.i(TAG, "onSuccess: " + e);
                            }
                        });

                    } else {
                        Toast.makeText(this, getString(R.string.coming_time), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.going_time), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getString(R.string.select_date_field), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.select_fare), Toast.LENGTH_SHORT).show();

        }
    }


    private void generateOrderNumber(String countryCode, String startDate) {
        int sixDigitRandomNumber = getSixDigitRandomNumber();
        String splitedDate = splitDate(startDate);
        String orderNumber = countryCode + splitedDate + String.valueOf(sixDigitRandomNumber);
        sendOrderNumberAndCustomerId(orderNumber, SharedPreference.getInstance().getUserId());
    }

    private void sendOrderNumberAndCustomerId(String orderNumber, String customerId) {
        Toast.makeText(DefaultPackageDetail.this, "Data Save Successfully", Toast.LENGTH_SHORT).show();
        startTransaction(orderNumber, customerId);
      /*  Intent intent = new Intent(PackageDetailActivity.this,PaymentTransactionActivity.class);
        intent.putExtra("orderNumber",orderNumber);
        intent.putExtra("customerId",customerId);
        startActivity(intent);
        Log.i(TAG, "onSuccess: ");*/
    }

    private void startTransaction(String orderNumber, String cstId) {
        orderId = orderNumber;
        customerId = cstId;
        sendUserDetailTOServer dl = new sendUserDetailTOServer();
        dl.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private String splitDate(String startDate) {
        String[] date = startDate.split("-");
        return date[1] + date[0];
    }

    private int getSixDigitRandomNumber() {

        // create instance of Random class
        Random rand = new Random();
        // Generate random integers in range 0 to 999
        int rand_six_digit = rand.nextInt(100000);
        return rand_six_digit;
    }

    private void getDateFromUser() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(DefaultPackageDetail.this, date, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void getGoingTime() {


        // TODO Auto-generated method stub

        int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = myCalendar.get(Calendar.MINUTE);
        mTimePicker = new TimePickerDialog(DefaultPackageDetail.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                if (selectedHour >= 12) {
                    tvGoingTime.setText(selectedHour + ":" + selectedMinute + "PM" + "(going)");
                } else {
                    tvGoingTime.setText(selectedHour + ":" + selectedMinute + "AM" + "(going)");

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
        mTimePicker = new TimePickerDialog(DefaultPackageDetail.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                if (selectedHour >= 12) {
                    tvComingTime.setText(selectedHour + ":" + selectedMinute + "PM" + "(coming)");
                } else {
                    tvComingTime.setText(selectedHour + ":" + selectedMinute + "AM" + "(coming)");

                }

            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();

    }

    @Override
    public void onTransactionResponse(Bundle inResponse) {

        String ORDERID = inResponse.getString("ORDERID");
        String TXNAMOUNT = inResponse.getString("TXNAMOUNT");
        String RESPCODE = inResponse.getString("RESPCODE");
        String STATUS = inResponse.getString("STATUS");
        if (STATUS.equals("TXN_SUCCESS")) {
            mDatabase.getReference().child("applyForService").
                    child(SharedPreference.getInstance().getUserId()).
                    child(pushKey).child("order_number").setValue(orderId);
            generateDialogForTransactionStatus(ORDERID, TXNAMOUNT, RESPCODE);
        } else {
            generateDialogForTransactionStatus(ORDERID, TXNAMOUNT, RESPCODE);

        }


    }

    private void generateDialogForTransactionStatus(String orderid, String txnamount, String respcode) {
        TextView tvTransactionStatus, tvTransactionFare, tvTransactionNumber;
        Button btnCancel;
        AlertDialog show;
        View alertLayout = LayoutInflater.from(this).inflate(R.layout.custom_dialog_transaction_status, null);
        final AlertDialog.Builder mAlertBuilder = new AlertDialog.Builder(this);
        tvTransactionFare = alertLayout.findViewById(R.id.tv_transaction_fare);
        tvTransactionStatus = alertLayout.findViewById(R.id.tv_transaction_status);
        tvTransactionNumber = alertLayout.findViewById(R.id.tv_transaction_number);
        btnCancel = alertLayout.findViewById(R.id.btn_cancel);
        mAlertBuilder.setView(alertLayout);
        tvTransactionStatus.setText(respcode);
        tvTransactionFare.setText(txnamount);
        tvTransactionNumber.setText(orderid);
        //progressBar.setVisibility(View.GONE);
        show = mAlertBuilder.show();
        show.setCancelable(false);
        btnCancel.setOnClickListener(view -> show.dismiss());
    }

    @Override
    public void networkNotAvailable() {

    }

    @Override
    public void clientAuthenticationFailed(String inErrorMessage) {

    }

    @Override
    public void someUIErrorOccurred(String inErrorMessage) {

    }

    @Override
    public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {

    }

    @Override
    public void onBackPressedCancelTransaction() {

    }

    @Override
    public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
        Log.i(TAG, "onTransactionCancel: " + inResponse);
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
                startDate = sdf.format(myCalendar.getTime());
                tvStartDate.setText(sdf.format(myCalendar.getTime()));


            }

        };
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        if (adapterView.getAdapter().getItem(0).toString().equals("select service type")) {
            typeOfServiceAtPosition = position;
        } else if (adapterView.getAdapter().getItem(0).toString().equals("select vehicle type")) {
            vehicleTypePosition = position;
        } else {
            numOfDays = position;
            // Toast.makeText(getActivity(), days[position], Toast.LENGTH_LONG).show();
        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(internetBroadcastReceiver);
    }

    // async class for paytmTransaction
    public class sendUserDetailTOServer extends AsyncTask<ArrayList<String>, Void, String> {
        //private String orderId , mid, custid, amt;
        String url = apiConstant.url;
        String verifyUrl = apiConstant.varifyUrl;
        // "https://securegw-stage.paytm.in/theia/paytmCallback?ORDER_ID"+orderId;
        String CHECKSUMHASH = "";
        private ProgressDialog dialog = new ProgressDialog(DefaultPackageDetail.this);

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Please wait");
            this.dialog.show();
        }

        protected String doInBackground(ArrayList<String>... alldata) {
            JsonParsing jsonParser = new JsonParsing(DefaultPackageDetail.this);
            String param =
                    "MID=" + mid +
                            "&ORDER_ID=" + orderId +
                            "&CUST_ID=" + customerId +
                            //for testing
                            //"&CHANNEL_ID=WAP&TXN_AMOUNT=1&WEBSITE=WEBSTAGING" +
                            // for production
                            "&CHANNEL_ID=WAP&TXN_AMOUNT=1&WEBSITE=DEFAULT" +
                            // "EMAIL="+ "honeykumar8826@gmail.com"+
                            "&MOBILE_NO=" + mobile_number +
                            "&CALLBACK_URL=" + verifyUrl + "&INDUSTRY_TYPE_ID=Retail";
            JSONObject jsonObject = jsonParser.makeHttpRequest(url, "POST", param);
            // yaha per checksum ke saht order id or status receive hoga..
            // Log.e("CheckSum result >>", jsonObject.toString());
            if (jsonObject != null) {
                Log.e("CheckSum result >>", jsonObject.toString());
                try {
                    CHECKSUMHASH = jsonObject.has("CHECKSUMHASH") ? jsonObject.getString("CHECKSUMHASH") : "";
                    Log.e("CheckSum result >>", CHECKSUMHASH);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return CHECKSUMHASH;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e(" setup acc ", "  signup result  " + result);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            // PaytmPGService Service = PaytmPGService.getStagingService();
            // when app is ready to publish use production service
            PaytmPGService Service = PaytmPGService.getProductionService();
            // now call paytm service here
            //below parameter map is required to construct PaytmOrder object, Merchant should replace below map values with his own values
            HashMap<String, String> paramMap = new HashMap<>();
            //these are mandatory parameters
            paramMap.put("MID", mid); //MID provided by paytm
            paramMap.put("ORDER_ID", orderId);
            paramMap.put("CUST_ID", customerId);
            paramMap.put("CHANNEL_ID", "WAP");
            paramMap.put("TXN_AMOUNT", "1");
            paramMap.put("WEBSITE", "DEFAULT");
            paramMap.put("CALLBACK_URL", verifyUrl);
            //paramMap.put( "EMAIL" , "honeykumar8826@gmail.com");   // no need
            paramMap.put("MOBILE_NO", mobile_number);  // no need
            paramMap.put("CHECKSUMHASH", CHECKSUMHASH);
            //paramMap.put("PAYMENT_TYPE_ID" ,"CC");    // no need
            paramMap.put("INDUSTRY_TYPE_ID", "Retail");
            PaytmOrder Order = new PaytmOrder(paramMap);
            Log.e("checksum ", "param " + paramMap.toString());
            Service.initialize(Order, null);
            // start payment service call here
            Service.startPaymentTransaction(DefaultPackageDetail.this, true, true,
                    DefaultPackageDetail.this);
            Log.i(TAG, "onPostExecute: ");
        }
    }

}

