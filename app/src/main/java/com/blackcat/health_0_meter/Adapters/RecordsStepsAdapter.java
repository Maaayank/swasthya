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

import com.blackcat.health_0_meter.Models.Steps;
import com.blackcat.health_0_meter.R;

public class RecordsStepsAdapter extends RecyclerView.Adapter<RecordsStepsAdapter.MyViewHolder>
{
    Context context;
    ArrayList<Steps> steps_arr ;

    public RecordsStepsAdapter( ArrayList<Steps> list)
    {
        steps_arr = list;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.records_step_card,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        Steps ld = steps_arr.get(position);
        holder.steps.setText(ld.getSteps()+"");
        holder.distance.setText(ld.getDistance()+"");
        holder.date.setText(ld.getDate() + "");

        //todo duration in steps recycler tomorrow
    }

    @Override
    public int getItemCount() {
        return steps_arr.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder
    {
        private TextView steps,distance,date;
        public MyViewHolder(@NonNull View itemView)
        {
            super(itemView);
            steps = (TextView) itemView.findViewById(R.id.step);
            distance = (TextView) itemView.findViewById(R.id.distance);
            date = (TextView) itemView.findViewById(R.id.date);
        }
    }
}