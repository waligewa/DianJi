package com.example.motor.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.adapter.ChronicleAdapter2;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.ChronicleInfo;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DateTimePickDialogUtil;
import com.example.motor.util.Loadding;
import com.example.motor.util.SPUtils;
import com.example.motor.widget.AutoListView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 启停机记录的activity
 * 只走getData2()方法，不走getData()方法
 *
 */
public class StartStopRecordActivity extends Activity implements AutoListView.OnRefreshListener,
        AutoListView.OnLoadListener {

    View view;
    @ViewInject(R.id.ic_a_title)
    TextView mtitleTextView;
    @ViewInject(R.id.lv_chronicle)
    AutoListView mListView;
    @ViewInject(R.id.start_time)
    TextView startTime;
    @ViewInject(R.id.end_time)
    TextView endTime;
    List<ChronicleInfo> infos = new ArrayList<>();
    ChronicleAdapter2 mAdapter;
    private Loadding loading;
    private int pageIndex = 1, pageSize = 50;
    private SharedPreferences prefs1, prefs2;
    private String gatewayAddress, currentTime, equipmentId;
    private Date date = new Date();
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
    private Calendar calendar;  // 用来装日期的
    private DateTimePickDialogUtil dateTimePicKDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_stop_record);
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);

        // 如下代码是用于给两个时间按钮赋值时间
        calendar = Calendar.getInstance();
        Date now = new Date(); // 当前时间
        Date tomorrow = new Date();
        calendar.setTime(now); // 把当前时间赋给日历
        calendar.add(Calendar.DAY_OF_MONTH, 1); // 设置为前一天
        tomorrow = calendar.getTime(); // 得到前一天的时间。getTime()返回一个Date对象
        startTime.setText(f.format(now) + " 00:00:00");
        endTime.setText(f.format(tomorrow) + " 00:00:00");
    }

    private void initData() {
        currentTime = format.format(date);
        prefs1 = getSharedPreferences("UserInfo", 0);
        prefs2 = getSharedPreferences("device", Context.MODE_PRIVATE);
        gatewayAddress = prefs1.getString("add", "");
        equipmentId = prefs2.getString("EquipmentID", "");
        loading = new Loadding(this);
        mtitleTextView.setText(prefs2.getString("comName", "开停机记录"));
        // 网络请求数据的接口
        if(!startTime.getText().toString().equals("开始时间") && !endTime.getText().toString().equals("结束时间")){
            // 点击时间选择框之后的数据采集方法
            getData2();
        } else {
            // 网络请求数据的接口
            getData();
        }
        mAdapter = new ChronicleAdapter2(this, infos);
        mListView.setAdapter(mAdapter);
        mListView.setOnRefreshListener(this);
        mListView.setOnLoadListener(this);
        dateTimePicKDialog = new DateTimePickDialogUtil(this, format.format(new Date()));
        DateTimePickDialogUtil.setOnItemSelectChangeListener(
                new DateTimePickDialogUtil.OnSetTimeChangeListener() {
                    @Override
                    public void OnSetTimeChange() {
                        // 点击时间选择框之后的数据采集方法
                        if(!startTime.getText().toString().equals("开始时间") && !endTime.getText().toString().equals("结束时间")){
                            // 点击时间选择框之后的数据采集方法
                            getData2();
                        }
                    }
                });
    }

    @Event(value = { R.id.iv_back, R.id.start_time, R.id.end_time }, type = View.OnClickListener.class)
    private void btnClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.start_time:
                // 日期选择的方法
                dateTimePicKDialog.dateTimePicKDialog(startTime);
                break;
            case R.id.end_time:
                if(startTime.getText().toString().contains("开始时间")){
                    toast("请先选择开始时间");
                } else {
                    // 日期选择的方法
                    dateTimePicKDialog.dateTimePicKDialog(endTime);
                }
                break;
        }
    }

    // 网络请求数据的接口
    private void getData() {
        infos.clear();
        loading.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString +
                    "Service/C_WNMS_API.asmx/PumpsState");
        } else {
            params = new RequestParams(gatewayAddress +
                    "Service/C_WNMS_API.asmx/PumpsState");
        }
        calendar = Calendar.getInstance();
        Date dNow = new Date(); // 当前时间
        Date dTomorrow = new Date();
        calendar.setTime(dNow); // 把当前时间赋给日历
        calendar.add(Calendar.DAY_OF_MONTH, 1); // 设置为后一天
        dTomorrow = calendar.getTime(); // 得到前一天的时间。getTime()返回一个Date对象
        String tomorrowTime = format.format(dTomorrow); // 格式化后一天
        params.addBodyParameter("EquipmentID", equipmentId);
        params.addBodyParameter("StartDate", currentTime);
        params.addBodyParameter("EndDate", tomorrowTime);
        x.http().get(params, new CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                if (pageIndex == 1) {
                    mListView.onRefreshComplete();
                } else {
                    mListView.onLoadComplete();
                }
                mListView.setPageSize(pageSize);
                mListView.setResultSize(1);
                toast("无数据");
                if (loading.isShow()) {
                    loading.close();
                }
            }

            @Override
            public void onFinished() { }

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        Intent intent = new Intent(StartStopRecordActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")) {
                        String pumpNumber = object1.getString("PumpNum");
                        JSONArray array = new JSONArray(object1.getString("Data"));
                        String a = "", b = "", c = "", d = "";
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            ChronicleInfo info = new ChronicleInfo();
                            info.setTimeString(object.get("UpdateTime").toString());  // 更新时间
                            if(pumpNumber.equals("4")){
                                // 1号泵的
                                if(object.get("PVF1").toString().equals("null")){
                                    info.setDeviceId(a);
                                } else if (object.get("PVF1").toString().equals("0")){
                                    a = "停止";
                                    info.setDeviceId("停止");
                                } else if (object.get("PVF1").toString().equals("1")){
                                    a = "启动";
                                    info.setDeviceId("启动");
                                }

                                // 2号泵的
                                if(object.get("PVF2").toString().equals("null")){
                                    info.setContentString(b);
                                } else if (object.get("PVF2").toString().equals("0")){
                                    b = "停止";
                                    info.setContentString(b);
                                } else if (object.get("PVF2").toString().equals("1")){
                                    b = "启动";
                                    info.setContentString(b);
                                }

                                // 3号泵的
                                if(object.get("PVF3").toString().equals("null")){
                                    info.setEventMessage(c);
                                } else if (object.get("PVF3").toString().equals("0")){
                                    c = "停止";
                                    info.setEventMessage(c);
                                } else if (object.get("PVF3").toString().equals("1")){
                                    c = "启动";
                                    info.setEventMessage(c);
                                }

                                // 4号泵的
                                if(object.get("PVF4").toString().equals("null")){
                                    info.setFaultReason(d);
                                } else if (object.get("PVF4").toString().equals("0")){
                                    d = "停止";
                                    info.setFaultReason(d);
                                } else if (object.get("PVF4").toString().equals("1")){
                                    d = "启动";
                                    info.setFaultReason(d);
                                }
                            }
                            infos.add(info);
                        }
                        if (pageIndex == 1) {
                            mListView.onRefreshComplete();
                        } else {
                            mListView.onLoadComplete();
                        }
                        mListView.setPageSize(pageSize);
                        mListView.setResultSize(array.length());
                        mAdapter.notifyDataSetChanged();
                    } else {
                        toast(object1.getString("Message"));
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (loading.isShow()) {
                    loading.close();
                }
            }
        });
    }

    // 点击时间选择框之后的数据采集方法
    private void getData2() {
        infos.clear();
        //mAdapter.notifyDataSetChanged();
        loading.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/PumpsState");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/PumpsState");
        }
        params.addBodyParameter("EquipmentID", equipmentId);
        params.addBodyParameter("StartDate", startTime.getText().toString());
        params.addBodyParameter("EndDate", endTime.getText().toString());
        x.http().get(params, new CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {}

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast("服务器异常");
                infos.clear();
                mAdapter.notifyDataSetChanged();
                if (loading.isShow()) {
                    loading.close();
                }
            }

            @Override
            public void onFinished() {}

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        Intent intent = new Intent(StartStopRecordActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")) {
                        String pumpNumber = object1.getString("PumpNum");
                        JSONArray array = new JSONArray(object1.getString("Data"));
                        String a = "", b = "", c = "", d = "", e = "";
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            ChronicleInfo info = new ChronicleInfo();
                            info.setTimeString(object.get("UpdateTime").toString());  // 更新时间
                            // 这样写2泵、 3泵、 4泵、 5泵的都可以适配了
                            if(Integer.valueOf(pumpNumber) > 1){
                                // 1号泵的
                                if(object.get("PVF1").toString().equals("null")){
                                    info.setDeviceId(a);
                                } else if (object.get("PVF1").toString().equals("0")){
                                    a = "停止";
                                    info.setDeviceId("停止");
                                } else if (object.get("PVF1").toString().equals("1")){
                                    a = "启动";
                                    info.setDeviceId("启动");
                                }

                                // 2号泵的
                                if(object.get("PVF2").toString().equals("null")){
                                    info.setContentString(b);
                                } else if (object.get("PVF2").toString().equals("0")){
                                    b = "停止";
                                    info.setContentString(b);
                                } else if (object.get("PVF2").toString().equals("1")){
                                    b = "启动";
                                    info.setContentString(b);
                                }
                            }
                            if (Integer.valueOf(pumpNumber) > 2) {
                                // 3号泵的
                                if(object.get("PVF3").toString().equals("null")){
                                    info.setEventMessage(c);
                                } else if (object.get("PVF3").toString().equals("0")){
                                    c = "停止";
                                    info.setEventMessage(c);
                                } else if (object.get("PVF3").toString().equals("1")){
                                    c = "启动";
                                    info.setEventMessage(c);
                                }
                            }
                            if (Integer.valueOf(pumpNumber) > 3) {
                                // 4号泵的
                                if(object.get("PVF4").toString().equals("null")){
                                    info.setFaultReason(d);
                                } else if (object.get("PVF4").toString().equals("0")){
                                    d = "停止";
                                    info.setFaultReason(d);
                                } else if (object.get("PVF4").toString().equals("1")){
                                    d = "启动";
                                    info.setFaultReason(d);
                                }
                            }
                            if (Integer.valueOf(pumpNumber) > 4){
                                // 5号泵的
                                if(object.get("PVF5").toString().equals("null")){
                                    info.setAlarmType(e);
                                } else if (object.get("PVF4").toString().equals("0")){
                                    e = "停止";
                                    info.setAlarmType(e);
                                } else if (object.get("PVF4").toString().equals("1")){
                                    e = "启动";
                                    info.setAlarmType(e);
                                }
                            }
                            info.setNum(Integer.valueOf(pumpNumber));
                            infos.add(info);
                        }
                        if (pageIndex == 1) {
                            mListView.onRefreshComplete();
                        } else {
                            mListView.onLoadComplete();
                        }
                        mListView.setPageSize(pageSize);
                        mListView.setResultSize(array.length());
                        mAdapter.notifyDataSetChanged();
                    } else {
                        toast(object1.getString("Message"));
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (loading.isShow()) {
                    loading.close();
                }
            }
        });
    }

    private void toast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoad() {
        pageIndex++;
        if(!startTime.getText().toString().equals("开始时间") && !endTime.getText().toString().equals("结束时间")){
            // 点击时间选择框之后的数据采集方法
            getData2();
        } else {
            // 网络请求数据的接口
            getData();
        }
    }

    @Override
    public void onRefresh() {
        pageIndex = 1;
        if(!startTime.getText().toString().equals("开始时间") && !endTime.getText().toString().equals("结束时间")){
            // 点击时间选择框之后的数据采集方法
            getData2();
        } else {
            // 网络请求数据的接口
            getData();
        }
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        initData();
    }
}
