package com.travel.cab.service.fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.travel.cab.service.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class ShowPackageFragment extends Fragment implements View.OnClickListener {
    private ImageView showMoreContentForTo,showMoreContentForFrom;
    private TextView toContent,fromContent;
    Boolean isButtonClickedTo = false;
    Boolean isButtonClickedFrom = false;

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showMoreContentForTo = view.findViewById(R.id.img_show_package_more_content_to);
        showMoreContentForFrom = view.findViewById(R.id.img_show_package_more_content_from);
        toContent = view.findViewById(R.id.tv_show_package_to_data);
        fromContent = view.findViewById(R.id.tv_show_package_for_data_from);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showMoreContentForTo.setOnClickListener(this);
        showMoreContentForFrom.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {

            case R.id.img_show_package_more_content_to:
                String getherDataTo = "thisisssssssssssssssssssssssssdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddssssssssssssssssssssssssssssssssssssssssss";
                if(!isButtonClickedTo)
                {
                    toContent.setText(getherDataTo);
                    isButtonClickedTo = true;
                }
                else {
                    toContent.setText(R.string.content_to_show_package);
                    isButtonClickedTo = false;
                }

                break;
            case R.id.img_show_package_more_content_from:
                String getherDataFrom = "thisisssssssssssssssssssssssssdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddssssssssssssssssssssssssssssssssssssssssss";
                if(!isButtonClickedFrom)
                {
                    fromContent.setText(getherDataFrom);
                    isButtonClickedFrom = true;
                }
                else {
                    fromContent.setText(R.string.content_from_show_package);
                    isButtonClickedFrom = false;
                }

                break;
            default:
                break;

        }
    }
}