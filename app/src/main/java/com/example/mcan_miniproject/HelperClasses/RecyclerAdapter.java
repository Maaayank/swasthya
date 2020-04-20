package com.example.mcan_miniproject.HelperClasses;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mcan_miniproject.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder>
{
    Context context;
    ArrayList<StepStats> steps_arr = new ArrayList<StepStats>();

    ArrayList<String> dates;
    public RecyclerAdapter(ArrayList<String> dates, ArrayList<StepStats> p)
    {
        this.dates = dates;
        steps_arr = p;
    }
//    Log.e("Size of array is ",steps_arr.size()+"");
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        StepStats ld=steps_arr.get(position);
        holder.steps.setText(ld.getSteps()+"");
        holder.distance.setText(ld.getDistance()+"");
        holder.date.setText(dates.get(position)+"");
    }

    @Override
    public int getItemCount() {
//        Log.e("size is ",steps_arr.size()+"");
//        System.out.println(steps_arr.size());
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
