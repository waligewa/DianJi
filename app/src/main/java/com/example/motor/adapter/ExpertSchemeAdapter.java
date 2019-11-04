package com.example.motor.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.motor.R;
import com.example.motor.db.DeviceStateInfo;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpertSchemeAdapter extends BaseAdapter {

	private List<DeviceStateInfo> mInfos;
	private Context mContext;

	public ExpertSchemeAdapter(Context context, List<DeviceStateInfo> infos) {
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
		TextView number, title, createTime, proDis, planContent;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder = null;
		if (convertView == null) {
			view = View.inflate(mContext, R.layout.item_expert_scheme, null);
			holder = new ViewHolder();
            holder.number = (TextView) view.findViewById(R.id.tv_device_id);  // 方案编号
            holder.title = (TextView) view.findViewById(R.id.tv_chro_right);  // 方案标题
			holder.createTime = (TextView) view.findViewById(R.id.tv_fault_reason);  // 创建时间
			holder.proDis = (TextView) view.findViewById(R.id.tv_chro_content);  // 方案描述
			holder.planContent = (TextView) view.findViewById(R.id.alarm_type);  // 解决方案
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		holder.number.setText(mInfos.get(position).getIdString()); // 方案编号
        holder.title.setText(mInfos.get(position).getRunStateString()); // 方案标题
		Pattern p = Pattern.compile("^([^s]*)\\."); // 取出“.”之前的所有字符串   \\..*$这个正则表达式可以取出后面的数据
		Matcher m = p.matcher(mInfos.get(position).getControlStateString()
				.replace("T"," "));
		m.find();
        holder.createTime.setText(m.group().replace(".", "")); // 创建时间
        holder.proDis.setText(mInfos.get(position).getElectricString()); // 方案描述
        holder.planContent.setText(mInfos.get(position).getNameString()); // 解决方案
		return view;
	}
}