package com.travel.cab.service.fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.travel.cab.service.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CouponFragment extends Fragment {


    public CouponFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_coupon, container, false);
    }

}
