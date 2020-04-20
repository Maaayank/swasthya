package com.example.mcan_miniproject.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
//import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.mcan_miniproject.HelperClasses.RecyclerAdapter;
import com.example.mcan_miniproject.HelperClasses.StepStats;
import com.example.mcan_miniproject.HelperClasses.TabAdapter;
import com.example.mcan_miniproject.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.EventListener;

public class RecordsFragment extends Fragment
{
    private static final String TAG = "hi";
    Query query;
    String s;
    //change3
//    private OnFragmentInteractionListener mListener;
    RecyclerView recyclerView;
    DatabaseReference reference;
//    RecyclerAdapter adapter;
    private TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    ArrayList<StepStats> list;
    ArrayList<String> dates = new ArrayList<String>();
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_records, container, false);
        return root;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        //change1
        Fragment childFragment = new ChildFragment();
//        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
//        transaction.replace(R.id.child_fragment_container, childFragment).commit();
        list = new ArrayList<StepStats>();
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        tabLayout = (TabLayout) view.findViewById(R.id.tabLayout);
        adapter = new TabAdapter(getChildFragmentManager());
        adapter.addFragment(new ChildFragment(), "Tab 1");
        adapter.addFragment(new ChildtwoFragment(), "Tab 2");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
//        recyclerView = view.findViewById(R.id.RecordsRecycler);
//        reference = FirebaseDatabase.getInstance().getReference().child("Users/uid/stepCount");
//        Log.e(TAG , "date:"+s);
//        reference.addValueEventListener(new ValueEventListener()
//        {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//            {
//                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
//                {
//                    StepStats p = dataSnapshot1.getValue(StepStats.class);
//                    String p2 = dataSnapshot1.getKey();
//                    list.add(p);
//                    dates.add(p2);
//                    Log.d("Date:",p2);
//                }
//                adapter = new RecyclerAdapter(dates,list);
//                recyclerView.setAdapter(adapter);
//                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError)
//            {
//
//            }
//        });


    }
    //change2
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener)
//        {
//            mListener = (OnFragmentInteractionListener) context;
//        }
//        else
//        {
//        throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach()
//    {
//        super.onDetach();
//        mListener = null;
//    }
//
//    public interface OnFragmentInteractionListener
//    {
//        // TODO: Update argument type and name
//        void messageFromParentFragment(Uri uri);
//    }

}