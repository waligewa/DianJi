package com.example.motor.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.motor.R;
import com.example.motor.videoSurveillance.MonitorInfo2;

import java.util.List;

public class VideoAdapter extends BaseAdapter {

    private List<MonitorInfo2> mInfos;
    private Context mContext;

    public VideoAdapter(Context context, List<MonitorInfo2> infos) {
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder = null;
        if (convertView == null) {
            view = View.inflate(mContext, R.layout.simple_list_item_text, null);
            holder = new ViewHolder();
            holder.textView1 = (TextView) view.findViewById(R.id.text1);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.textView1.setText(mInfos.get(position).getIP());
        return view;
    }

    public class ViewHolder {
        TextView textView1;
    }
}