package com.example.motor.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.motor.R;
import com.example.motor.db.ChronicleInfo;

import java.util.List;

public class ChronicleAdapter extends BaseAdapter {

	private List<ChronicleInfo> mInfos;
	private Context mContext;

	public ChronicleAdapter(Context context, List<ChronicleInfo> infos) {
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
		TextView mcontentTextView, mtimeTextView, deviceId, faultReason, alarmType, alarmState;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder = null;
		if (convertView == null) {
			view = View.inflate(mContext, R.layout.item_chronicle, null);
			holder = new ViewHolder();

			holder.mcontentTextView = (TextView) view.findViewById(R.id.tv_chro_content);  // 报警信息
			holder.mtimeTextView = (TextView) view.findViewById(R.id.tv_chro_right);  // 时间
            holder.deviceId = (TextView) view.findViewById(R.id.tv_device_id);  // 设备id
            holder.faultReason = (TextView) view.findViewById(R.id.tv_fault_reason);  // 故障原因
            holder.alarmType = (TextView) view.findViewById(R.id.alarm_type);  // 报警类型
            holder.alarmState = (TextView) view.findViewById(R.id.alarm_state);   // 报警状态
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		holder.deviceId.setText("泵站名称：" +mInfos.get(position).getDeviceId());
        holder.mtimeTextView.setText("报警时间：" + mInfos.get(position).getTimeString()
                .replace("T"," "));
		holder.faultReason.setVisibility(View.GONE);
        //holder.faultReason.setText("故障原因：" + mInfos.get(position).getFaultReason());
		holder.mcontentTextView.setText("报警信息：" + mInfos.get(position).getContentString());
		holder.alarmType.setText("报警类型：" + mInfos.get(position).getAlarmType());
		if(mInfos.get(position).getAlarmType().equals("紧急报警")){
			holder.alarmType.setBackgroundResource(R.color.common_red);
		} else {
			holder.alarmType.setBackgroundResource(R.color.white);
		}
		holder.alarmState.setText("报警状态：" + mInfos.get(position).getAlarmState());
		return view;
	}
}