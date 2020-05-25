package com.blackcat.health_0_meter.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.blackcat.health_0_meter.Adapters.TabAdapter;
import com.blackcat.health_0_meter.Models.Steps;
import com.blackcat.health_0_meter.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.ArrayList;

public class RecordsFragment extends Fragment {


    private static final String TAG = "hi";
    Query query;
    String s;
    RecyclerView recyclerView;
    DatabaseReference reference;
    private TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    ArrayList<Steps> list;
    ArrayList<String> dates = new ArrayList<String>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_records, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list = new ArrayList<Steps>();
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        tabLayout = (TabLayout) view.findViewById(R.id.tabLayout);
        adapter = new TabAdapter(getChildFragmentManager());
        adapter.addFragment(new RecordStepChild(), "Steps Record");
        adapter.addFragment(new RecordsHeartChild(), "Heart Record");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

    }
}