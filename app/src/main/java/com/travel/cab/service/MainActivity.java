package com.travel.cab.service;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.travel.cab.service.network.ApiConstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements PaytmPaymentTransactionCallback

{
    //private String mid="axxflC61464899135819";
    private String mid=BuildConfig.MerchantId;

    private String orderId;
    private String customerId;
    private String mobile_number="7042226632";
    private static final String TAG = "MainActivity";
    private ApiConstant apiConstant;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         apiConstant = new ApiConstant();
        Button paymentStart =  findViewById(R.id.start_transaction);
         EditText orderid =  findViewById(R.id.orderid);
         EditText custid = findViewById(R.id.custid);

        paymentStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 orderId = orderid.getText().toString();
                 customerId = custid.getText().toString();
                sendUserDetailTOServerd dl = new sendUserDetailTOServerd();
                dl.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
    }

    @Override
    public void onTransactionResponse(Bundle inResponse) {
        // response code 01 during successfully transaction RESPCODE=330 STATUS = TXN_FALIURE
        Log.i(TAG, "onTransactionResponse: ");
    }

    @Override
    public void networkNotAvailable() {
        Log.i(TAG, "networkNotAvailable: ");
    }

    @Override
    public void clientAuthenticationFailed(String inErrorMessage) {
        Log.i(TAG, "clientAuthenticationFailed: ");
    }

    @Override
    public void someUIErrorOccurred(String inErrorMessage) {
        Log.i(TAG, "someUIErrorOccurred: ");
    }

    @Override
    public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
        Log.i(TAG, "onErrorLoadingWebPage: ");
    }

    @Override
    public void onBackPressedCancelTransaction() {
        // when we click back button during transaction
        Log.i(TAG, "onBackPressedCancelTransaction: ");
    }

    @Override
    public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
        Log.i(TAG, "onTransactionCancel: ");
    }
    public class sendUserDetailTOServerd extends AsyncTask<ArrayList<String>, Void, String> {
        //private String orderId , mid, custid, amt;
        String url = apiConstant.url;
        String varifyUrl = apiConstant.varifyUrl;
        // "https://securegw-stage.paytm.in/theia/paytmCallback?ORDER_ID"+orderId;
        String CHECKSUMHASH = "";
        private ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Please wait");
            this.dialog.show();
        }

        protected String doInBackground(ArrayList<String>... alldata) {
            JsonParsing jsonParser = new JsonParsing(MainActivity.this);
            String param =
                    "MID=" + mid +
                            "&ORDER_ID=" + orderId +
                            "&CUST_ID=" + customerId +
                            //for testing
                            //"&CHANNEL_ID=WAP&TXN_AMOUNT=1&WEBSITE=WEBSTAGING" +
                            // for production
                            "&CHANNEL_ID=WAP&TXN_AMOUNT=1&WEBSITE=DEFAULT" +
                           // "EMAIL="+ "honeykumar8826@gmail.com"+
                            "&MOBILE_NO="+ mobile_number +
                            "&CALLBACK_URL=" + varifyUrl + "&INDUSTRY_TYPE_ID=Retail";
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
             PaytmPGService  Service = PaytmPGService.getProductionService();
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
            paramMap.put("CALLBACK_URL", varifyUrl);
            //paramMap.put( "EMAIL" , "honeykumar8826@gmail.com");   // no need
            paramMap.put( "MOBILE_NO" , mobile_number);  // no need
            paramMap.put("CHECKSUMHASH", CHECKSUMHASH);
            //paramMap.put("PAYMENT_TYPE_ID" ,"CC");    // no need
            paramMap.put("INDUSTRY_TYPE_ID", "Retail");
            PaytmOrder Order = new PaytmOrder(paramMap);
            Log.e("checksum ", "param " + paramMap.toString());
            Service.initialize(Order, null);
            // start payment service call here
            Service.startPaymentTransaction(MainActivity.this, true, true,
                    MainActivity.this);
            Log.i(TAG, "onPostExecute: ");
        }
    }

}

/*implements OnMapReadyCallback,
        LocationListener,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap mMap;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

}
*/

      /*  package com.travel.cab.service.activity;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.travel.cab.service.R;

import java.util.concurrent.TimeUnit;

        public class PhoneLogin extends AppCompatActivity {

            public static final long TIME_SECOND = 60;
            public static final String TAG = "OtpActivity";
            private EditText etMobile, etVerificationCode;
            private Button sendOtp;
            private String mobileNum, verificationId;
            private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack;
            private FirebaseAuth mAuth;
            private Context context;

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_phone_login);
                //        initialize the id
                inItId();
                context = com.travel.cab.service.activity.PhoneLogin.this;
                // firebase auth intialization
                mAuth = FirebaseAuth.getInstance();
                // FirebaseApp.initializeApp(OtpActivity.this);
                sendOtp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                get the value from the user
                        getUserInput();
                        if (!mobileNum.isEmpty()) {
//                      send the post reqest on the server
                            sendSms();

                        } else {
                            Toast.makeText(com.travel.cab.service.activity.PhoneLogin.this, "Fill all field", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
//        initialize the phoneAuthProvider
                mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        String code = phoneAuthCredential.getSmsCode();
                        if (code != null) {
                            etVerificationCode.setText(code);
                            verifyCode(code);
                        }
                        Log.i(TAG, "onVerificationCompleted: " + code);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Toast.makeText(context, "failed" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verificationId = s;
                        Log.i(TAG, "onCodeSent: " + verificationId);
                    }
                };
            }

            private void verifyCode(String code) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                signInWithMobile(credential);
            }

            private void sendSms() {
                PhoneAuthProvider.getInstance().verifyPhoneNumber("+91" + mobileNum, TIME_SECOND, TimeUnit.SECONDS, com.travel.cab.service.activity.PhoneLogin.this, mCallBack);
                Log.i("as", "sendSms: ");
            }


            private void getUserInput() {
                mobileNum = etMobile.getText().toString();
            }

            private void inItId() {
                etMobile = findViewById(R.id.et_mobile);
                etVerificationCode = findViewById(R.id.verified_code);
                sendOtp = findViewById(R.id.btn_send_otp);
            }

            public void signInWithMobile(PhoneAuthCredential credential) {
                mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(com.travel.cab.service.activity.PhoneLogin.this, "Code Verified successfully", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(context, "Code Not Verified Successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        }
*/

