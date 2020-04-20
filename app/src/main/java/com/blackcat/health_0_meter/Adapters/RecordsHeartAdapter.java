package com.blackcat.health_0_meter.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blackcat.health_0_meter.Models.Heart;
import com.blackcat.health_0_meter.R;

public class RecordsHeartAdapter extends RecyclerView.Adapter<RecordsHeartAdapter.MyViewHolder>
{
    Context context;
    ArrayList<Heart> heart_arr;
    ArrayList<String> dates;

    public RecordsHeartAdapter(ArrayList<Heart> heart_arr )
    {
        this.heart_arr = heart_arr;
        this.dates = dates;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.records_heart_card,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        Heart ld=heart_arr.get(position);
        holder.rate.setText(ld.getRate()+"");
        holder.timestamp.setText(ld.getTimestamp()+"");
        holder.date.setText(ld.getDate() + "");
    }
    @Override
    public int getItemCount() {
        Log.d("heartattack",""+heart_arr.size());

        return heart_arr.size();

    }

    class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView rate, date , timestamp;
        public MyViewHolder(View itemView)
        {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.datetwo);
            timestamp = (TextView) itemView.findViewById(R.id.timestamp);
            rate = (TextView) itemView.findViewById(R.id.rate);
        }
    }
}