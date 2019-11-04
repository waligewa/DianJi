package com.example.motor.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.constant.ConstantsField;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DoubleClickUtils;
import com.example.motor.util.SPUtils;

import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

public class GatewayAddressActivity1 extends Activity {

    @ViewInject(R.id.gatewayaddress)
    EditText gatewayAddress;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Intent intent;
    private String mAddress;

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.gateway_address);
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);
        init();
    }

    private void init(){
        prefs = this.getSharedPreferences("UserInfo", 0);  //  创建一个数据库
        editor = prefs.edit();
        mAddress = prefs.getString("add", "");
        intent = new Intent();
        // selection方法是用来设置光标位置的
        gatewayAddress.setSelection(gatewayAddress.getText().toString().trim().length());
        if(!mAddress.equals("")){
            gatewayAddress.setText(mAddress);
            gatewayAddress.setSelection(gatewayAddress.getText().toString().trim().length());
        }
    }

    @Event(value = { R.id.restoredefaults, R.id.determine, R.id.back },
            type = View.OnClickListener.class)
    private void btnClick(View v){
        switch (v.getId()){
            // 回复默认按钮,关于默认网关地址有两处地方要改，一个是CommonUrl，一个是这个活动的xml文件
            case R.id.restoredefaults:
                if(!DoubleClickUtils.isFastDoubleClick()){
                    gatewayAddress.setText(CommenUrl.iPSetString);
                    gatewayAddress.setSelection(CommenUrl.iPSetString.length());
                }
                break;
            // 确认按钮
            case R.id.determine:
                if(!DoubleClickUtils.isFastDoubleClick()){
                    if(!gatewayAddress.getText().toString().trim().equals("")){
                        String address = gatewayAddress.getText().toString().trim();
                        editor.putString("add", address);
                        // 这句代码的意思就是在cache偏好文件里面保存key为address，value为网关地址
                        SPUtils.saveStringData(GatewayAddressActivity1.this, ConstantsField.ADDRESS, address);
                        editor.apply();
                        intent.setClass(GatewayAddressActivity1.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        Toast.makeText(GatewayAddressActivity1.this, "设置成功，请重新登录",
                                Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(GatewayAddressActivity1.this, "请输入网关地址", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.back:
                intent.setClass(GatewayAddressActivity1.this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            default:
                break;
        }
    }

    // 手机返回按钮返回功能
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                intent.setClass(GatewayAddressActivity1.this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }
}
