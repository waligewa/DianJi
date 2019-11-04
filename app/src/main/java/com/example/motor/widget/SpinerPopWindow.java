package com.example.motor.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupWindow;

import com.example.motor.R;
import com.example.motor.adapter.AbstractSpinerAdapter;

import java.util.List;

public class SpinerPopWindow extends PopupWindow implements OnItemClickListener, RefreshListView.OnRefreshListener {

    private Context mContext;
    //  private ListView mListView;
    private RefreshListView mListView;
    private AbstractSpinerAdapter mAdapter;
    private AbstractSpinerAdapter.IOnItemSelectListener mItemSelectListener;
    private RefreshListView.OnRefreshListener mOnRefreshListener;

    public SpinerPopWindow(Context context) {
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

    public void setAdatper(AbstractSpinerAdapter adapter) {
        mAdapter = adapter;
        mListView.setAdapter(mAdapter);
    }

    private void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.spiner_window_layout, null);
        setContentView(view);
        setWidth(LayoutParams.WRAP_CONTENT);
        setHeight(LayoutParams.WRAP_CONTENT);

        setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x00);
        setBackgroundDrawable(dw);

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

    public <T> void refreshData(List<T> list, int selIndex) {
        if (list != null && selIndex != -1) {
            if (mAdapter != null) {
                mAdapter.refreshData(list, selIndex);
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
