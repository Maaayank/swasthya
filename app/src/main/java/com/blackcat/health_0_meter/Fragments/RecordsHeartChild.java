package com.blackcat.health_0_meter.Fragments;


import android.content.Context;
import android.content.SharedPreferences;
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

import com.blackcat.health_0_meter.Adapters.RecordsHeartAdapter;
import com.blackcat.health_0_meter.Adapters.RecordsStepsAdapter;
import com.blackcat.health_0_meter.Models.Heart;
import com.blackcat.health_0_meter.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RecordsHeartChild extends Fragment
{
    RecyclerView recyclerView;
    FirebaseDatabase mdb;
    DatabaseReference data_ref ;
    RecordsHeartAdapter adapter;
    ArrayList<Heart> list;
    SharedPreferences user ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.records_child2, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        list = new ArrayList<Heart>();
        recyclerView = view.findViewById(R.id.RecordsRecycler);

        user = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        String address = user.getString("address","");

        mdb = FirebaseDatabase.getInstance();
        data_ref = mdb.getReference(address).child("heartratestats");
//        Log.e(TAG , "date:"+s);

        try {
            data_ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        try {
                            final String key = dataSnapshot1.getKey();
                            data_ref.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {

                                        Log.d("heartattack", "key" + key);
                                        Heart heart = new Heart();
                                        int rate = dataSnapshot2.getValue(Integer.class);
                                        Log.d("heartattack", "rate" + rate);
                                        heart.setDate(key);
                                        heart.setRate(rate);
                                        String time = dataSnapshot2.getKey();
                                        Log.d("heartattack", "time" + time);
                                        heart.setTimestamp(time);
                                        list.add(heart);
                                    }

                                    adapter = new RecordsHeartAdapter(list);
                                    recyclerView.setAdapter(adapter);
                                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }catch (Exception e){

                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }catch (Exception e){

        }
    }
}
