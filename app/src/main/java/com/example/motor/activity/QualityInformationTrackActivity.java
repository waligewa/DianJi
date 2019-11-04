package com.example.motor.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.base.BaseActivity;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

// 质量信息追踪activity
public class QualityInformationTrackActivity extends BaseActivity {

    @ViewInject(R.id.listview)
    ListView listView;
    private Activity mActivity;
    private Intent intent;
    // layout文件
    @Override
    public int setLayout() {
        return R.layout.activity_quality_information_track;
    }

    // 在这里面设置TitleBar相关的
    @Override
    public void setTitle() {
        mBaseTitleBarView.setTitleText("质量信息追踪");
        mBaseTitleBarView.setLeftDrawable(-1);
    }

    // 初始化控件，设置监听
    @SuppressLint("JavascriptInterface")
    @Override
    public void initWidget() {
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);
        init();
    }

    // View的click事件
    @Override
    public void widgetClick(View v) {}

    // 返回键事件
    @Override
    public void onTitleLeftPressed() {
        onBackPressed();
    }

    private void init() {
        mActivity = this;
        intent = new Intent();
    }

    private void toast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
