package com.travel.cab.service.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.travel.cab.service.R;
import com.travel.cab.service.broadcast.InternetBroadcastReceiver;
import com.travel.cab.service.ui.IntentFilterCondition;
import com.travel.cab.service.utils.validation.CustomCheck;

import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;

public class PhoneLogin extends AppCompatActivity {

    public static final long TIME_SECOND = 60;
    public static final String TAG = "OtpActivity";
    private EditText etMobile;
    private Button sendOtp;
    private String mobileNum, verificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack;
    private FirebaseAuth mAuth;
    private Context context;
    private ProgressBar showProgressBar;
    private InternetBroadcastReceiver internetBroadcastReceiver;
    private IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        //        initialize the id
        inItId();
        context = PhoneLogin.this;
        // firebase auth intialization
        mAuth = FirebaseAuth.getInstance();
        // FirebaseApp.initializeApp(OtpActivity.this);
        sendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                get the value from the user
                showProgressBar.setVisibility(View.VISIBLE);
                getUserInput();
                if (CustomCheck.getInstance().checkPhoneNumber(mobileNum)) {
                    if (InternetBroadcastReceiver.isNetworkInterfaceAvailable(PhoneLogin.this)) {
                        // send the post reqest on the server
                        sendSms();
                    } else {
                        showProgressBar.setVisibility(View.GONE);
                        Toast.makeText(PhoneLogin.this, "You are Offline", Toast.LENGTH_SHORT).show();

                    }
//

                } else {
                    showProgressBar.setVisibility(View.GONE);
                    Toast.makeText(PhoneLogin.this, "Enter valid mobile number", Toast.LENGTH_SHORT).show();
                }
            }
        });
//        initialize the phoneAuthProvider
        mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                String code = phoneAuthCredential.getSmsCode();
                if (code != null) {
                    showProgressBar.setVisibility(View.GONE);
                    Intent intent = new Intent(getBaseContext(), OtpVerifyActivity.class);
                    intent.putExtra("VERIFICATION_ID", verificationId);
                    intent.putExtra("OTP_CODE", code);
                    startActivity(intent);
                } else {
                    showProgressBar.setVisibility(View.GONE);
                    Toast.makeText(context, "Authentication Problem", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "onVerificationCompleted: " + code);
                }

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                showProgressBar.setVisibility(View.GONE);
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

    @Override
    protected void onStart() {
        super.onStart();
        intentFilter = IntentFilterCondition.getInstance().callIntentFilter();
        registerReceiver(internetBroadcastReceiver, intentFilter);
    }

    private void sendSms() {

        PhoneAuthProvider.getInstance().verifyPhoneNumber("+91" + mobileNum, TIME_SECOND, TimeUnit.SECONDS, PhoneLogin.this, mCallBack);
        Log.i("as", "sendSms: ");
    }


    private void getUserInput() {
        mobileNum = etMobile.getText().toString();
    }

    private void inItId() {
        etMobile = findViewById(R.id.et_mobile);
        sendOtp = findViewById(R.id.btn_send_otp);
        showProgressBar = findViewById(R.id.show_progress);
        internetBroadcastReceiver = new InternetBroadcastReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(internetBroadcastReceiver);
    }
}
