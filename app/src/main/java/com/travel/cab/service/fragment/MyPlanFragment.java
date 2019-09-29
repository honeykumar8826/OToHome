package com.travel.cab.service.fragment;


import android.content.Context;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.travel.cab.service.R;
import com.travel.cab.service.adapter.MyOrderDetailAdapter;
import com.travel.cab.service.database.User;
import com.travel.cab.service.modal.MyOrderDetailModel;
import com.travel.cab.service.modal.UserProfileDetail;
import com.travel.cab.service.utils.preference.SharedPreference;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyPlanFragment extends Fragment {

    private Context context;
    private View myView;
    private FrameLayout myFrameLayout;
    private static final String TAG = "MyPlanFragment";
    private FirebaseDatabase mDatabase;
    private List<MyOrderDetailModel> orderDetailModelList;
    private RecyclerView myOrderRecyclerView;
    private MyOrderDetailAdapter myOrderDetailAdapter;
    public MyPlanFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_my_plan, container, false);
        return myView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        myFrameLayout = view.findViewById(R.id.fl_my_plan) ;
        myOrderRecyclerView = view.findViewById(R.id.rv_my_order) ;
        mDatabase = FirebaseDatabase.getInstance();
        orderDetailModelList = new ArrayList<>();
        setUpRecyclerView();

    }

    private void setUpRecyclerView() {
    myOrderRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    myOrderDetailAdapter = new MyOrderDetailAdapter(orderDetailModelList);
    myOrderRecyclerView.setAdapter(myOrderDetailAdapter);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
       /* View child = getLayoutInflater().inflate(R.layout.custom_layout_no_data_found, null);
        myFrameLayout.addView(child);*/
        try {
            DatabaseReference databaseReference= mDatabase.getReference().child("applyForService").child(SharedPreference.getInstance().getUserId());
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() !=null)
                    {
                        Log.i(TAG, "onDataChange: ");
                         showData(dataSnapshot);
                    }
                    else
                    {
                       // progressBar.setVisibility(View.GONE);
                        Toast.makeText(context, getString(R.string.create_profile), Toast.LENGTH_SHORT).show();
                    }
                    Log.i(TAG, "onDataChange: ");
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //progressBar.setVisibility(View.GONE);
                    Log.i(TAG, "onCancelled: " + databaseError);
                }
            });
        } catch (Exception e) {
           // progressBar.setVisibility(View.GONE);
            Log.i(TAG, "fetchValue: " + e);
        }
    }
    private void showData(DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren()) {

            MyOrderDetailModel orderDetailModel = ds.getValue(MyOrderDetailModel.class);
            Log.i(TAG, "showData: ");
            orderDetailModel.setComing_time(orderDetailModel.getComing_time());
            orderDetailModel.setDistance_home_office(orderDetailModel.getDistance_home_office());
            orderDetailModel.setDrop_location(orderDetailModel.getDrop_location());
            orderDetailModel.setGoing_time(orderDetailModel.getGoing_time());
            orderDetailModel.setOrder_number(orderDetailModel.getOrder_number());
            orderDetailModel.setPickup_location(orderDetailModel.getPickup_location());
            orderDetailModel.setOrder_number(orderDetailModel.getOrder_number());
            orderDetailModel.setService_starting_date(orderDetailModel.getService_starting_date());
            orderDetailModel.setService_days(orderDetailModel.getService_days());
            orderDetailModel.setService_type(orderDetailModel.getService_type());
            orderDetailModel.setVehicle_type(orderDetailModel.getVehicle_type());
            orderDetailModel.setRide_fare(orderDetailModel.getRide_fare());
            orderDetailModelList.add(orderDetailModel);
        }
        myOrderDetailAdapter.notifyDataSetChanged();


    }
}
