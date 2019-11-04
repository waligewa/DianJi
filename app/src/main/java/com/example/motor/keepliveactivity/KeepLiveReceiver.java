package com.example.motor.keepliveactivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

/**
 * 要有一个BroadcastReceiver，用于监听屏幕的点亮和关闭的广播，在这里我们使用了
 * Intent.ACTION_USER_PRESENT这个action，它会早于系统发出的Intent.ACTION_SCREEN_OFF 广播。
 * 这样可以更早的结束之前息屏的时候启动的Activity。
 */

public class KeepLiveReceiver extends BroadcastReceiver {
    private static final String TAG = "KeepLiveReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e(TAG, "receive action:" + action);
        // 屏幕关闭事件
        if (TextUtils.equals(action, Intent.ACTION_SCREEN_OFF)) {
            // 关屏 开启1px activity
            KeepLiveManager.getInstance().startKeepLiveActivity(context);
            // 解锁事件
        } else if (TextUtils.equals(action, Intent.ACTION_USER_PRESENT)) {
            KeepLiveManager.getInstance().finishKeepLiveActivity();
        }

        KeepLiveManager.getInstance().startKeepLiveService(context);
    }
}
