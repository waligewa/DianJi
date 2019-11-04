package com.example.motor.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.motor.R;
import com.example.motor.base.CustemInfo;

import java.util.ArrayList;
import java.util.List;

public class MultiChoicAdapter extends BaseAdapter implements OnItemClickListener {

    private Context mContext;
    private List<CustemInfo> mObjects = new ArrayList<CustemInfo>();
    private boolean mBoolean[] = null;
    private LayoutInflater mInflater;

    public MultiChoicAdapter(Context context) {
        init(context);
    }

    public MultiChoicAdapter(Context context, List<CustemInfo> objects, boolean[] flag) {
        init(context);
        mObjects = objects;
        mBoolean = flag;
    }

    private void init(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void refreshData(List<CustemInfo> objects, boolean[] flag) {
        if (objects != null) {
            mObjects = objects;
            mBoolean = flag;
            notifyDataSetChanged();
        }
    }

    public void setSelectItem(boolean[] flag) {
        if (flag != null) {
            mBoolean = flag;
            notifyDataSetChanged();
        }
    }

    public boolean[] getSelectItem() {
        return mBoolean;
    }

    public void clear() {
        mObjects.clear();
        notifyDataSetChanged();
    }

    public int getCount() {
        return mObjects.size();
    }

    public CustemInfo getItem(int position) {
        return mObjects.get(position);
    }

    public int getPosition(CustemInfo item) {
        return mObjects.indexOf(item);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.choice_list_item_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.mTextView = (TextView) convertView.findViewById(R.id.textView);
            viewHolder.mCheckBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.mCheckBox.setChecked(mBoolean[position]);
        CustemInfo item = getItem(position);
        viewHolder.mTextView.setText(item.getDeviceName());
        viewHolder.mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("===点击了===", position + "");
                mBoolean[position] = !mBoolean[position];
                Log.e("===mBoolean===", mBoolean[position] + "");
                notifyDataSetChanged();
            }
        });
        return convertView;
    }

    public static class ViewHolder {
        public TextView mTextView;
        public CheckBox mCheckBox;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        Log.e("===点击了===", position + "");
        mBoolean[position] = !mBoolean[position];
        Log.e("=== mBoolean===", mBoolean[position] + "");
        notifyDataSetChanged();
    }
}
