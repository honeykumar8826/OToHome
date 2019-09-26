package com.travel.cab.service.fragment;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.travel.cab.service.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyPlanFragment extends Fragment {

    private Context context;
    private View myView;
    private FrameLayout myFrameLayout;
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
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View child = getLayoutInflater().inflate(R.layout.custom_layout_no_data_found, null);
        myFrameLayout.addView(child);
    }
}
