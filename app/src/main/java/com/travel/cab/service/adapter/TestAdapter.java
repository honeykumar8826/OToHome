package com.travel.cab.service.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.travel.cab.service.R;
import com.travel.cab.service.modal.StudentInfoModal;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.ViewHolder> {
    private final String TAG = "abc";
    private final List<StudentInfoModal> modalList;

    public TestAdapter(List<StudentInfoModal> modalList) {
        this.modalList = modalList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Log.i(TAG, "onCreateViewHolder: ");
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_test_laoyout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Log.i(TAG, "onBindViewHolder: ");
        StudentInfoModal studentInfoModalList = modalList.get(i);
        Log.i("onBindViewHolder", "onBindViewHolder: " + studentInfoModalList);
        viewHolder.tvUserName.setText(studentInfoModalList.getStudentName());
        viewHolder.tvUserAge.setText(studentInfoModalList.getStudentAge());
    }

    @Override
    public int getItemCount() {
//        Log.i(TAG, "getItemCount: " + modalList.size());

        return modalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUserName, tvUserAge;


        public ViewHolder(@NonNull View itemView) {

            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_book_package_for_data_from);
            tvUserAge = itemView.findViewById(R.id.tv_book_package_for_data_to);

        }
    }
}