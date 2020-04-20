package com.blackcat.health_0_meter.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
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

import com.blackcat.health_0_meter.Adapters.RecordsStepsAdapter;
import com.blackcat.health_0_meter.Models.Steps;
import com.blackcat.health_0_meter.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecordStepChild extends Fragment
{

    RecyclerView recyclerView;
    DatabaseReference reference;
    SharedPreferences user ;
    RecordsStepsAdapter adapter;
    ArrayList<Steps> list;

    public RecordStepChild()
    {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.records_child1, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        list = new ArrayList<Steps>();
        recyclerView = view.findViewById(R.id.RecordsRecycler);

        user = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        String address = user.getString("address","");

        reference = FirebaseDatabase.getInstance().getReference(address).child("stepstat");

        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                {
                    Steps step = dataSnapshot1.getValue(Steps.class);
                    String date = dataSnapshot1.getKey();
                    step.setDate(date);
                    list.add(step);
                }
                adapter = new RecordsStepsAdapter(list);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });


    }

}