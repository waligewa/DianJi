package com.example.motor.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.base.BaseActivity;
import com.example.motor.constant.ConstantsField;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DoubleClickUtils;
import com.example.motor.util.SPUtils;
import com.example.motor.util.UserInfoUtils;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.x;

public class ModifyPasswordActivity extends BaseActivity {

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private EditText oldPassword, newPassword, confirmPassword;
    private String password, oldPwdValue, newPwdValue, confirmPwdValue, address, guid;
    private Intent intent;
    private int userId;

    // layout文件
    @Override
    public int setLayout() {
        return R.layout.activity_modify_password;
    }

    // 在这里面设置TitleBar相关的
    @Override
    public void setTitle() {
        mBaseTitleBarView.setTitleText("修改密码");
        mBaseTitleBarView.setLeftDrawable(-1);
    }

    // 初始化控件，设置监听
    @Override
    public void initWidget() {
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);
        init();
    }

    private void init() {
        intent = new Intent();
        prefs = getSharedPreferences("UserInfo", 0); // 创建一个数据库
        editor = prefs.edit();
        oldPassword = (EditText) findViewById(R.id.old_password);          // 原密码
        newPassword = (EditText) findViewById(R.id.new_password);          // 新密码
        confirmPassword = (EditText) findViewById(R.id.confirm_password);  // 确认新密码
        address = prefs.getString("add", "");
        // 三个输入框的初始化
        oldPassword.setText("");
        newPassword.setText("");
        confirmPassword.setText("");
        // 从SP中取出id   password
        userId = prefs.getInt("UserID", 0);
        password = prefs.getString("userpass", "");
        guid = prefs.getString("guid", "");
        Log.e("哈哈哈", userId + "+++" + guid);
    }

    // View的click事件
    @Override
    public void widgetClick(View v) {}

    @Event(value = { R.id.change_password }, type = View.OnClickListener.class)
    private void btnClick(View v){
        switch (v.getId()){
            case R.id.change_password:
                oldPwdValue = oldPassword.getText().toString().trim(); // 原密码字符串
                newPwdValue = newPassword.getText().toString().trim(); // 新密码字符串
                confirmPwdValue = confirmPassword.getText().toString().trim(); //确认新密码字符串
                if (oldPwdValue.equals("")) {
                    toast("原密码输入不能为空!");
                }else if(!(oldPwdValue.equals(password))){
                    toast("原密码输入错误");
                }else if(newPwdValue.equals("")){
                    toast("新密码输入不能为空!");
                } else if (confirmPwdValue.equals("")) {
                    toast("确认新密码输入不能为空!");
                } else if (!(confirmPwdValue.equals(newPwdValue))) {
                    toast("两次密码输入不一致!");
                    confirmPassword.setText("");
                } else if(!DoubleClickUtils.isFastDoubleClick()){
                    getData();
                }
                break;
            default:
                break;
        }
    }

    // 返回键事件
    @Override
    public void onTitleLeftPressed() {
        onBackPressed();
    }

    // 进行网络请求的方法
    private void getData() {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/EditPassWord");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/EditPassWord");
        }
        params.addBodyParameter("userID", userId + "");
        params.addBodyParameter("oldPwd", oldPassword.getText().toString().trim());
        params.addBodyParameter("newPwd", newPassword.getText().toString().trim());
        params.addBodyParameter("confirmPwd", confirmPassword.getText().toString().trim());
        params.addBodyParameter("guid", guid);
        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast(ex.getMessage());
            }

            @Override
            public void onFinished() { }

            @Override
            public void onSuccess(String arg0) {
                try{
                    JSONObject jsonObject = new JSONObject(arg0);
                    if(jsonObject.getString("Code").equals("1")){
                        toast("修改密码成功");
                        editor.putBoolean("ischeck", false);
                        editor.commit();
                        // 将SettingActivity活动结束
                        SettingActivity.instance1.finish();
                        // 将MainActivity活动结束
                        MainActivity.instance2.finish();
                        UserInfoUtils.getInstance().removeUserCache(ModifyPasswordActivity.this);
                        intent.setClass(ModifyPasswordActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (jsonObject.getString("Code").equals("0")){
                        toast("身份验证过期,请重新登陆");
                        intent.setClass(ModifyPasswordActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        toast(jsonObject.getString("Message"));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void toast(String text){
        Toast.makeText(ModifyPasswordActivity.this, text, Toast.LENGTH_SHORT).show();
    }
}
