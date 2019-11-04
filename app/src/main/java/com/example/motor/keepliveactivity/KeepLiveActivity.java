package com.example.motor.keepliveactivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

/**
 * 这个keepliveactivity是用来：如果此时用户通过电源键进行息屏了。可以考虑通过监听息屏和解锁的广播，
 * 在息屏的时候启动一个只有一个像素的Activity。这样的话，在息屏这段时间，应用的进程优先级很高，
 * 不容易被杀死。采用这种方案要注意的是要使用户无感知。
 *     该方案主要解决第三方应用及系统管理工具在检测到锁屏事件后一段时间（一般为5分钟以内）内会杀死后台进程，
 * 已达到省电的目的问题。
 *     为了让用户无感知，Activity要设置的小（只有一个像素），无背景并且是透明的。此外还要注意一点，
 * 需要设置Activity的taskAffinity属性，要与我们的应用默认的taskAffinity不同，否则当这个Activity启动
 * 的时候，会把我们的应用所在的任务栈移动到前台，当屏幕解锁之后，会发现我们的应用移动到前台了。而用户在
 * 息屏的时候明明已经把我们的应用切换到后台了，这会给用户造成困扰。
 *     要有一个BroadcastReceiver，用于监听屏幕的点亮和关闭的广播，在这里我们使用了
 * Intent.ACTION_USER_PRESENT这个action，它会早于系统发出的Intent.ACTION_SCREEN_OFF 广播。这样可以更
 * 早的结束之前息屏的时候启动的Activity。
 */

public class KeepLiveActivity extends Activity {

    private static final String TAG = "KeepLiveActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG,"start Keep app activity");
        Window window = getWindow();
        // 设置这个act 左上角
        window.setGravity(Gravity.START | Gravity.TOP);
        // 宽 高都为1
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = 1;
        attributes.height = 1;
        attributes.x = 0;
        attributes.y = 0;
        window.setAttributes(attributes);

        KeepLiveManager.getInstance().setKeep(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"stop keep app activity");
    }
}
