package com.example.motor.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupWindow;

import com.example.motor.R;
import com.example.motor.adapter.AbstractSpinerAdapter;
import com.example.motor.adapter.MultiChoicAdapter;
import com.example.motor.base.CustemInfo;

import java.util.List;

public class MyPopWindow extends PopupWindow implements OnItemClickListener, RefreshListView.OnRefreshListener {

    private Context mContext;
    private RefreshListView mListView;
    private MultiChoicAdapter mAdapter;
    private AbstractSpinerAdapter.IOnItemSelectListener mItemSelectListener;
    private RefreshListView.OnRefreshListener mOnRefreshListener;

    public MyPopWindow(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public void setItemListener(AbstractSpinerAdapter.IOnItemSelectListener listener) {
        mItemSelectListener = listener;
    }

    public void setOnRefreshListener(RefreshListView.OnRefreshListener listener) {
        mOnRefreshListener = listener;
    }

    public void setAdatper(MultiChoicAdapter adapter) {
        mAdapter = adapter;
        mListView.setAdapter(mAdapter);
    }

    private void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.spiner_window_layout, null);
        setContentView(view);
        setWidth(LayoutParams.WRAP_CONTENT);
        setHeight(LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        mListView = (RefreshListView) view.findViewById(R.id.listview);
        mListView.setOnItemClickListener(this);
        mListView.setOnRefreshListener(this);
    }

    public void removeFooterView() {
        mListView.removeFooterView();
    }

    public void hideFooterView() {
        mListView.hideFooterView();
    }

    public void refreshData(List<CustemInfo> objects, boolean[] flag) {
        if (objects != null && flag != null) {
            if (mAdapter != null) {
                mAdapter.refreshData(objects, flag);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3) {
        if (mItemSelectListener != null) {
            mItemSelectListener.onItemClick(pos);
        }
    }

    @Override
    public void onLoadingMore() {
        if (mOnRefreshListener != null) {
            mOnRefreshListener.onLoadingMore();
        }
    }
}
