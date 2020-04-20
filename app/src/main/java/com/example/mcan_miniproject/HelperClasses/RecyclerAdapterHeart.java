package com.example.mcan_miniproject.HelperClasses;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mcan_miniproject.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerAdapterHeart extends RecyclerView.Adapter<RecyclerAdapterHeart.MyViewHolder>
{
    Context context;
    ArrayList<HeartrateStat> heart_arr;
    ArrayList<String> dates;

    public RecyclerAdapterHeart(ArrayList<HeartrateStat> heart_arr , ArrayList<String> dates)
    {
        this.heart_arr = heart_arr;
        this.dates = dates;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardviewtwo,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        HeartrateStat ld=heart_arr.get(position);
        holder.rate.setText(ld.getRate()+"");
        holder.timestamp.setText(ld.getTimestamp()+"");
        holder.datetwo.setText(dates.get(position)+"");
    }
    @Override
    public int getItemCount() {
        return heart_arr.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView rate, datetwo , timestamp;
         public MyViewHolder(View itemView)
         {
             super(itemView);
             datetwo = (TextView) itemView.findViewById(R.id.datetwo);
             timestamp = (TextView) itemView.findViewById(R.id.timestamp);
             rate = (TextView) itemView.findViewById(R.id.rate);
         }
    }
}
