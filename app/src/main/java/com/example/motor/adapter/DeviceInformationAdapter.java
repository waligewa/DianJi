package com.example.motor.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.motor.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DeviceInformationAdapter extends BaseAdapter {

    private JSONArray MyData;
    private Context mContext;

    public DeviceInformationAdapter(Context context, JSONArray MyData) {
        this.mContext = context;
        this.MyData = MyData;
    }

    @Override
    public int getCount() {
        return MyData.length();
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
        TextView Worker, DataTime;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder = null;
        if (convertView == null) {
            view = View.inflate(mContext, R.layout.item_device_information, null);
            holder = new ViewHolder();
            holder.Worker = (TextView) view.findViewById(R.id.Worker);
            holder.DataTime = (TextView) view.findViewById(R.id.DataTime);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        try {
            JSONObject jsonObject = MyData.getJSONObject(position);
            holder.Worker.setText(jsonObject.getString("Worker"));
            holder.DataTime.setText(jsonObject.getString("DataTime").replace("T"," "));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }
}