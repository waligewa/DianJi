package com.example.motor.activity;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.base.BaseActivity;

import org.xutils.x;

public class EditActivity extends BaseActivity {

    private Intent intent;

    //layout文件
    @Override
    public int setLayout() {
        return R.layout.activity_edit;
    }

    //在这里面设置TitleBar相关的
    @Override
    public void setTitle() {
        mBaseTitleBarView.setTitleText("编辑界面");
        mBaseTitleBarView.setLeftDrawable(-1);
    }

    //初始化控件，设置监听
    @Override
    public void initWidget() {
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);
        init();
    }

    //View的click事件
    @Override
    public void widgetClick(View v) { }

    //返回键事件
    @Override
    public void onTitleLeftPressed() {
        //onBackPressed();
        intent.setClass(EditActivity.this, PersonalDataActivity.class);
        startActivity(intent);
        finish();
    }

    private void init(){
        intent = new Intent();
    }

    //手机返回按钮返回功能
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                intent.setClass(EditActivity.this, PersonalDataActivity.class);
                startActivity(intent);
                finish();
                break;
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }
}
