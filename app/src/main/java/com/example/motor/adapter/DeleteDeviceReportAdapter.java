package com.example.motor.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.example.motor.R;
import com.example.motor.db.DeleteDeviceReportItem;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeleteDeviceReportAdapter extends BaseAdapter {

	private List<DeleteDeviceReportItem> mInfos;
	private Context mContext;

	public DeleteDeviceReportAdapter(Context context, List<DeleteDeviceReportItem> infos) {
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
		TextView deviceName, deviceNumber, deviceAddress, reportTime, feedbackPerson,
				faultPhenomenon, dispatchSituation;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder = null;
		if (convertView == null) {
			view = View.inflate(mContext, R.layout.item_delete_device_report, null);
			holder = new ViewHolder();
            holder.deviceName = (TextView) view.findViewById(R.id.device_name);  // 设备名称
            holder.deviceNumber = (TextView) view.findViewById(R.id.device_number);  // 设备编号
			holder.deviceAddress = (TextView) view.findViewById(R.id.device_address);  // 设备地址
			holder.reportTime = (TextView) view.findViewById(R.id.report_time);  // 提报时间
			holder.feedbackPerson = (TextView) view.findViewById(R.id.feedback_person);  // 反馈人
            holder.faultPhenomenon = (TextView) view.findViewById(R.id.fault_phenomenon); // 故障现象
            holder.dispatchSituation = (TextView) view.findViewById(R.id.dispatch_situation); // 派工情况
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		holder.deviceName.setText(mInfos.get(position).getCompany()); // 设备名称
        holder.deviceNumber.setText(mInfos.get(position).getEquipmentNo()); // 设备编号
		String s = mInfos.get(position).getTime().replace("T"," ");
		if(mInfos.get(position).getTime().contains(".")){
			Pattern p = Pattern.compile("^([^s]*)\\."); // 取出“.”之前的所有字符串   \\..*$这个正则表达式可以取出后面的数据
			Matcher m = p.matcher(s);
			m.find();
			holder.reportTime.setText(m.group().replace(".", "")); // 提报时间
		} else {
			holder.reportTime.setText(s); // 提报时间
		}
		holder.deviceAddress.setText(mInfos.get(position).getDevicePosition()); // 设备地址
		holder.feedbackPerson.setText(mInfos.get(position).getFeedbackPerson()); // 反馈人
        holder.faultPhenomenon.setText(mInfos.get(position).getSymptom()); // 故障现象
        holder.dispatchSituation.setText(mInfos.get(position).getIsrevice()); // 派工情况
		return view;
	}
}