package com.travel.cab.service.fragment;

import android.content.Context;
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
import com.travel.cab.service.modal.UserProfileDetail;
import com.travel.cab.service.utils.preference.SharedPreference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;


public class VIewProfileFragment extends Fragment {

    private static final String TAG = "MainActivity";
    private FirebaseDatabase mDatabase;
    private TextView tvName;
    private CircleImageView imageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvName = view.findViewById(R.id.tv_name);
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
                    .load(userProfileDetail.getImage_url())
                    .centerCrop()
                    .into(imageView);

            // imageView.setImageURI(Uri.parse(userInformation.getCourse()));
            //tvName.setText(userProfile.getName());


            //


            /*UserInformation userInformation = new UserInformation();
            userInformation.setAge(ds.child("name").getValue(UserInformation.class).getAge());
            userInformation.setCourse(ds.child("name").getValue(UserInformation.class).getCourse());*/

            Log.i(TAG, "showData: " + userProfileDetail.getAddress());
            // Log.i(TAG, "showData: "+ userProfile.getImage_url());

       /*     UserInformation uInfo = new UserInformation();
            uInfo.setName(ds.child(userID).getValue(UserInformation.class).getName()); //set the name
            uInfo.setEmail(ds.child(userID).getValue(UserInformation.class).getEmail()); //set the email
            uInfo.setPhone_num(ds.child(userID).getValue(UserInformation.class).getPhone_num()); //set the phone_num

            //display all the information
            Log.d(TAG, "showData: name: " + uInfo.getName());
            Log.d(TAG, "showData: email: " + uInfo.getEmail());
            Log.d(TAG, "showData: phone_num: " + uInfo.getPhone_num());

            ArrayList<String> array  = new ArrayList<>();
            array.add(uInfo.getName());
            array.add(uInfo.getEmail());
            array.add(uInfo.getPhone_num());
            ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,array);
            mListView.setAdapter(adapter);*/
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }


}
//

