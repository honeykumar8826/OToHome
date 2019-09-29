package com.travel.cab.service;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.travel.cab.service.adapter.TestAdapter;
import com.travel.cab.service.modal.StudentInfoModal;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends AppCompatActivity {

    private List<StudentInfoModal> listItem;
    private TestAdapter studentInfoAdapter;
    private Button btnDataSet;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        /*id initialization*/
        inItIds();
        /*set the data on recyclerView with blank list*/
        setBlankListOnRecyclerView();

        btnDataSet.setOnClickListener(v -> {

            /*set the data on recyclerView with data list*/
            setDataOnRecyclerView();

        });
    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            Toast.makeText(TestActivity.this, ""+ linearLayoutManager.findFirstVisibleItemPosition(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    });
    }

    private void setDataOnRecyclerView() {

        for(int i=0;i<100;i++)
        {

            StudentInfoModal studentInfoModalList = new StudentInfoModal("harsh"+i,"21");
            listItem.add(studentInfoModalList);
        }

        studentInfoAdapter.notifyDataSetChanged();

    }

    private void setBlankListOnRecyclerView() {
       linearLayoutManager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        listItem = new ArrayList<>();
        studentInfoAdapter = new TestAdapter(listItem);
        recyclerView.setAdapter(studentInfoAdapter);
        Toast.makeText(this, "size="+listItem.size(), Toast.LENGTH_SHORT).show();
    }

    private void inItIds() {
        recyclerView = findViewById(R.id.recycleList);
        btnDataSet = findViewById(R.id.button_dataset);
    }

}