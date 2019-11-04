package com.example.motor.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.motor.R;
import com.example.motor.db.DeviceStateInfo;

import java.util.List;

public class DeviceStateAdapter extends BaseAdapter {

    private List<DeviceStateInfo> mInfos;
    private Context mContext;

    public DeviceStateAdapter(Context context, List<DeviceStateInfo> infos) {
        this.mContext = context;
        this.mInfos = infos;
    }

    @Override
    public int getCount() {
        return mInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public class ViewHolder {
        // UI
        TextView ms1TextView, ms2TextView, ms3TextView, ms4TextView, mleft1TextView, mleft2TextView,
                mleft3TextView, mleft4TextView, pumpNumber;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder = null;
        if (convertView == null) {
            view = View.inflate(mContext, R.layout.item_device_state, null);
            holder = new ViewHolder();

            holder.mleft1TextView = (TextView) view.findViewById(R.id.tv_left1);
            holder.mleft2TextView = (TextView) view.findViewById(R.id.tv_left2);
            holder.mleft3TextView = (TextView) view.findViewById(R.id.tv_left3);
            holder.mleft4TextView = (TextView) view.findViewById(R.id.tv_left4);

            holder.ms1TextView = (TextView) view.findViewById(R.id.tv_s1);
            holder.ms2TextView = (TextView) view.findViewById(R.id.tv_s2);
            holder.ms3TextView = (TextView) view.findViewById(R.id.tv_s3);
            holder.ms4TextView = (TextView) view.findViewById(R.id.tv_s4);
            holder.pumpNumber = (TextView) view.findViewById(R.id.pump_number);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.mleft1TextView.setText(mInfos.get(position).getIdString().trim() + "号泵方式：");
        holder.mleft4TextView.setText(mInfos.get(position).getIdString().trim() + "号泵状态：");
        holder.mleft2TextView.setText(mInfos.get(position).getIdString().trim() + "号泵电流：");
        holder.mleft3TextView.setText(mInfos.get(position).getIdString().trim() + "号泵频率：");


        holder.ms1TextView.setText(mInfos.get(position).getRunStateString().trim());
        holder.ms4TextView.setText(mInfos.get(position).getControlStateString().equals("null") ? "" : mInfos.get(position).getControlStateString());
        holder.ms2TextView.setText(String.format("%.1f", Double.parseDouble(mInfos.get(position).getElectricString().trim())));

        /*if (mInfos.get(position).getControlStateString().trim().equals("变频")){
            holder.ms3TextView.setText(String.format("%.1f", Double.parseDouble(mInfos.get(position).getFrequencyString().trim())));
        } else if (mInfos.get(position).getControlStateString().trim().equals("工频")){
            holder.ms3TextView.setText(String.format("%.1f", Double.parseDouble("50.0")));
        } else if (mInfos.get(position).getControlStateString().trim().equals("停止")){
            holder.ms3TextView.setText(String.format("%.1f", Double.parseDouble("0.0")));
        }*/
        if (mInfos.get(position).getControlStateString().trim().equals("工频")){
            holder.ms3TextView.setText(String.format("%.1f", Double.parseDouble("50.0")));
        } else {
            holder.ms3TextView.setText(String.format("%.1f", Double.parseDouble(mInfos.get(position).getFrequencyString().trim())));
        }
        holder.pumpNumber.setText(mInfos.get(position).getIdString().trim() + "号泵");
        //holder.ms3TextView.setText(String.format("%.1f", Double.parseDouble(mInfos.get(position).getFrequencyString())));
        return view;
    }
}