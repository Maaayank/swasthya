package com.example.mcan_miniproject.Fragments;

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

import com.example.mcan_miniproject.HelperClasses.HeartrateStat;
import com.example.mcan_miniproject.HelperClasses.RecyclerAdapter;
import com.example.mcan_miniproject.HelperClasses.RecyclerAdapterHeart;
import com.example.mcan_miniproject.HelperClasses.StepStats;
import com.example.mcan_miniproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChildtwoFragment extends Fragment
{
    RecyclerView recyclerViewtwo;
    DatabaseReference referencetwo , refer;
    RecyclerAdapterHeart adaptertwo;
    ArrayList<HeartrateStat> listtwo;
    ArrayList<String> datestwo = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_childtwo, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        listtwo = new ArrayList<HeartrateStat>();
        recyclerViewtwo = view.findViewById(R.id.RecordsRecycler);
        referencetwo = FirebaseDatabase.getInstance().getReference().child("Users/uid/HeartrateStat");
//        Log.e(TAG , "date:"+s);
        referencetwo.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                {
                    HeartrateStat p = dataSnapshot1.getValue(HeartrateStat.class);
                    String p2 = dataSnapshot1.getKey();
                    Log.e("value of rate is ",p.rate+"");
                    listtwo.add(p);
                    datestwo.add(p2);
//                    Log.d("Date:",p2);
                }
                adaptertwo = new RecyclerAdapterHeart(listtwo,datestwo);
                recyclerViewtwo.setAdapter(adaptertwo);
                recyclerViewtwo.setLayoutManager(new LinearLayoutManager(getContext()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
}
