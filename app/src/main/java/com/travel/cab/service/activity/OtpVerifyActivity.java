package com.travel.cab.service.activity;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.travel.cab.service.R;
import com.travel.cab.service.broadcast.InternetBroadcastReceiver;
import com.travel.cab.service.ui.IntentFilterCondition;
import com.travel.cab.service.utils.preference.SharedPreference;

public class OtpVerifyActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static final String TAG = "OtpVerifyActivity";
    private EditText etOpt1,etOpt2,etOpt3,etOpt4,etOpt5,etOpt6;
    private Button verifyOtp;
    private String enterOtp, verificationId,generatedOtp,Opt1,Opt2,Opt3,Opt4,Opt5,Opt6;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack;
    private ProgressBar showProgressBar;
    private IntentFilter intentFilter;
    private InternetBroadcastReceiver internetBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verify);
        inItId();
        getValueFromIntent();
        puttingTextWatcherOnOtp();
        verifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                get the value from the user
                showProgressBar.setVisibility(View.VISIBLE);
                getUserInput();
                if (!enterOtp.isEmpty()) {

                    if(enterOtp.equals(generatedOtp))
                    {
//                      send the post reqest on the server
                        verifyCode(generatedOtp,verificationId);
                    }
                    else {
                        showProgressBar.setVisibility(View.GONE);
                        Toast.makeText(OtpVerifyActivity.this, "Invalid OTP ", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    showProgressBar.setVisibility(View.GONE);
                    Toast.makeText(OtpVerifyActivity.this, "OTP is blank", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        intentFilter= IntentFilterCondition.getInstance().callIntentFilter();
        registerReceiver(internetBroadcastReceiver,intentFilter);
    }

    private void puttingTextWatcherOnOtp() {

        etOpt1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length()==1)
                {
                    etOpt2.requestFocus();
                }

            }
        });
        etOpt2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length()==1)
                {
                    etOpt3.requestFocus();
                }
            }
        });
        etOpt3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length()==1)
                {
                    etOpt4.requestFocus();
                }
            }
        });
        etOpt4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length()==1)
                {
                    etOpt5.requestFocus();
                }
            }
        });
        etOpt5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length()==1)
                {
                    etOpt6.requestFocus();
                }
            }
        });
        etOpt6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void verifyCode(String generatedOtp, String verificationId) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, generatedOtp);
        signInWithMobile(credential);
    }

    public void signInWithMobile(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String user_id = mAuth.getCurrentUser().getUid();
                    // put the user unique id value in sharedPreference
                    SharedPreference.getInstance().setUserId(user_id);
                    SharedPreference.getInstance().setFirstTimeForPhone(false);
                    showProgressBar.setVisibility(View.GONE);
                    Toast.makeText(OtpVerifyActivity.this, "Code Verified successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(OtpVerifyActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    showProgressBar.setVisibility(View.GONE);
                    Toast.makeText(OtpVerifyActivity.this, "Code Not Verified Successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void getValueFromIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            verificationId = extras.getString("VERIFICATION_ID");
            generatedOtp = extras.getString("OTP_CODE");
        }
        else {
            Toast.makeText(OtpVerifyActivity.this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
        }
    }

    private void getUserInput() {
        Opt1 = etOpt1.getText().toString();
        Opt2 = etOpt2.getText().toString();
        Opt3 = etOpt3.getText().toString();
        Opt4 = etOpt4.getText().toString();
        Opt5 = etOpt5.getText().toString();
        Opt6 = etOpt6.getText().toString();
        enterOtp = Opt1+Opt2+Opt3+Opt4+Opt5+Opt6;

    }

    private void inItId() {
        // firebase auth intialization
        mAuth = FirebaseAuth.getInstance();
        etOpt1 = findViewById(R.id.et1);
        etOpt2 = findViewById(R.id.et2);
        etOpt3 = findViewById(R.id.et3);
        etOpt4 = findViewById(R.id.et4);
        etOpt5 = findViewById(R.id.et5);
        etOpt6 = findViewById(R.id.et6);
        verifyOtp = findViewById(R.id.btn_verify_otp);
        showProgressBar = findViewById(R.id.show_progress_otp_verify);
        internetBroadcastReceiver = new InternetBroadcastReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(internetBroadcastReceiver);
    }
}