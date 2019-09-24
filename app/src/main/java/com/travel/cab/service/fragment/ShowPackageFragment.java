package com.travel.cab.service.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.travel.cab.service.R;
import com.travel.cab.service.activity.DefaultPackageDetail;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class ShowPackageFragment extends Fragment implements View.OnClickListener {
    Boolean isButtonClickedTo = false;
    Boolean isButtonClickedFrom = false;
    private ImageView showMoreContentForTo, showMoreContentForFrom;
    private TextView toContent, fromContent,tvDistance;
    private Context context;
    private String toAddress, fromAddress;
    private Button btnBuyPlan;

    public ShowPackageFragment() {
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
        btnBuyPlan = view.findViewById(R.id.btn_buy_plan);
        tvDistance = view.findViewById(R.id.tv_distance_default_package);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getAddressValue();
        showMoreContentForTo.setOnClickListener(this);
        showMoreContentForFrom.setOnClickListener(this);
        btnBuyPlan.setOnClickListener(this);

    }

    private void getAddressValue() {
        fromAddress = fromContent.getText().toString();
        toAddress = toContent.getText().toString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.img_show_package_more_content_to:
                String getherDataTo = "Hcl Technologies, Tower-2, Sector 126, Noida, Uttar Pradesh";
                if (!isButtonClickedTo) {
                    toContent.setText(getherDataTo);
                    toAddress = toContent.getText().toString();
                    isButtonClickedTo = true;
                } else {
                    toContent.setText(R.string.content_to_show_package);
                    toAddress = toContent.getText().toString();
                    isButtonClickedTo = false;
                }

                break;
            case R.id.img_show_package_more_content_from:
                String getherDataFrom = "Shipra Suncity, Indirapuram, Ghaziabad, Uttar Pradesh";
                if (!isButtonClickedFrom) {
                    fromContent.setText(getherDataFrom);
                    fromAddress = fromContent.getText().toString();
                    isButtonClickedFrom = true;
                } else {
                    fromContent.setText(R.string.content_from_show_package);
                    fromAddress = fromContent.getText().toString();
                    isButtonClickedFrom = false;
                }

                break;
            case R.id.btn_buy_plan:
                sendPackageDetailViaIntent();
                break;

            default:
                break;

        }
    }

    private void sendPackageDetailViaIntent() {
        Map<String, String> packageMap = new HashMap<>();
        packageMap.put("fromLocation", fromAddress);
        packageMap.put("toLocation", toAddress);
        packageMap.put("distanceDiff", tvDistance.getText().toString());
//        mPackageInfo.getPackageDetail(packageMap);
        Intent intent = new Intent(context, DefaultPackageDetail.class);
        intent.putExtra("packageInfoMap", (Serializable) packageMap);
        startActivity(intent);

    }
}