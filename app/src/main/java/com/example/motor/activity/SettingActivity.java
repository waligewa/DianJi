package com.example.motor.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Toast;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.base.BaseActivity;
import com.example.motor.util.ActionSheetDialog;
import com.example.motor.util.UserInfoUtils;

import org.xutils.view.annotation.Event;
import org.xutils.x;

public class SettingActivity extends BaseActivity {

    private SharedPreferences prefs1, prefs2;
    private SharedPreferences.Editor editor;
    private Intent intent;
    private String mAddress;
    public static SettingActivity instance1 = null;

    // layout文件
    @Override
    public int setLayout() {
        return R.layout.activity_setting;
    }

    // 在这里面设置TitleBar相关的
    @Override
    public void setTitle() {
        mBaseTitleBarView.setTitleText("设置");
        mBaseTitleBarView.setLeftDrawable(-1);
    }

    // 初始化控件，设置监听
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

    private void init(){
        prefs1 = this.getSharedPreferences("UserInfo", 0);  //  创建一个偏好文件
        prefs2 = this.getSharedPreferences("device", Context.MODE_PRIVATE);
        editor = prefs1.edit();
        mAddress = prefs1.getString("add", "");
        intent = new Intent();
        instance1 = this;
    }

    @Event(value = { R.id.logout_login, R.id.modify_password }, type = View.OnClickListener.class)
    private void btnClick(View v){
        switch (v.getId()){
            case R.id.logout_login:
                new ActionSheetDialog(SettingActivity.this)
                        .builder()
                        .setTitle("确定完全退出该应用程序吗？")
                        .setCancelable(false)
                        .setCanceledOnTouchOutside(false)
                        .addSheetItem("退出应用程序", ActionSheetDialog.SheetItemColor.Red,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        prefs2.edit().putString("EquipmentNo", "").commit();
                                        // 移除key为user的SP字段
                                        UserInfoUtils.getInstance().removeUserCache(SettingActivity.this);
                                        MyApplication.getInstance().exit();
                                    }
                                })
                        .show();
                break;
            case R.id.modify_password:
                intent.setClass(SettingActivity.this, ModifyPasswordActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void toast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
