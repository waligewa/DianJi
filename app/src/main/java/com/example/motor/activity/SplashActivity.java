package com.example.motor.activity;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import com.example.motor.R;
import com.example.motor.base.BaseActivity;
import com.example.motor.base.PageBaseParams;
import com.example.motor.constant.ConstantsField;
import com.example.motor.util.SPUtils;
import com.example.motor.util.UserInfoUtils;

/**
 * Author : yanftch
 * Date   : 2018/3/10
 * Time   : 20:20
 * Desc   : 启动页
 *
 */

public class SplashActivity extends BaseActivity {
    private static final String TAG = "SplashActivity";
    private SplashHandler mSplashHandler;
    private int what;

    public static class Params extends PageBaseParams {
        private static final int WHAT_LOGIN = 1;
        private static final int WHAT_MAIN = 2;
    }

    @Override
    public int setLayout() {
        if(Build.VERSION.SDK_INT > 18){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        return R.layout.activity_splash;
    }

    @Override
    public void setTitle() {
        mBaseTitleBarView.setVisibility(View.GONE);
    }

    @Override
    public void initWidget() {
        mSplashHandler = new SplashHandler();
        // 下面这句代码所得的值永远都为网关地址，也就是说永远都不为空字符串，也就是说下面的判断的后者
        // 永远为true
        String data = SPUtils.getStringData(SplashActivity.this, ConstantsField.ADDRESS);
        // 对于这个判断的第一个判断条件，顺着isLogin方法有详细说明，可以去查阅
        if (UserInfoUtils.getInstance().isLogin(this) && !TextUtils.isEmpty(data)) {
            // 2018年6月17日之前一直都是直接进入MainActivity，但是由于存在guid，总是会长时间等待
            // （大约4秒5秒）返回到登录界面，所以这样的自动登录就没有意义了。因此就直接到登录界面了。
            //what = Params.WHAT_MAIN;
            what = Params.WHAT_LOGIN;
        } else {
            what = Params.WHAT_LOGIN;
        }
        mSplashHandler.sendEmptyMessageDelayed(what, 1000);
    }

    @Override
    public void widgetClick(View v) {}

    private class SplashHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Params.WHAT_LOGIN:
                    startActivityAndFinish(LoginActivity.class);
                    break;
                case Params.WHAT_MAIN:
                    startActivityAndFinish(MainActivity.class);
                    break;
                default:
                    break;
            }
        }
    }
}
