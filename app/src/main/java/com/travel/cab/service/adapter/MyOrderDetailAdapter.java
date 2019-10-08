package com.travel.cab.service.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.travel.cab.service.R;
import com.travel.cab.service.modal.MyOrderDetailModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyOrderDetailAdapter extends RecyclerView.Adapter<MyOrderDetailAdapter.ViewHolder> {
    private final String TAG = "abc";
    private final List<MyOrderDetailModel> orderDetailModelList;

    public MyOrderDetailAdapter(List<MyOrderDetailModel> modalList) {
        this.orderDetailModelList = modalList;
    }

    @NonNull
    @Override
    public MyOrderDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Log.i(TAG, "onCreateViewHolder: ");
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_layout_show_my_orders, viewGroup, false);
        return new MyOrderDetailAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyOrderDetailAdapter.ViewHolder viewHolder, int i) {
        Log.i(TAG, "onBindViewHolder: ");
        MyOrderDetailModel myOrderDetailModel = orderDetailModelList.get(i);
        viewHolder.tvPickup.setText(myOrderDetailModel.getPickup_location());
        viewHolder.tvDrop.setText(myOrderDetailModel.getDrop_location());
        viewHolder.tvDistance.setText(myOrderDetailModel.getDistance_home_office());
        viewHolder.tvServiceDays.setText(myOrderDetailModel.getService_days());
        viewHolder.tvServiceType.setText(myOrderDetailModel.getService_type());
        viewHolder.tvVehicleType.setText(myOrderDetailModel.getVehicle_type());
        viewHolder.tvRideFare.setText(myOrderDetailModel.getService_fare());
        viewHolder.tvServiceStartDate.setText(myOrderDetailModel.getService_starting_date());
        viewHolder.tvGoingTime.setText(myOrderDetailModel.getGoing_time());
        viewHolder.tvComingTime.setText(myOrderDetailModel.getComing_time());
        if(myOrderDetailModel.getOrder_number() !=null && !myOrderDetailModel.getOrder_number().equals("") )
        viewHolder.tvOrderNumber.setText(myOrderDetailModel.getOrder_number());
        else
            viewHolder.tvOrderNumber.setText("pending");
    }

    @Override
    public int getItemCount() {
//        Log.i(TAG, "getItemCount: " + modalList.size());

        return orderDetailModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPickup, tvDrop, tvDistance, tvServiceDays, tvRideFare,
                tvOrderNumber, tvServiceStartDate, tvServiceType, tvVehicleType, tvGoingTime, tvComingTime;


        public ViewHolder(@NonNull View itemView) {

            super(itemView);
            tvPickup = itemView.findViewById(R.id.tv_pickup_point_show);
            tvDrop = itemView.findViewById(R.id.tv_drop_point_show);
            tvDistance = itemView.findViewById(R.id.tv_distance_show);
            tvServiceDays = itemView.findViewById(R.id.tv_service_days_show);
            tvRideFare = itemView.findViewById(R.id.tv_fare_show);
            tvOrderNumber = itemView.findViewById(R.id.tv_order_name_show);
            tvServiceStartDate = itemView.findViewById(R.id.tv_service_starting_date_show);
            tvServiceType = itemView.findViewById(R.id.tv_service_type_show);
            tvVehicleType = itemView.findViewById(R.id.tv_vehicle_type_show);
            tvGoingTime = itemView.findViewById(R.id.tv_going_timing_show);
            tvComingTime = itemView.findViewById(R.id.tv_coming_timing_show);

        }
    }
}