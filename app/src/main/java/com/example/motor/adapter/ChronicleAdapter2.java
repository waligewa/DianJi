package com.example.motor.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.motor.R;
import com.example.motor.db.ChronicleInfo;

import java.util.List;

public class ChronicleAdapter2 extends BaseAdapter {

	private List<ChronicleInfo> mInfos;
	private Context mContext;

	public ChronicleAdapter2(Context context, List<ChronicleInfo> infos) {
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
		TextView updateTime, pumpState1, pumpState2, pumpState3, pumpState4, pumpState5;
		// 这三个是用于隐藏3泵 4泵 5泵状态用的
		LinearLayout linearlayout1, linearlayout2, linearlayout3;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder = null;
		int num = mInfos.get(position).getNum(); // 得到水泵的数量
		if (convertView == null) {
			view = View.inflate(mContext, R.layout.item_chronicle2, null);
			holder = new ViewHolder();
            holder.pumpState1 = (TextView) view.findViewById(R.id.tv_pump_state1);  // 1#泵状态
			holder.pumpState2 = (TextView) view.findViewById(R.id.tv_pump_state2);  // 2#泵状态
            holder.pumpState3 = (TextView) view.findViewById(R.id.tv_pump_state3);  // 3#泵状态
            holder.pumpState4 = (TextView) view.findViewById(R.id.tv_pump_state4);  // 4#泵状态
            holder.pumpState5 = (TextView) view.findViewById(R.id.tv_pump_state5);  // 5#泵状态
            holder.linearlayout1 = (LinearLayout) view.findViewById(R.id.linearlayout1); // 3号泵的线性布局
            holder.linearlayout2 = (LinearLayout) view.findViewById(R.id.linearlayout2); // 4号泵的线性布局
            holder.linearlayout3 = (LinearLayout) view.findViewById(R.id.linearlayout3); // 5号泵的线性布局
            holder.updateTime = (TextView) view.findViewById(R.id.tv_chro_right);  // 更新时间
            // 如下判断是用于在界面上分出2泵  3泵  4泵  5泵来
            if(num == 2){
                holder.linearlayout1.setVisibility(View.GONE);
                holder.linearlayout2.setVisibility(View.GONE);
                holder.linearlayout3.setVisibility(View.GONE);
            } else if (num == 3){
                holder.linearlayout1.setVisibility(View.VISIBLE);
                holder.linearlayout2.setVisibility(View.GONE);
                holder.linearlayout3.setVisibility(View.GONE);
            } else if (num == 4){
                holder.linearlayout1.setVisibility(View.VISIBLE);
                holder.linearlayout2.setVisibility(View.VISIBLE);
                holder.linearlayout3.setVisibility(View.GONE);
            } else if (num == 5){
                holder.linearlayout1.setVisibility(View.VISIBLE);
                holder.linearlayout2.setVisibility(View.VISIBLE);
                holder.linearlayout3.setVisibility(View.VISIBLE);
            }
            view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		holder.pumpState1.setText(mInfos.get(position).getDeviceId());  // 1#泵状态
        holder.pumpState2.setText(mInfos.get(position).getContentString());  // 2#泵状态
        holder.pumpState3.setText(mInfos.get(position).getEventMessage());  // 3#泵状态
        holder.pumpState4.setText(mInfos.get(position).getFaultReason());  // 4#泵状态
        holder.pumpState5.setText(mInfos.get(position).getAlarmType());  // 5#泵状态
        holder.updateTime.setText(mInfos.get(position).getTimeString().replace("T"," "));  // 更新时间
		return view;
	}
}