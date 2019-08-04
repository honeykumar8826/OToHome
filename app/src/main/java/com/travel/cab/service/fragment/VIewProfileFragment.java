package com.travel.cab.service.fragment;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.travel.cab.service.R;
import com.travel.cab.service.broadcast.InternetBroadcastReceiver;
import com.travel.cab.service.modal.UserProfileDetail;
import com.travel.cab.service.ui.IntentFilterCondition;
import com.travel.cab.service.utils.preference.SharedPreference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;


public class VIewProfileFragment extends Fragment {

    private static final String TAG = "MainActivity";
    private FirebaseDatabase mDatabase;
    private TextView tvName,tvMobile,tvEmail,tvAddress,tvComapnyName;
    private CircleImageView imageView;
    private IntentFilter intentFilter;
    private InternetBroadcastReceiver internetBroadcastReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        internetBroadcastReceiver = new InternetBroadcastReceiver();
        tvName = view.findViewById(R.id.tv_name);
        tvMobile = view.findViewById(R.id.tv_mobile);
        tvEmail = view.findViewById(R.id.tv_email);
        tvAddress = view.findViewById(R.id.tv_address);
        tvComapnyName = view.findViewById(R.id.tv_companyName);
        imageView = view.findViewById(R.id.img_user);
        mDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fetchValue();
    }

    private void fetchValue() {
        Log.i(TAG, "onStart: ");
        try {
          DatabaseReference databaseReferenc= mDatabase.getReference().child("users").child(SharedPreference.getInstance().getUserId());
            databaseReferenc.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.i(TAG, "onDataChange: ");
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    showData(dataSnapshot);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.i(TAG, "onCancelled: " + databaseError);
                }
            });
        } catch (Exception e) {
            Log.i(TAG, "fetchValue: " + e);
        }

    }

    private void showData(DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren()) {

            UserProfileDetail userProfileDetail = ds.getValue(UserProfileDetail.class);

            tvName.setText(userProfileDetail.getName());

            Glide.with(this)
                    .load(userProfileDetail.getImage_Url())
                    .centerCrop()
                    .into(imageView);
            tvName.setText("Name            :   "+userProfileDetail.getName());
            tvMobile.setText("Mobile        :   "+userProfileDetail.getMobile());
            tvEmail.setText("Email          :   "+userProfileDetail.getEmail());
            tvAddress.setText("Address      :   "+userProfileDetail.getAdres());
            tvComapnyName.setText("Company  :   "+userProfileDetail.getCompany());
            Log.i(TAG, "showData: "+ userProfileDetail.getEmail());


            Log.i(TAG, "showData: " + userProfileDetail.getAdres());
            // Log.i(TAG, "showData: "+ userProfile.getImage_url());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onResume() {
        super.onResume();
        intentFilter= IntentFilterCondition.getInstance().callIntentFilter();
        getActivity().registerReceiver(internetBroadcastReceiver,intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(internetBroadcastReceiver);
    }
}
//

