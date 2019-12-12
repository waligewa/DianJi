package com.example.motor.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.motor.activity.LoginActivity;
import com.example.motor.constant.ConstantsField;
import com.example.motor.fragment.HomeFragment;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.SPUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

public class LongRunningService extends Service {

    private SharedPreferences prefs1, prefs2;
    private AlarmManager manager;
    private PendingIntent pi;
    private String equNo, address, equipmentType, guidString, userId;
    public LongRunningService() { }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        prefs1 = getSharedPreferences("device", Context.MODE_PRIVATE);
        prefs2 = getSharedPreferences("UserInfo", 0);
        address = prefs2.getString("add", "");
        guidString = prefs2.getString("guid", "");
        userId = String.valueOf(prefs2.getInt("UserID", 0));
        // 2018年6月30日，以前一直在HomeFragment中无法实现设备状态即使刷新的功能，这次可以了，原来是
        // 以前2了，就是简单的在标题赋值之后走网络请求的方法，这样就可以每次进入这个界面都进行数据的更新，
        // 以前那么呆滞，一直没有实现
        if(!prefs1.getString("comName", "首页").equals("首页")){
            // 设备状态的赋值操作
            getData();
        }
        manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 10 * 1000; // 10秒一次
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour; // trigger触发 elapsed过去
        Intent i = new Intent(this, LongRunningService.class);
        pi = PendingIntent.getService(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    // 设备状态的赋值操作
    private void getData() {
//      请求参数
        RequestParams params;
        if(TextUtils.isEmpty(SPUtils.getStringData(getApplicationContext(), ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadDianJiData");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/LoadDianJiData");
        }
        params.addBodyParameter("EquipmentID", prefs1.getString("EquipmentID", ""));
        params.addBodyParameter("UserID", userId);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {}

            @Override
            public void onError(Throwable ex, boolean arg1) {

            }

            @Override
            public void onFinished() {}

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("1")) {
                        JSONArray ja = object1.getJSONArray("Data");
                        JSONObject js = ja.getJSONObject(0);
                        JSONArray jy = js.getJSONArray("Pumplist");
                        JSONObject jt = jy.getJSONObject(0);
                        HomeFragment.influentPressure.setText(jt.getString("InPDec"));         // 进水压力
                        HomeFragment.waterPressure.setText(jt.getString("OutPDec"));           // 出水压力
                        HomeFragment.setPressure.setText(jt.getString("SetP"));                // 设定压力
                        if(!TextUtils.isEmpty(jt.getString("ScrEquipPower"))){
                            if (jt.getString("ScrEquipPower").equals("3")){
                                HomeFragment.eqPower.setText("0.37");                                //  设备功率
                            } else if (jt.getString("ScrEquipPower").equals("5")){
                                HomeFragment.eqPower.setText("0.55");                                //  设备功率
                            } else if (jt.getString("ScrEquipPower").equals("7")){
                                HomeFragment.eqPower.setText("0.75");                                //  设备功率
                            } else {
                                HomeFragment.eqPower.setText(jt.getString("ScrEquipPower"));  //  设备功率
                            }
                        }
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void toast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
