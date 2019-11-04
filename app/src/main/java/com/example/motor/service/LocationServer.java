package com.example.motor.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.motor.constant.ConstantsField;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.SPUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class LocationServer extends Service {

    // 错误标记
    private static String TAG = "locationApplicationBeanError";
    // 时间间隔
    private static int myTime = 120 * 1000;
    private LocationClient mLocationClient; // 定位类
    /**
     * 定位的监听器
     */
    public MyLocationListenerOwn mMyLocationListener;

    private String mData, myLocation = ""; // 获取的数据
    // 定位时间间隔
    private int myLocationTime = 120 * 1000;
    // 是否启动了定位API
    private boolean isOpenLocation = false;
    // 是否启动了定位线程
    private boolean isOpenLocationTask = false;
    //保持程序唤醒
    PowerManager.WakeLock wakeLock = null;

    @Override
    public void onCreate() {
        SharedPreferences.Editor editor = getSharedPreferences("UserInfo", 0).edit();
        editor.putFloat("latitude", 0);
        editor.putFloat("longitude", 0);
        editor.apply();
        // 定位初始化
        mLocationClient = new LocationClient(this);
        mMyLocationListener = new MyLocationListenerOwn();
        mLocationClient.registerLocationListener(mMyLocationListener);
        openLocationTask();
    }

    @Override
    public void onDestroy() {
        closeLocationTask();
        super.onDestroy();
    }

    /***
     * 打开定位定时器线程
     */
    public void openLocationTask() {
        acquireWakeLock();
        try {
            if (!isOpenLocationTask) {//  如果不是打开状态，则打开线程
                startLocation();//  启动定位更新经纬度
                // 开启定时器
                initLocationTimeAndTimeTask(); //  初始化定时器和定时线程
                myLocationTimer.schedule(myLocationTimerTask, myTime, myTime);
                Log.e(TAG, " 打开了定位定时器线程 ");
                isOpenLocationTask = true; //  标记为打开了定时线程
            } else {
                Log.e(TAG, " 已经开启了定位定时器线程 ");
            }
        } catch (Exception e) {
            Log.e(TAG, "打开定位定时器线程 异常" + e.toString());
        }
    }

    /**
     * start定位
     */
    private void startLocation() {
        try {
            if (!isOpenLocation) {// 如果没有打开
                // 设置定位的相关配置
                LocationClientOption option = new LocationClientOption();
                option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
                option.setScanSpan(myLocationTime);// 时间间隔
                option.setIsNeedAddress(true);// 地址
                option.setCoorType("bd09ll"); // 设置坐标类型
                option.setOpenGps(true);
                mLocationClient.setLocOption(option);
                mLocationClient.start(); // 打开定位
                isOpenLocation = true; // 标识为已经打开了定位
            }
        } catch (Exception e) {
            Log.e(TAG, "打开定位异常" + e.toString());
        }
    }

    /**
     * 实现实位回调监听
     */
    private class MyLocationListenerOwn extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append(location.getProvince()).append("");
            currentPosition.append(location.getCity()).append("");
            currentPosition.append(location.getDistrict()).append("");
            currentPosition.append(location.getStreet()).append("");
            myLocation = currentPosition.toString();
            logMsg(location); // 调用回调函数
        }
    }


    /***
     * 初始化time对象和timetask对象
     */
    private void initLocationTimeAndTimeTask() {
        initLocationTime();
        initLocationTimeTask();
    }

    /***
     * 初始化定时器
     */
    private void initLocationTime() {
        if (myLocationTimer == null) {
            Log.e(TAG, "myLocationTimer 已经被清空了");
            myLocationTimer = new Timer();
        } else {
            Log.e(TAG, "myLocationTimer 已经存在");
        }
    }

    /***
     * 初始化 定时器线程
     */
    private void initLocationTimeTask() {
        myLocationTimerTask = new TimerTask() {
            /***
             * 定时器线程方法
             */
            @Override
            public void run() {
                handler.sendEmptyMessage(1); // 发送消息
            }
        };
    }

    /***
     * 定时器的回调函数
     */
    private Handler handler = new Handler() {
        // 更新的操作
        @Override
        public void handleMessage(Message msg) {
            getLocationInfo(); // 获取经纬度
            Log.e(TAG, "调用了获取经纬度方法");
            super.handleMessage(msg);
        }
    };

    /***
     * 获取经纬度
     */
    public void getLocationInfo() {
        /**
         * 0：正常。
         *
         * 1：SDK还未启动。
         *
         * 2：没有监听函数。
         *
         * 6：请求间隔过短。
         */
        int i = mLocationClient.requestLocation();
        String TAGfont = "getLocationInfo() : ";
        switch (i) {
            case 0:
                Log.e(TAG, TAGfont + "正常。");
                break;
            case 1:
                Log.e(TAG, TAGfont + "SDK还未启动。");
                break;
            case 2:
                Log.e(TAG, TAGfont + "没有监听函数。 ");
                break;
            case 6:
                Log.e(TAG, TAGfont + "请求间隔过短。 ");
                break;
            default:
                Log.e(TAG, TAGfont + "其他原因	");
        }
    }

    // 获取到经纬的回调操作
    private void logMsg(BDLocation str) {
        try {
            double latitude = str.getLatitude();
            double longitude = str.getLongitude();
            SharedPreferences.Editor editor = getSharedPreferences("UserInfo", 0).edit();
            editor.putFloat("latitude", (float) latitude);//用户id
            editor.putFloat("longitude", (float) longitude);//用户角色
            editor.apply();
            loadRealLoc(latitude, longitude);
            upLocation(latitude, longitude);
        } catch (Exception e) {
            Log.e(TAG, "更新操作异常" + e.toString());
        }
    }

    /***
     * 将 string 转换成 double 类型
     *
     * @param str
     * @return
     */
    private double stringToDouble(String str) {
        double v = 0.0;
        try {
            if (str != null && !str.trim().equals("")) {
                v = Double.parseDouble(str.trim());
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "string转换成double 失败 ");
        }
        return v;
    }

    /**
     * end 定位
     */
    private void closeLocation() {
        try {
            mLocationClient.stop(); // 结束定位
            isOpenLocation = false; // 标识为已经结束了定位
        } catch (Exception e) {
            Log.e(TAG, "结束定位异常" + e.toString());
        }
    }

    // 定时器
    private Timer myLocationTimer = null;
    // 定时线程
    private TimerTask myLocationTimerTask = null;

    /***
     * 销毁 time 对象 和 timetask 对象
     */
    private void destroyLocationTimeAndTimeTask() {
        myLocationTimer = null;
        myLocationTimerTask = null;
    }

    /***
     * 关闭定位定时器线程
     */
    public void closeLocationTask() {
        releaseWakeLock();
        try {
            if (isOpenLocationTask) // 如果是打开状态，则关闭
            {
                closeLocation();
                // 关闭定时器
                myLocationTimer.cancel();
                destroyLocationTimeAndTimeTask();
                Log.e(TAG, " 关闭了定位定时器线程 ");
                isOpenLocationTask = false; // 标记为关闭了定时线程
            } else {
                Log.e(TAG, " 已经关闭了定位定时器线程 ");
            }

        } catch (Exception e) {
            Log.e(TAG, "关闭定位定时器线程异常: " + e.toString());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // 获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
    private void acquireWakeLock() {
        if (null == wakeLock) {
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,
                    "PostLocationService");
            if (null != wakeLock) {
                wakeLock.acquire();
            }
        }
    }

    // 释放设备电源锁
    private void releaseWakeLock() {
        if (null != wakeLock) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    // 因为王珂的接口里面加了一个SavePosition接口，所有就将这个loadRealLoc的方法给隐藏掉，不用给
    // 百度服务器发送位置了
    private void loadRealLoc(Double latitude, Double longitude) {
        // 请求参数
        RequestParams params = new RequestParams("http://yingyan.baidu.com/api/v3/track/addpoint");
        int UserId = getSharedPreferences("UserInfo", 0).getInt("UserID", 0);
        params.addBodyParameter("ak", "vzK9L9kahlLVsjh94NjGmOrYsiIP5Ugp");
        params.addBodyParameter("service_id", "205193");
        params.addBodyParameter("entity_name", UserId + "");
        params.addBodyParameter("latitude", latitude + "");
        params.addBodyParameter("longitude", longitude + "");
        params.addBodyParameter("loc_time", System.currentTimeMillis()/1000 + "");
        params.addBodyParameter("coord_type_input", "bd09ll");
        params.addBodyParameter("mcode", "BE:E2:A3:09:0E:18:69:54:7D:03:16:44:EB:D4:73:2F:51:F4:61:57;com.example.shenzhen");
        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                Toast.makeText(x.app(), ex.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFinished() { }

            @Override
            public void onSuccess(String arg0) {
                Log.e("jsonLS", arg0);
                try {
                    JSONObject jsonObject = new JSONObject(arg0);
                    if (jsonObject.getInt("status") == 0) {
                        Log.e("==上传坐标成功==", jsonObject.getString("message"));
                    }else {
                        Log.e("==上传坐标失败==", jsonObject.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void upLocation(Double latitude, Double longitude) {
        // 出过一个异常，总是定位到几内亚湾，于是如果经纬度其中之一为"4.9E-324"的话就return，不往上汇报经纬度了。
        if (String.valueOf(latitude).equals("4.9E-324") || String.valueOf(longitude).equals("4.9E-324") ||
                myLocation.contains("null")) return;
        // 请求参数
        String address = getSharedPreferences("UserInfo", 0).getString("add", "");
        int UserId = getSharedPreferences("UserInfo", 0).getInt("UserID", 0);
        // 请求参数
        RequestParams params;
        if(TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/SavePosition");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/SavePosition");
        }
        params.addBodyParameter("UserID", String.valueOf(UserId));
        params.addBodyParameter("UpdateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        params.addBodyParameter("Lat", String.valueOf(latitude));
        params.addBodyParameter("Lng", String.valueOf(longitude));
        params.addBodyParameter("AddressMsg", myLocation);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) { }

            @Override
            public void onFinished() { }

            @Override
            public void onSuccess(String arg0) {
                Log.e("jsonLS", arg0);
                try {
                    JSONObject jsonObject = new JSONObject(arg0);
                    if (jsonObject.getInt("Code") == 1) {
                        Log.e("==上传王珂成功==", jsonObject.getString("Message"));
                    } else {
                        Log.e("==上传王珂失败==", jsonObject.getString("Message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
