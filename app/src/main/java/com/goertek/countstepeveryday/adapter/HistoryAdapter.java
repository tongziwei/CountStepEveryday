package com.goertek.countstepeveryday.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.goertek.countstepeveryday.R;
import com.goertek.countstepeveryday.bean.StepData;

import java.util.List;

/**
 * Created by clara.tong on 2019/1/8.
 */

public class HistoryAdapter extends ArrayAdapter<StepData>{
    private int resourceId;

    public HistoryAdapter(Context context, int resource,List<StepData> objects) {
        super(context, resource,objects);
        this.resourceId = resource;
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        StepData currentDateStep= getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView==null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.dateTextView = (TextView)view.findViewById(R.id.tv_date);
            viewHolder.stepTextView = (TextView)view.findViewById(R.id.tv_step);
            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }
        viewHolder.dateTextView.setText(currentDateStep.getDate());
        viewHolder.stepTextView.setText(String.valueOf(currentDateStep.getStep()));
        return view;
    }

    class ViewHolder{
        TextView dateTextView;
        TextView stepTextView;
    }

}
