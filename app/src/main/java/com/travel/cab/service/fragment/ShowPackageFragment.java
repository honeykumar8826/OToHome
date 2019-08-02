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
public class ShowPackageFragment extends Fragment {


    public ShowPackageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setTitle("Home");
        return inflater.inflate(R.layout.fragment_show_package, container, false);
    }

}
