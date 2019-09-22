package com.travel.cab.service.activity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
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
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.travel.cab.service.BuildConfig;
import com.travel.cab.service.JsonParsing;
import com.travel.cab.service.MainActivity;
import com.travel.cab.service.R;
import com.travel.cab.service.activity.paymentGateway.PaymentTransactionActivity;
import com.travel.cab.service.broadcast.InternetBroadcastReceiver;
import com.travel.cab.service.network.ApiConstant;
import com.travel.cab.service.ui.IntentFilterCondition;
import com.travel.cab.service.utils.preference.SharedPreference;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class PackageDetailActivity extends AppCompatActivity implements View.OnClickListener, PaytmPaymentTransactionCallback {
    private static final String TAG = "PackageDetailActivity";
    private String pickupAddress, dropAddress, distanceBetweenLoc, rideFare,serviceType ,vehicleType,startDate, goingTime, comingTime, numberOfDays;
    private TextView tvPickupAddress,tvDropAddress,tvDistanceBetweenLoc,tvRideFare,tvServiceDays, tvVehicleType
            ,tvStartDate,tvGoingTime,tvComingTime,tvServiceType;
    private Calendar myCalendar;
    private  DatePickerDialog.OnDateSetListener date;
    private  TimePickerDialog mTimePicker;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_detail);
        inItId();
        getValueFromIntent();
        setListener();
        setGettedValue();
        setUpToStoreValueInDb();
    }
    @Override
    protected void onStart() {
        super.onStart();
        intentFilter = IntentFilterCondition.getInstance().callIntentFilter();
        registerReceiver(internetBroadcastReceiver, intentFilter);
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
        tvRideFare.setText(rideFare+""+R.string.rupees);
        tvServiceDays.setText(numberOfDays);
        tvVehicleType.setText(vehicleType);
        tvServiceType.setText(serviceType);
        btnSubmit.setText(getString(R.string.proceed_to_pay)+""+Float.parseFloat(rideFare));
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
        tvVehicleType = findViewById(R.id.tv_vehicle_type);
        tvServiceType = findViewById(R.id.tv_service_type);
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
            rideFare = hashMap.get("fare");
            numberOfDays = hashMap.get("serviceDays");
            serviceType = hashMap.get("serviceType");
            vehicleType = hashMap.get("vehicleType");
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
                   serviceDetailMap.put("vehicle_type", vehicleType);
                   serviceDetailMap.put("going_time", tvGoingTime.getText().toString());
                   serviceDetailMap.put("coming_time", tvComingTime.getText().toString());
                   serviceDetailMap.put("service_created_at_time", Calendar.getInstance().getTime().toString());
                   mDatabaseReference.push().setValue(serviceDetailMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                       @Override
                       public void onSuccess(Void aVoid) {
                           generateOrderNumber(countryCode,startDate);

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

    private void generateOrderNumber(String countryCode, String startDate) {
       int sixDigitRandomNumber = getSixDigitRandomNumber();
       String splitedDate = splitDate(startDate);
       String orderNumber = countryCode+splitedDate+String.valueOf(sixDigitRandomNumber);
        sendOrderNumberAndCustomerId(orderNumber,SharedPreference.getInstance().getUserId());
    }

    private void sendOrderNumberAndCustomerId(String orderNumber, String customerId) {
        Toast.makeText(PackageDetailActivity.this, "Data Save Successfully", Toast.LENGTH_SHORT).show();
        startTransaction(orderNumber,customerId);
      /*  Intent intent = new Intent(PackageDetailActivity.this,PaymentTransactionActivity.class);
        intent.putExtra("orderNumber",orderNumber);
        intent.putExtra("customerId",customerId);
        startActivity(intent);
        Log.i(TAG, "onSuccess: ");*/
    }

    private void startTransaction(String orderNumber, String cstId) {
        orderId = orderNumber;
        customerId =cstId;
       sendUserDetailTOServerd dl = new sendUserDetailTOServerd();
            dl.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private String splitDate(String startDate) {
        String[] date = startDate.split("-" );
        return date[1]+date[0];
    }

    private int getSixDigitRandomNumber() {

        // create instance of Random class
        Random rand = new Random();
        // Generate random integers in range 0 to 999
        int rand_six_digit = rand.nextInt(100000);
        return rand_six_digit;
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
                            tvGoingTime.setText( selectedHour + ":" + selectedMinute+"PM"+"(going)");
                        }
                        else
                        {
                            tvGoingTime.setText( selectedHour + ":" + selectedMinute+"AM"+"(going)");

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
                            tvComingTime.setText( selectedHour + ":" + selectedMinute+"PM"+"(coming)");
                        }
                        else
                        {
                            tvComingTime.setText( selectedHour + ":" + selectedMinute+"AM"+"(coming)");

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
                startDate = sdf.format(myCalendar.getTime());
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

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(internetBroadcastReceiver);
    }
    // all paytm callback method
    @Override
    public void onTransactionResponse(Bundle inResponse) {
       // if(inResponse.get(0)
       // RESPCODE=330
        Toast.makeText(this, getString(R.string.transaction_complete)+inResponse, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void networkNotAvailable() {
        Toast.makeText(this, getString(R.string.connection_slow), Toast.LENGTH_SHORT).show();
        Log.i(TAG, "networkNotAvailable: ");
    }

    @Override
    public void clientAuthenticationFailed(String inErrorMessage) {
        Toast.makeText(this, ""+inErrorMessage, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "clientAuthenticationFailed: ");
    }

    @Override
    public void someUIErrorOccurred(String inErrorMessage) {
        Toast.makeText(this, getString(R.string.webpage_not_load), Toast.LENGTH_SHORT).show();
        Log.i(TAG, "someUIErrorOccurred: ");
    }

    @Override
    public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
        Toast.makeText(this, ""+inErrorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressedCancelTransaction() {
        Toast.makeText(this, getString(R.string.dont_press_back_button), Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onBackPressedCancelTransaction: ");
    }

    @Override
    public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
        Toast.makeText(this, ""+inErrorMessage, Toast.LENGTH_SHORT).show();
    }

    // async class for paytmTransaction
    public class sendUserDetailTOServerd extends AsyncTask<ArrayList<String>, Void, String> {
        //private String orderId , mid, custid, amt;
        String url = apiConstant.url;
        String verifyUrl = apiConstant.varifyUrl;
        // "https://securegw-stage.paytm.in/theia/paytmCallback?ORDER_ID"+orderId;
        String CHECKSUMHASH = "";
        private ProgressDialog dialog = new ProgressDialog(PackageDetailActivity.this);

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Please wait");
            this.dialog.show();
        }

        protected String doInBackground(ArrayList<String>... alldata) {
            JsonParsing jsonParser = new JsonParsing(PackageDetailActivity.this);
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
            Service.startPaymentTransaction(PackageDetailActivity.this, true, true,
                    PackageDetailActivity.this);
            Log.i(TAG, "onPostExecute: ");
        }
    }
}
