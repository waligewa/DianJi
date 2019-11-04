package com.example.motor.doubleprocess;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import motor.IMyAidlInterface;

/**
 * 双进程守护。
 *     我们都知道Service可以以bind方式启动，当Service被系统杀死的时候，会在
 * ServiceConnection的onServiceDisconnected方法中会收到回调。利用这个原理，可以在主进程中进行有一个
 * LocalService，在子进程中有RemoteService。LocalService中以bind和start方式启动RemoteService，
 * 同时RemoteService以bind和start方式启动LocalService。并且在它们各自的ServiceConnection的
 * onServiceDisconnected方法中重新bind和start。这种Java层通过Service这种双进程守护的方式，可以有效的
 * 保证进程的存活能力。
 *     为了提高Service所在进程的优先级，可以结合我们之前讲的startForground来开启一个
 * Notification的方式，提高进程的优先级，以降低被杀风险。
 *
 */
public class LocalService extends Service {

    private final static int NOTIFICATION_ID = 1003;
    private static final String TAG = "LocalService";
    private ServiceConnection serviceConnection;

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Notification notification = new Notification();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                // 10月16日开启
                //startForeground(NOTIFICATION_ID, notification);
            } else {
                //startForeground(NOTIFICATION_ID, notification);
                // start InnerService
                startService(new Intent(this, InnerService.class));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        serviceConnection = new LocalServiceConnection();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    class LocalServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 服务连接后回调
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "remote service died，make it alive");
            // 连接中断后回调
            startService(new Intent(LocalService.this, RemoteService.class));
            // 重新绑定
            bindService(new Intent(LocalService.this, RemoteService.class),
                    serviceConnection, BIND_AUTO_CREATE);
        }
    }


    public static class InnerService extends Service {

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onCreate() {
            super.onCreate();
            try {
                startForeground(NOTIFICATION_ID, new Notification());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            stopSelf();
        }

        @Override
        public void onDestroy() {
            stopForeground(true);
            super.onDestroy();
        }
    }

    static class MyBinder extends IMyAidlInterface.Stub {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
                               double aDouble, String aString) throws RemoteException { }
    }
}
