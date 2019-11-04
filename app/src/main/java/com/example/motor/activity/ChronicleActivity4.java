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

import com.example.motor.R;
import com.example.motor.adapter.ChronicleAdapter;
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
import java.util.Date;
import java.util.List;

/**
 * 从历史故障记录右上角搜索按钮跳转过来的界面
 *
 */
public class ChronicleActivity4 extends Activity implements AutoListView.OnRefreshListener,
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
    List<ChronicleInfo> infos = new ArrayList<ChronicleInfo>();
    ChronicleAdapter mAdapter;
    Loadding loading;
    private int pageIndex = 1, pageSize = 20;
    private SharedPreferences prefs1;
    private String gatewayAddress;
    private Intent intent;
    private Date date = new Date();
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
    private DateTimePickDialogUtil dateTimePicKDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chronicle4);
        x.view().inject(this);
    }

    private void initData() {
        prefs1 = getSharedPreferences("UserInfo", 0);
        gatewayAddress = prefs1.getString("add", "");
        loading = new Loadding(this);
        intent = new Intent();
        mtitleTextView.setText(getSharedPreferences("device", Context.MODE_PRIVATE).getString("comName", "故障详情"));
        // 如下代码是用于给两个时间按钮赋值时间
        Date now = new Date(); // 当前时间
        startTime.setText(f.format(now) + " 00:00:00");
        endTime.setText(f.format(now) + " 23:59:59");
        // 获取所有设备故障记录，从当天00:00:00到23:59:59的
        getData();
        mAdapter = new ChronicleAdapter(this, infos);
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

    @Event(value = { R.id.iv_back, R.id.history, R.id.start_time, R.id.end_time }, type = View.OnClickListener.class)
    private void btnClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.history:
                intent.setClass(this, ChronicleActivity.class);
                startActivity(intent);
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

    // 获取所有设备故障记录，从当天00:00:00到23:59:59的
    private void getData() {
        Date d = new Date();
        String s = f.format(d);
        loading.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadTodayEventHistoryInfo");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadTodayEventHistoryInfo");
        }
        String guidString = getSharedPreferences("UserInfo", 0).getString("guid", "");
        String equNo = getSharedPreferences("device", Context.MODE_PRIVATE).getString("EquipmentNo", "");
        int userId = getSharedPreferences("UserInfo", 0).getInt("UserID", 0);
        params.addBodyParameter("guid", "");
        params.addBodyParameter("equNo", equNo);
        params.addBodyParameter("BeginTime", s + " 00:00:00");
        params.addBodyParameter("EndTime", s + " 23:59:59");
        // pageIndex永远为1，pageSize为pageSize乘以pageIndex。listview能记忆位置
        params.addBodyParameter("pageIndex", "1");
        //params.addBodyParameter("pageSize", String.valueOf(pageSize * pageIndex));
        params.addBodyParameter("pageSize", "2000");
        params.addBodyParameter("userId", String.valueOf(userId));
        x.http().get(params, new CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast("服务器异常");
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
                    if (object1.getString("Code") == "0") {
                        Intent intent = new Intent(ChronicleActivity4.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")) {
                        infos.clear();
                        JSONArray array = new JSONArray(object1.getString("Data"));
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            ChronicleInfo info = new ChronicleInfo();
                            info.setDeviceId(object.get("DeviceName").toString());  // 泵站名称
                            info.setTimeString(object.get("EventTime").toString());  // 报警时间
                            info.setContentString(object.get("EventMessage").toString());  // 报警信息
                            info.setFaultReason(object.getString("EventSource"));
                            info.setAlarmType(object.getString("EventLevel"));  // 报警类型
                            info.setAlarmState(object.getString("State"));  // 报警状态
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
        loading.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadTodayEventHistoryInfo");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadTodayEventHistoryInfo");
        }
        String equNo = getSharedPreferences("device", Context.MODE_PRIVATE).getString("EquipmentNo", "");
        int userId = getSharedPreferences("UserInfo", 0).getInt("UserID", 0);
        params.addBodyParameter("guid", "");
        params.addBodyParameter("equNo", equNo);
        params.addBodyParameter("BeginTime", startTime.getText().toString());
        params.addBodyParameter("EndTime", endTime.getText().toString());
        // pageIndex永远为1，pageSize为pageSize乘以pageIndex。listview能记忆位置
        params.addBodyParameter("pageIndex", "1");
        //params.addBodyParameter("pageSize", String.valueOf(pageSize * pageIndex));
        params.addBodyParameter("pageSize", "20000");
        params.addBodyParameter("userId", String.valueOf(userId));
        x.http().get(params, new CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast("服务器异常");
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
                    if (object1.getString("Code") == "0") {
                        Intent intent = new Intent(ChronicleActivity4.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")) {
                        infos.clear();
                        JSONArray array = new JSONArray(object1.getString("Data"));
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            ChronicleInfo info = new ChronicleInfo();
                            info.setDeviceId(object.get("DeviceName").toString());  // 泵站名称
                            info.setTimeString(object.get("EventTime").toString());  // 报警时间
                            info.setContentString(object.get("EventMessage").toString());  // 报警信息
                            info.setFaultReason(object.getString("EventSource"));
                            info.setAlarmType(object.getString("EventLevel"));  // 报警类型
                            info.setAlarmState(object.getString("State"));  // 报警状态
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

    @Override
    public void onLoad() {
        pageIndex++;
        if(!startTime.getText().toString().equals("开始时间") && !endTime.getText().toString().equals("结束时间")){
            // 点击时间选择框之后的数据采集方法
            getData2();
        } else {
            // 获取所有设备故障记录，从当天00:00:00到23:59:59的
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
            // 获取所有设备故障记录，从当天00:00:00到23:59:59的
            getData();
        }
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        initData();
    }

    private void toast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
