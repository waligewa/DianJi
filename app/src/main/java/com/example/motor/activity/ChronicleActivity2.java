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

import java.util.ArrayList;
import java.util.List;

/**
 * 故障记录的一级界面
 *
 */
public class ChronicleActivity2 extends Activity implements AutoListView.OnRefreshListener,
        AutoListView.OnLoadListener {

    View view;
    @ViewInject(R.id.ic_a_title)
    TextView mtitleTextView;
    @ViewInject(R.id.lv_chronicle)
    AutoListView mListView;
    List<ChronicleInfo> infos = new ArrayList<ChronicleInfo>();
    ChronicleAdapter mAdapter;
    Loadding loading;
    private int pageIndex = 1, pageSize = 20;
    private SharedPreferences prefs1;
    private String gatewayAddress;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chronicle2);
        x.view().inject(this);
        //initData();
    }

    private void initData() {
        prefs1 = getSharedPreferences("UserInfo", 0);
        gatewayAddress = prefs1.getString("add", "");
        loading = new Loadding(this);
        intent = new Intent();
        mtitleTextView.setText(getSharedPreferences("device", Context.MODE_PRIVATE)
                .getString("comName", "故障详情") + " 当前故障");
        // 获取所有设备故障记录
        getData();
        mAdapter = new ChronicleAdapter(this, infos);
        mListView.setAdapter(mAdapter);
        mListView.setOnRefreshListener(this);
        mListView.setOnLoadListener(this);
    }

    @Event(value = { R.id.iv_back, R.id.history }, type = View.OnClickListener.class)
    private void btnClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.history:
                intent.setClass(this, TestChartActivity.class);
                startActivity(intent);
                break;
        }
    }

    // 获取所有设备故障记录
    private void getData() {
        loading.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadEventJKInfo");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadEventJKInfo");
        }
        String guidString = getSharedPreferences("UserInfo", 0).getString("guid", "");
        String equNo = getSharedPreferences("device", Context.MODE_PRIVATE).getString("EquipmentNo", "");
        int userId = getSharedPreferences("UserInfo", 0).getInt("UserID", 0);
        params.addBodyParameter("guid", guidString);
        params.addBodyParameter("equNo", equNo);
        params.addBodyParameter("Year", "");
        // pageIndex永远为1，pageSize为pageSize乘以pageIndex。listview能记忆位置
        params.addBodyParameter("pageIndex", "1");
        params.addBodyParameter("pageSize", String.valueOf(pageSize * pageIndex));
        params.addBodyParameter("userId", userId + "");
        x.http().get(params, new CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                //toast(ex.getMessage());
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
                        Intent intent = new Intent(ChronicleActivity2.this, LoginActivity.class);
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
        // 获取所有设备故障记录
        getData();
    }

    @Override
    public void onRefresh() {
        pageIndex = 1;
        // 获取所有设备故障记录
        getData();
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
