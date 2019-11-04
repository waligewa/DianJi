package com.example.motor.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.jpush.android.service.PushService;

/**
 *（18.09.19）静态注册广播。这个有个问题存在：只要是以前登录郭这个账号，只要能进入登录界面，
 * 就能收到极光推送数据。
 */
public class TestReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent pushintent = new Intent(context, PushService.class); // 启动极光推送的服务
        context.startService(pushintent);
    }
}
