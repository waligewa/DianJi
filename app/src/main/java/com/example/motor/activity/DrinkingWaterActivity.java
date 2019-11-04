package com.example.motor.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.constant.ConstantsField;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.NetWorkUtil;
import com.example.motor.util.SPUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

public class DrinkingWaterActivity extends AppCompatActivity {

    @ViewInject(R.id.update_time)
    TextView updateTime;
    @ViewInject(R.id.ic_a_title)
    TextView mtitleTextView;
    @ViewInject(R.id.origin_box_level)
    TextView originBoxLevel; // 原水箱液位
    @ViewInject(R.id.origin_box_level_isno)
    TextView originBoxLevelIsno; // 原水箱液位是否正常
    @ViewInject(R.id.clean_box_level)
    TextView cleanBoxLevel; // 净水箱液位
    @ViewInject(R.id.clean_box_level_isno)
    TextView cleanBoxLevelIsno; // 净水箱液位是否正常
    @ViewInject(R.id.origin_water_pressure)
    TextView originWaterPressure; // 原出水压力
    @ViewInject(R.id.origin_water_pressure_isno)
    TextView originWaterPressureIsno; // 原出水压力是否正常
    @ViewInject(R.id.clean_setting_pressure)
    TextView cleanSettingPressure; // 净设定压力
    @ViewInject(R.id.clean_setting_pressure_isno)
    TextView cleanSettingPressureIsno; // 净设定压力是否正常
    @ViewInject(R.id.clean_out_pressure)
    TextView cleanOutPressure; // 净出水压力
    @ViewInject(R.id.clean_out_pressure_isno)
    TextView cleanOutPressureIsno; // 净出水压力是否正常
    @ViewInject(R.id.conductivity)
    TextView conductivity; // 电导率
    @ViewInject(R.id.conductivity_isno)
    TextView conductivityIsno; // 电导率是否正常
    @ViewInject(R.id.ph_value)
    TextView phValue; // ph值
    @ViewInject(R.id.ph_value_isno)
    TextView phValueIsno; // ph值是否正常
    @ViewInject(R.id.residual_chlorine)
    TextView residualChlorine; // 余氯
    @ViewInject(R.id.residual_chlorine_isno)
    TextView residualChlorineIsno; // 余氯是否正常
    @ViewInject(R.id.turbidity)
    TextView turbidity; // 浊度
    @ViewInject(R.id.turbidity_isno)
    TextView turbidityIsno; // 浊度是否正常
    @ViewInject(R.id.orp_value)
    TextView orpValue; // orp的值
    @ViewInject(R.id.orp_value_isno)
    TextView orpValueIsno; // orp的值是否正常
    @ViewInject(R.id.salinity)
    TextView salinity; // 盐度
    @ViewInject(R.id.salinity_isno)
    TextView salinityIsno; // 盐度是否正常
    @ViewInject(R.id.dissolved_oxygen)
    TextView dissolvedOxygen; // 溶解氧
    @ViewInject(R.id.dissolved_oxygen_isno)
    TextView dissolvedOxygenIsno; // 溶解氧是否正常
    @ViewInject(R.id.water_quality)
    TextView waterQuality; // 水质硬度
    @ViewInject(R.id.water_quality_isno)
    TextView waterQualityIsno; // 水质硬度是否正常

    @ViewInject(R.id.run_state1)
    TextView runState1; // 1#原水泵的运行状态
    @ViewInject(R.id.frequency1)
    TextView frequency1; // 1#原水泵的变频频率
    @ViewInject(R.id.ampere1)
    TextView ampere1; // 1#原水泵的电流
    /*@ViewInject(R.id.state1)
    TextView state1; // 1#原水泵的状态*/
    @ViewInject(R.id.run_state2)
    TextView runState2; // 2#原水泵的运行状态
    @ViewInject(R.id.frequency2)
    TextView frequency2; // 2#原水泵的变频频率
    @ViewInject(R.id.ampere2)
    TextView ampere2; // 2#原水泵的电流
    /*@ViewInject(R.id.state2)
    TextView state2; // 2#原水泵的状态*/
    @ViewInject(R.id.run_state3)
    TextView runState3; // 高压泵的运行状态
    @ViewInject(R.id.frequency3)
    TextView frequency3; // 高压泵的变频频率
    @ViewInject(R.id.ampere3)
    TextView ampere3; // 高压泵的电流
    /*@ViewInject(R.id.state3)
    TextView state3; // 高压泵的状态*/
    @ViewInject(R.id.run_state4)
    TextView runState4; // 1#净水泵的运行状态
    @ViewInject(R.id.frequency4)
    TextView frequency4; // 1#净水泵的变频频率
    @ViewInject(R.id.ampere4)
    TextView ampere4; // 1#净水泵的电流
    /*@ViewInject(R.id.state4)
    TextView state4; // 1#净水泵的状态*/
    @ViewInject(R.id.run_state5)
    TextView runState5; // 2#净水泵的运行状态
    @ViewInject(R.id.frequency5)
    TextView frequency5; // 2#净水泵的变频频率
    @ViewInject(R.id.ampere5)
    TextView ampere5; // 2#净水泵的电流
    /*@ViewInject(R.id.state5)
    TextView state5; // 2#净水泵的状态*/

    @ViewInject(R.id.clean_pump_state)
    TextView cleanPumpState; // 清洗泵状态
    @ViewInject(R.id.inlet_valve_state)
    TextView inletValveState; // 进水阀状态
    @ViewInject(R.id.bypass_valve_condition)
    TextView bypassValveState; // 旁通阀状态
    @ViewInject(R.id.high_pressure_valve_state)
    TextView highPressureValveState; // 高压阀状态
    @ViewInject(R.id.concentrated_water_valve_state)
    TextView concentratedWaterValveState; // 浓水阀状态
    @ViewInject(R.id.return_valve_state)
    TextView returnValveState; // 回水阀状态
    private Activity mActivity;
    private Intent intent;
    private SharedPreferences prefs1, prefs2;
    private MyAsyncTask task = null;
    private boolean running = true;
    private String equNo, address, guidString, userId;
    private Animation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drinking_water);
        MyApplication.getInstance().addActivity(this);
        x.view().inject(this);

        init();
        // 设备状态的赋值操作
        getData();
    }

    private void init(){
        mActivity = this;
        intent = new Intent();
        prefs1 = getSharedPreferences("UserInfo", 0);
        prefs2 = getSharedPreferences("device", Context.MODE_PRIVATE);
        address = prefs1.getString("add", "");
        guidString = prefs1.getString("guid", "");
        userId = String.valueOf(prefs1.getInt("UserID", 0));
        mtitleTextView.setText(prefs2.getString("comName", "设备状态").trim());
        alphaAnimation.setDuration(2000);
        alphaAnimation.setRepeatCount(100000);
        alphaAnimation.setInterpolator(new LinearInterpolator());

        /*Animation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setDuration(2000);
        alphaAnimation.setRepeatCount(100000);
        alphaAnimation.setInterpolator(new LinearInterpolator());
        cleanOutPressureIsno.startAnimation(alphaAnimation);
        conductivityIsno.startAnimation(alphaAnimation);
        phValueIsno.startAnimation(alphaAnimation);
        turbidityIsno.startAnimation(alphaAnimation);
        orpValueIsno.startAnimation(alphaAnimation);
        dissolvedOxygenIsno.startAnimation(alphaAnimation);*/
    }

    @Event(value = { R.id.iv_back }, type = View.OnClickListener.class)
    private void btnClick(View v){
        switch (v.getId()){
            case R.id.iv_back:
                finish();
                break;
        }
    }

    public class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            while (running) {
                try {
                    publishProgress();  // 类似于给主线程发消息，通知更新UI
                    Thread.sleep(5000);
                } catch (InterruptedException e) {  // interrupt  打断
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            if (!NetWorkUtil.isNetworkConnected(getApplicationContext())) {
                toast("网络连接错误，请检查网络...");
            } else {
                originBoxLevelIsno.clearAnimation();
                cleanBoxLevelIsno.clearAnimation();
                originWaterPressureIsno.clearAnimation();
                cleanSettingPressureIsno.clearAnimation();
                cleanOutPressureIsno.clearAnimation();
                conductivityIsno.clearAnimation();
                phValueIsno.clearAnimation();
                residualChlorineIsno.clearAnimation();
                turbidityIsno.clearAnimation();
                orpValueIsno.clearAnimation();
                salinityIsno.clearAnimation();
                dissolvedOxygenIsno.clearAnimation();
                getData(); // 为主线程 更新UI
                //toast("AAA");
            }
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

    // 设备状态的赋值操作
    private void getData() {
        // 请求参数
        equNo = prefs2.getString("EquipmentNo", "");
        if(equNo.equals("")) return;
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadDeviceJKInfo");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/LoadDeviceJKInfo");
        }
        params.addBodyParameter("guid", guidString);
        params.addBodyParameter("equNo", equNo);
        params.addBodyParameter("userId", userId);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {}

            @Override
            public void onError(Throwable ex, boolean arg1) {
                //toast(ex.getMessage());
                //toast("内部服务器出现错误");
            }

            @Override
            public void onFinished() {}

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        Intent intent = new Intent(mActivity, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    }
                    if (object1.getString("Code").equals("1")) {
                        updateTime.setText("数据更新时间为：" + object1.getString("UpdateTime").replace("T", " ").split("\\.")[0]);
                        // 这是设备状态的数据
                        String[] array = object1.getString("Equipmentstate")
                                .replace("[", "")
                                .replace("]", "")
                                .split("\\,");
                        originBoxLevel.setText(array[0] + "米");
                        originBoxLevelIsno.setText("液位正常");
                        originBoxLevelIsno.setBackgroundResource(R.color.green);
                        /*if(Double.valueOf(array[0]) < 0.5){
                            originBoxLevelIsno.setText("液位偏低");
                            originBoxLevelIsno.setBackgroundResource(R.color.common_red);
                            originBoxLevelIsno.startAnimation(alphaAnimation);
                        } else if (Double.valueOf(array[0]) > 1){
                            originBoxLevelIsno.setText("液位偏高");
                            originBoxLevelIsno.setBackgroundResource(R.color.common_red);
                            originBoxLevelIsno.startAnimation(alphaAnimation);
                        } else {
                            originBoxLevelIsno.setText("液位正常");
                            originBoxLevelIsno.setBackgroundResource(R.color.green);
                        }*/
                        cleanBoxLevel.setText(array[1] + "米");
                        cleanBoxLevelIsno.setText("液位正常");
                        cleanBoxLevelIsno.setBackgroundResource(R.color.green);
                        /*if(Double.valueOf(array[1]) < 0.5){
                            cleanBoxLevelIsno.setText("液位偏低");
                            cleanBoxLevelIsno.setBackgroundResource(R.color.common_red);
                            cleanBoxLevelIsno.startAnimation(alphaAnimation);
                        } else if (Double.valueOf(array[1]) > 1){
                            cleanBoxLevelIsno.setText("液位偏高");
                            cleanBoxLevelIsno.setBackgroundResource(R.color.common_red);
                            cleanBoxLevelIsno.startAnimation(alphaAnimation);
                        } else {
                            cleanBoxLevelIsno.setText("液位正常");
                            cleanBoxLevelIsno.setBackgroundResource(R.color.green);
                        }*/
                        originWaterPressure.setText(array[2] + " MPa");
                        originWaterPressureIsno.setText("压力正常");
                        originWaterPressureIsno.setBackgroundResource(R.color.green);
                        /*if(Double.valueOf(array[2]) < 0.5){
                            originWaterPressureIsno.setText("压力偏低");
                            originWaterPressureIsno.setBackgroundResource(R.color.common_red);
                            originWaterPressureIsno.startAnimation(alphaAnimation);
                        } else if (Double.valueOf(array[2]) > 1){
                            originWaterPressureIsno.setText("压力偏高");
                            originWaterPressureIsno.setBackgroundResource(R.color.common_red);
                            originWaterPressureIsno.startAnimation(alphaAnimation);
                        } else {
                            originWaterPressureIsno.setText("压力正常");
                            originWaterPressureIsno.setBackgroundResource(R.color.green);
                        }*/
                        cleanSettingPressure.setText(array[3] + " MPa");
                        cleanSettingPressureIsno.setText("压力正常");
                        cleanSettingPressureIsno.setBackgroundResource(R.color.green);
                        /*if(Double.valueOf(array[3]) < 0.5){
                            cleanSettingPressureIsno.setText("压力偏低");
                            cleanSettingPressureIsno.setBackgroundResource(R.color.common_red);
                            cleanSettingPressureIsno.startAnimation(alphaAnimation);
                        } else if (Double.valueOf(array[3]) > 1){
                            cleanSettingPressureIsno.setText("压力偏高");
                            cleanSettingPressureIsno.setBackgroundResource(R.color.common_red);
                            cleanSettingPressureIsno.startAnimation(alphaAnimation);
                        } else {
                            cleanSettingPressureIsno.setText("压力正常");
                            cleanSettingPressureIsno.setBackgroundResource(R.color.green);
                        }*/
                        cleanOutPressure.setText(array[4] + " MPa");
                        cleanOutPressureIsno.setText("压力正常");
                        cleanOutPressureIsno.setBackgroundResource(R.color.green);
                        /*if (Double.valueOf(array[4]) < 0.5){
                            cleanOutPressureIsno.setText("压力偏低");
                            cleanOutPressureIsno.setBackgroundResource(R.color.common_red);
                            cleanOutPressureIsno.startAnimation(alphaAnimation);
                        } else if (Double.valueOf(array[4]) > 1){
                            cleanOutPressureIsno.setText("压力偏高");
                            cleanOutPressureIsno.setBackgroundResource(R.color.common_red);
                            cleanOutPressureIsno.startAnimation(alphaAnimation);
                        } else {
                            cleanOutPressureIsno.setText("压力正常");
                            cleanOutPressureIsno.setBackgroundResource(R.color.green);
                        }*/
                        // 电导率
                        conductivity.setText(array[5] + " us/cm");
                        if (Double.valueOf(array[5]) <= 50.0){
                            conductivityIsno.setText("正常");
                            conductivityIsno.setBackgroundResource(R.color.green);
                        } else {
                            conductivityIsno.setText("偏高");
                            conductivityIsno.setBackgroundResource(R.color.common_red);
                            conductivityIsno.startAnimation(alphaAnimation);
                        }
                        // PH值
                        phValue.setText(array[6]);
                        if (Double.valueOf(array[6]) < 6.0){
                            phValueIsno.setText("偏低");
                            phValueIsno.setBackgroundResource(R.color.common_red);
                            phValueIsno.startAnimation(alphaAnimation);
                        } else if (Double.valueOf(array[6]) <= 8.5){
                            phValueIsno.setText("正常");
                            phValueIsno.setBackgroundResource(R.color.green);
                        } else if (Double.valueOf(array[6]) <= 14){
                            phValueIsno.setText("偏高");
                            phValueIsno.setBackgroundResource(R.color.common_red);
                            phValueIsno.startAnimation(alphaAnimation);
                        }
                        // 余氯
                        residualChlorine.setText(array[7] + " mg/L");
                        if (Double.valueOf(array[7]) <= 0.010) {
                            residualChlorineIsno.setText("正常");
                            residualChlorineIsno.setBackgroundResource(R.color.green);
                        } else {
                            residualChlorineIsno.setText("偏高");
                            residualChlorineIsno.setBackgroundResource(R.color.common_red);
                            residualChlorineIsno.startAnimation(alphaAnimation);
                        }
                        // 浊度
                        turbidity.setText(array[8] + " NTU");
                        if (Double.valueOf(array[8]) <= 0.5) {
                            turbidityIsno.setText("正常");
                            turbidityIsno.setBackgroundResource(R.color.green);
                        } else {
                            turbidityIsno.setText("偏高");
                            turbidityIsno.setBackgroundResource(R.color.common_red);
                            turbidityIsno.startAnimation(alphaAnimation);
                        }
                        // orp的值
                        orpValue.setText(array[9] + " mV");
                        if (Double.valueOf(array[9]) <= 650.0) {
                            orpValueIsno.setText("正常");
                            orpValueIsno.setBackgroundResource(R.color.green);
                        } else {
                            orpValueIsno.setText("偏高");
                            orpValueIsno.setBackgroundResource(R.color.common_red);
                            orpValueIsno.startAnimation(alphaAnimation);
                        }
                        // 盐度
                        salinity.setText(array[10] + " ppm");
                        if (Double.valueOf(array[10]) <= 100) {
                            salinityIsno.setText("正常");
                            salinityIsno.setBackgroundResource(R.color.green);
                        } else {
                            salinityIsno.setText("偏高");
                            salinityIsno.setBackgroundResource(R.color.common_red);
                            salinityIsno.startAnimation(alphaAnimation);
                        }
                        // 溶氧量
                        dissolvedOxygen.setText(array[11] + " mg/L");
                        if (Double.valueOf(array[11]) <= 9.0) {
                            dissolvedOxygenIsno.setText("正常");
                            dissolvedOxygenIsno.setBackgroundResource(R.color.green);
                        } else {
                            dissolvedOxygenIsno.setText("偏高");
                            dissolvedOxygenIsno.setBackgroundResource(R.color.common_red);
                            dissolvedOxygenIsno.startAnimation(alphaAnimation);
                        }
                        // 水质硬度
                        waterQuality.setText(array[12] + " ppm");
                        if (Double.valueOf(array[12]) <= 300.0) {
                            waterQualityIsno.setText("正常");
                            waterQualityIsno.setBackgroundResource(R.color.green);
                        } else {
                            waterQualityIsno.setText("偏高");
                            waterQualityIsno.setBackgroundResource(R.color.common_red);
                            waterQualityIsno.startAnimation(alphaAnimation);
                        }
                        // 这是水泵状态的数据
                        JSONArray jsonArray = new JSONArray(object1.getString("Pump"));
                        JSONObject jsonObject1 = jsonArray.getJSONObject(0);
                        runState1.setText(jsonObject1.getString("PFault"));
                        frequency1.setText(jsonObject1.getString("Frequency"));
                        ampere1.setText(jsonObject1.getString("Electric"));
                        /*if(Double.valueOf(jsonObject1.getString("Electric")) < 40){
                            state1.setText("正常");
                            state1.setBackgroundResource(R.color.green);
                        } else {
                            state1.setText("不正常");
                            state1.setBackgroundResource(R.color.common_red);
                            state1.startAnimation(alphaAnimation);
                        }*/

                        JSONObject jsonObject2 = jsonArray.getJSONObject(1);
                        runState2.setText(jsonObject2.getString("PFault"));
                        frequency2.setText(jsonObject2.getString("Frequency"));
                        ampere2.setText(jsonObject2.getString("Electric"));
                        /*if(Double.valueOf(jsonObject2.getString("Electric")) < 40){
                            state2.setText("正常");
                            state2.setBackgroundResource(R.color.green);
                        } else {
                            state2.setText("不正常");
                            state2.setBackgroundResource(R.color.common_red);
                            state2.startAnimation(alphaAnimation);
                        }*/

                        JSONObject jsonObject3 = jsonArray.getJSONObject(2);
                        runState3.setText(jsonObject3.getString("PFault"));
                        frequency3.setText(jsonObject3.getString("Frequency"));
                        ampere3.setText(jsonObject3.getString("Electric"));
                        /*if(Double.valueOf(jsonObject3.getString("Electric")) < 40){
                            state3.setText("正常");
                            state3.setBackgroundResource(R.color.green);
                        } else {
                            state3.setText("不正常");
                            state3.setBackgroundResource(R.color.common_red);
                            state3.startAnimation(alphaAnimation);
                        }*/

                        JSONObject jsonObject4 = jsonArray.getJSONObject(3);
                        runState4.setText(jsonObject4.getString("PFault"));
                        frequency4.setText(jsonObject4.getString("Frequency"));
                        ampere4.setText(jsonObject4.getString("Electric"));
                        /*if(Double.valueOf(jsonObject4.getString("Electric")) < 40){
                            state4.setText("正常");
                            state4.setBackgroundResource(R.color.green);
                        } else {
                            state4.setText("不正常");
                            state4.setBackgroundResource(R.color.common_red);
                            state4.startAnimation(alphaAnimation);
                        }*/

                        JSONObject jsonObject5 = jsonArray.getJSONObject(4);
                        runState5.setText(jsonObject5.getString("PFault"));
                        frequency5.setText(jsonObject5.getString("Frequency"));
                        ampere5.setText(jsonObject5.getString("Electric"));
                        /*if(Double.valueOf(jsonObject5.getString("Electric")) < 40){
                            state5.setText("正常");
                            state5.setBackgroundResource(R.color.green);
                        } else {
                            state5.setText("不正常");
                            state5.setBackgroundResource(R.color.common_red);
                            state5.startAnimation(alphaAnimation);
                        }*/
                        // 这是阀门状态的数据
                        String[] array2 = object1.getString("Valve")
                                .replace("[", "")
                                .replace("]", "")
                                .replace("\"", "")
                                .split("\\,");
                        cleanPumpState.setText(array2[0]);
                        inletValveState.setText(array2[1]);
                        bypassValveState.setText(array2[2]);
                        highPressureValveState.setText(array2[3]);
                        concentratedWaterValveState.setText(array2[4]);
                        returnValveState.setText(array2[5]);
                        //toast("婷婷");
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

    @Override
    protected void onStart() {
        super.onStart();
        startTask();
        init();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopTask();
    }

    private void toast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
