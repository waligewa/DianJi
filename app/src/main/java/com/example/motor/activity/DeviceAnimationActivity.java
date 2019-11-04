package com.example.motor.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.motor.R;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.AnalogDicInfo;
import com.example.motor.db.DeviceStateInfo;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.NetWorkUtil;
import com.example.motor.util.SPUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

public class DeviceAnimationActivity extends Activity {

    private LinearLayout back;
    private TextView title;
    private int number, TIME = 20 * 1000;
    int arg0 = 0;
    private TextView tvPressureIn, tvPressureOut, tvInstantFlow, tvTotalFlow, tvSetFlow,
            tvTotalPower, tvAmp1, tvAmp2, tvAmp3, tvAmp4, tvAmp5,
            tvFrequency1, tvFrequency2, tvFrequency3, tvFrequency4, tvFrequency5,
            tvPumpState1, tvPumpState2, tvPumpState3, tvPumpState4, tvPumpState5,
                     tvState1, tvState2, tvState3, tvState4, tvState5,
                     tvManualAuto1, tvManualAuto2, tvManualAuto3, tvManualAuto4, tvManualAuto5,
                     tvUpdateTime, tvComState;
    private AnalogDicInfo ainfo = new AnalogDicInfo();          //analog  类似物
    private List<DeviceStateInfo> mInfos = new ArrayList<DeviceStateInfo>();
    private SharedPreferences prefs1, prefs2;
    private RelativeLayout relativeLayout;
    private String gatewayAddress, text, guidString, equNo, userId;
    private Intent intent;
    private boolean flag = false;
    private boolean running = true;
    private MyAsyncTask task = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 去除标题
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 不显示系统的标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deviceanimation);
        title = (TextView) findViewById(R.id.device_animation_title);
        //返回键
        back = (LinearLayout) findViewById(R.id.device_animation_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initData() {
        prefs1 = getSharedPreferences("device", Context.MODE_PRIVATE);
        prefs2 = getSharedPreferences("UserInfo", 0);
        text = prefs1.getString("comName", getString(R.string.app_name));
        guidString = prefs2.getString("guid", "");
        equNo = prefs1.getString("EquipmentNo", "");
        userId = String.valueOf(prefs2.getInt("UserID", 0));
        title.setText(text);
        gatewayAddress = prefs2.getString("add", "");
        intent = new Intent();
    }

    private void load() {
        initData();
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadDeviceJKInfo");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadDeviceJKInfo");
        }
        params.addBodyParameter("guid", guidString);
        params.addBodyParameter("equNo", equNo);
        // 这个就是登陆账号的id
        params.addBodyParameter("userId", userId);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast("数据获取失败");
            }

            @Override
            public void onFinished() { }

            @Override
            public void onSuccess(String arg0) {
                Log.e("==设备监控返回值==", arg0);
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        intent.setClass(DeviceAnimationActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")) {
                        toast(object1.getString("Message"));
                        JSONObject jsonObject1 = object1.getJSONObject("Equipments");
                        // 此接口没有ID、更新时间、设备ID、是否在线、在线状态
                        ainfo.setPressureIN(jsonObject1.get("PressureIN").toString());
                        ainfo.setPressureOut(jsonObject1.get("PressureOut").toString());
                        ainfo.setPressureSet(jsonObject1.get("PressureSet").toString());      // 设定压力
                        ainfo.setInstantFlow1(jsonObject1.get("InstantFlow1").toString());
                        ainfo.setTotalFlow1(jsonObject1.get("TotalFlow1").toString());
                        if(jsonObject1.has("PressureSet")){
                            flag = true;
                        } else {
                            flag = false;
                        }
                        ainfo.setSetFlow(jsonObject1.get("SetFlow").toString());              // 设定流量
                        ainfo.setTotalPower(jsonObject1.get("TotalPower").toString());
                        ainfo.setUpdateTime(object1.get("UpdateTime").toString());
                        ainfo.setIsOnline(object1.getBoolean("IsOnline"));
                        JSONArray jsonArray1 = new JSONArray(object1.getString("Pump"));
                        // 获得水泵数量
                        ainfo.setPumpNum(jsonArray1.length());
                        number = ainfo.getPumpNum();
                        // 遍历方法这次的i是1，下面的getJSONObject就得变为i-1，因为数组的下标是从0开始的
                        // 下面的语句只要有一条是null，就直接报异常出去，不会往下执行了
                        mInfos.clear();
                        for (int i = 1; i <= jsonArray1.length(); i++) {
                            JSONObject jsonObject2 = jsonArray1.getJSONObject(i - 1);
                            DeviceStateInfo info = new DeviceStateInfo();
                            info.setIdString(String.valueOf(i));
                            info.setNameString(i + "号水泵运行状态");
                            String s = jsonObject2.getString("PState");
                            info.setRunStateString(jsonObject2.getString("PState"));  // 手动自动
                            if(jsonObject2.getString("PFault").equals("null")){
                                info.setControlStateString("");  // 变频或者停止或者工频
                            } else {
                                info.setControlStateString(jsonObject2.getString("PFault"));  // 变频或者停止或者工频
                            }
                            info.setElectricString(jsonObject2.getString("Electric"));
                            info.setFrequencyString(jsonObject2.getString("Frequency"));
                            mInfos.add(info);
                        }
                        initView();
                    } else {
                        toast(object1.getString("Message"));
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    private void initView() {
        findViewById(R.id.linear_beng1).setVisibility(View.GONE); // 2号泵
        findViewById(R.id.linear_beng2).setVisibility(View.GONE); // 3号泵
        findViewById(R.id.linear_beng3).setVisibility(View.GONE); // 4号泵
        findViewById(R.id.linear_beng4).setVisibility(View.GONE); // 5号泵
        findViewById(R.id.device_State3).setVisibility(View.GONE);// 3号泵状态
        findViewById(R.id.device_State4).setVisibility(View.GONE);// 4号泵状态
        findViewById(R.id.device_State5).setVisibility(View.GONE);// 5号泵状态
        // 这个也是一个非空判断，可以这么写
        if (number > 0) {
            switch (number) {
                case 2:
                    findViewById(R.id.linear_beng1).setVisibility(View.VISIBLE);
                    relativeLayout = (RelativeLayout) findViewById(R.id.linear_beng1);
                    break;
                // 3泵的时候附上背景图片
                case 3:
                    findViewById(R.id.linear_beng2).setVisibility(View.VISIBLE);
                    relativeLayout = (RelativeLayout) findViewById(R.id.linear_beng2);
                    relativeLayout.setBackgroundResource(R.drawable.device_3);
                    break;
                case 4:
                    // 这个没有setBackgroundResource。加setBackgroundResource目的是为了防止oom
                    findViewById(R.id.linear_beng3).setVisibility(View.VISIBLE);
                    relativeLayout = (RelativeLayout) findViewById(R.id.linear_beng3);
                    break;
                // 5泵的时候附上背景图片
                case 5:
                    findViewById(R.id.linear_beng4).setVisibility(View.VISIBLE);
                    relativeLayout = (RelativeLayout) findViewById(R.id.linear_beng4);
                    relativeLayout.setBackgroundResource(R.drawable.device_5);
                    break;
                default:
                    break;
            }
        }
        tvPressureIn = (TextView) findViewById(R.id.tvPressureIn);
        tvPressureOut = (TextView) findViewById(R.id.tvPressureOut);
        // 18-09-25隐藏掉设定压力
        //tvPressureSet = (TextView) findViewById(R.id.tvPressureSet);
        tvInstantFlow = (TextView) findViewById(R.id.tvInstantFlow);  // 瞬时流量
        tvTotalFlow = (TextView) findViewById(R.id.tvTotalFlow);  // 累计流量
        tvSetFlow = (TextView) findViewById(R.id.set_flow);  // 设定流量
        tvTotalPower = (TextView) findViewById(R.id.tvTotalPower);  // 累计电量
        if (number > 0) {
            tvAmp1 = (TextView) relativeLayout.findViewById(R.id.tvAmp1);
            tvFrequency1 = (TextView) relativeLayout.findViewById(R.id.tvFrequency1);
            tvPumpState1 = (TextView) relativeLayout.findViewById(R.id.tvPumpState1);  // 手动自动
            tvManualAuto1 = (TextView) findViewById(R.id.tvManualAuto1);  // 变频停止，用不着加relativeLayout，其他三个需要relativeLayout

            tvAmp2 = (TextView) relativeLayout.findViewById(R.id.tvAmp2);
            tvFrequency2 = (TextView) relativeLayout.findViewById(R.id.tvFrequency2);
            tvPumpState2 = (TextView) relativeLayout.findViewById(R.id.tvPumpState2);  // 手动自动
            tvManualAuto2 = (TextView) findViewById(R.id.tvManualAuto2);  // 变频停止，用不着加relativeLayout，其他三个需要relativeLayout
        }
        // 3泵的电流、频率、水泵状态、手动自动的初始化
        if (number > 2) {
            findViewById(R.id.device_State3).setVisibility(View.VISIBLE);
            tvAmp3 = (TextView) relativeLayout.findViewById(R.id.tvAmp3);
            tvFrequency3 = (TextView) relativeLayout.findViewById(R.id.tvFrequency3);
            tvPumpState3 = (TextView) relativeLayout.findViewById(R.id.tvPumpState3);  // 手动自动
            tvManualAuto3 = (TextView) findViewById(R.id.tvManualAuto3);  // 变频停止，用不着加relativeLayout，其他三个需要relativeLayout
        }
        // 4泵的电流、频率、水泵状态、手动自动的初始化
        if (number > 3) {
            findViewById(R.id.device_State4).setVisibility(View.VISIBLE);
            tvAmp4 = (TextView) relativeLayout.findViewById(R.id.tvAmp4);
            tvFrequency4 = (TextView) relativeLayout.findViewById(R.id.tvFrequency4);
            tvPumpState4 = (TextView) relativeLayout.findViewById(R.id.tvPumpState4);  // 手动自动
            tvManualAuto4 = (TextView) findViewById(R.id.tvManualAuto4);  // 变频停止，用不着加relativeLayout，其他三个需要relativeLayout
        }
        // 5泵的电流、频率、水泵状态、手动自动的初始化
        if (number > 4) {
            findViewById(R.id.device_State5).setVisibility(View.VISIBLE);
            tvAmp5 = (TextView) relativeLayout.findViewById(R.id.tvAmp5);
            tvFrequency5 = (TextView) relativeLayout.findViewById(R.id.tvFrequency5);
            tvPumpState5 = (TextView) relativeLayout.findViewById(R.id.tvPumpState5);  // 手动自动
            tvManualAuto5 = (TextView) findViewById(R.id.tvManualAuto5);  // 变频停止，用不着加relativeLayout，其他三个需要relativeLayout
        }
        tvUpdateTime = (TextView) findViewById(R.id.tvUpdateTime);  // 数据时间
        tvComState = (TextView) findViewById(R.id.tvComState);  // 通讯状态
        // 填充获取的数据
        setDeviceData();  // 对象已经初始化了，就要开始赋值了
    }

    // 填充获取的数据
    private void setDeviceData() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.round_loading);
        tvPressureIn.setText("进水压力：" + ainfo.getPressureIN() + "MPa");
        tvPressureOut.setText("出水压力：" + ainfo.getPressureOut() + "MPa");
        // 18-09-25隐藏掉设定压力
        //tvPressureSet.setText("设定压力：" + ainfo.getPressureSet() + "MPa");
        tvInstantFlow.setText("瞬时流量：" + ainfo.getInstantFlow1() + "m³/h"); // 瞬时流量:0.00m³/h
        tvTotalFlow.setText("累计流量：" + ainfo.getTotalFlow1() + "m³"); // 累计流量:0.00m³
        if(flag){
            tvSetFlow.setText("设定压力：" + ainfo.getPressureSet() + "MPa"); // 设定压力MPa
        } else {
            tvSetFlow.setText("设定流量：" + ainfo.getSetFlow() + "m³/h"); // 设定流量m³/h
        }
        tvTotalPower.setText("累计电量：" + ainfo.getTotalPower() + "kW·h");
        if(ainfo.getUpdateTime().contains(".")){
            tvUpdateTime.setText("数据时间：" + ainfo.getUpdateTime().replace("T", " ").split("\\.")[0]);
        } else {
            tvUpdateTime.setText("数据时间：" + ainfo.getUpdateTime().replace("T", " ").substring(0, 19));
        }
        if (ainfo.getIsOnline()) {
            tvComState.setText("在线");
        } else {
            tvComState.setText("离线");
        }
        if (number > 0) {
            // 第一个水泵的数据
            if (mInfos.size() > 0) {
                tvAmp1.setText(mInfos.get(0).getElectricString() + "A");
                tvFrequency1.setText(mInfos.get(0).getFrequencyString() + "Hz");
                tvManualAuto1.setText(mInfos.get(0).getControlStateString());  // 变频停止
                tvPumpState1.setText(mInfos.get(0).getRunStateString());  // 手动自动
                if (mInfos.get(0).getControlStateString().contains("停止") || mInfos.get(0).getFrequencyString().equals("0")) {
                    relativeLayout.findViewById(R.id.ivPump1).clearAnimation();
                } else {
                    relativeLayout.findViewById(R.id.ivPump1).startAnimation(anim);
                }
            }
            // 第二个水泵的数据
            if (mInfos.size() > 1) {
                tvAmp2.setText(mInfos.get(1).getElectricString() + "A");
                tvFrequency2.setText(mInfos.get(1).getFrequencyString() + "Hz");
                tvManualAuto2.setText(mInfos.get(1).getControlStateString());  // 变频停止
                tvPumpState2.setText(mInfos.get(1).getRunStateString());  // 手动自动
                if (mInfos.get(1).getControlStateString().contains("停止") || mInfos.get(1).getFrequencyString().equals("0")) {
                    relativeLayout.findViewById(R.id.ivPump2).clearAnimation();
                } else {
                    relativeLayout.findViewById(R.id.ivPump2).startAnimation(anim);
                }
            }
        }
        if (number > 2) {
            // 第三个水泵的数据
            if (mInfos.size() > 2) {
                tvAmp3.setText(mInfos.get(2).getElectricString() + "A");
                tvFrequency3.setText(mInfos.get(2).getFrequencyString() + "Hz");
                tvManualAuto3.setText(mInfos.get(2).getControlStateString());  // 变频停止
                tvPumpState3.setText(mInfos.get(2).getRunStateString());  // 手动自动
                if (mInfos.get(2).getControlStateString().contains("停止") || mInfos.get(2).getFrequencyString().equals("0")) {
                    relativeLayout.findViewById(R.id.ivPump3).clearAnimation();
                } else {
                    relativeLayout.findViewById(R.id.ivPump3).startAnimation(anim);
                }
            }
        }
        if (number > 3) {
            // 第四个水泵的数据
            if (mInfos.size() > 3) {
                tvAmp4.setText(mInfos.get(3).getElectricString() + "A");
                tvFrequency4.setText(mInfos.get(3).getFrequencyString() + "Hz");
                tvManualAuto4.setText(mInfos.get(3).getControlStateString());  // 变频停止
                tvPumpState4.setText(mInfos.get(3).getRunStateString());  // 手动自动
                if (mInfos.get(3).getControlStateString().contains("停止") || mInfos.get(3).getFrequencyString().equals("0")) {
                    relativeLayout.findViewById(R.id.ivPump4).clearAnimation();
                } else {
                    relativeLayout.findViewById(R.id.ivPump4).startAnimation(anim);
                }
            }
        }
        if (number > 4) {
            // 第五个水泵的数据
            if (mInfos.size() > 4) {
                tvAmp5.setText(mInfos.get(4).getElectricString() + "A");
                tvFrequency5.setText(mInfos.get(4).getFrequencyString() + "Hz");
                tvManualAuto5.setText(mInfos.get(4).getControlStateString());  // 变频停止
                tvPumpState5.setText(mInfos.get(4).getRunStateString());  // 手动自动
                if (mInfos.get(4).getControlStateString().contains("停止") || mInfos.get(4).getFrequencyString().equals("0")) {
                    relativeLayout.findViewById(R.id.ivPump5).clearAnimation();
                } else {
                    relativeLayout.findViewById(R.id.ivPump5).startAnimation(anim);
                }
            }
        }
    }

    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            while (running) {
                try {
                    publishProgress();                   // 类似于给主线程发消息，通知更新UI
                    Thread.sleep(TIME);
                } catch (InterruptedException e) {       // interrupt  打断
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if (!NetWorkUtil.isNetworkConnected(getApplicationContext())) {
                toast("网络连接错误，请检查网络...");
            } else {
                load();
            }
            super.onProgressUpdate(values);
        }
    }

    private void startTask() {
        stopTask();
        running = true;
        task = (MyAsyncTask) new MyAsyncTask().execute();
    }

    private void stopTask() {
        if (task != null) {
            running = false;
            task.cancel(true);
            task = null;
        }
    }

    // 对Toast进行一下封装
    private void toast(String text) {
        Toast.makeText(DeviceAnimationActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        startTask();
        super.onStart();
    }

    @Override
    protected void onStop() {
        stopTask();
        super.onStop();
    }
}
