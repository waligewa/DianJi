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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 点击listview查看故障详情
 *
 */
public class ChronicleActivity1 extends Activity implements AutoListView.OnRefreshListener,
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
    private String gatewayAddress, faultName, startDate, endDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");  // 年-月-日 时-分

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chronicle1);
        x.view().inject(this);
        //initData();
    }

    private void initData() {
        prefs1 = getSharedPreferences("UserInfo", 0);
        gatewayAddress = prefs1.getString("add", "");
        loading = new Loadding(this);
        mtitleTextView.setText(getSharedPreferences("device", Context.MODE_PRIVATE).getString("comName", "故障详情"));
        faultName = getIntent().getStringExtra("fault_name");
        startDate = getIntent().getStringExtra("start_date");
        endDate = getIntent().getStringExtra("end_date");
        // 获取所有设备故障记录
        getData();
        mAdapter = new ChronicleAdapter(this, infos);
        mListView.setAdapter(mAdapter);
        mListView.setOnRefreshListener(this);
        mListView.setOnLoadListener(this);
    }

    @Event(value = { R.id.iv_back }, type = View.OnClickListener.class)
    private void btnClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
        }
    }

    // 获取所有设备故障记录
    private void getData() {
        loading.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadEventHistoryInfo");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadEventHistoryInfo");
        }
        String guidString = getSharedPreferences("UserInfo", 0).getString("guid", "");
        String equNo = getSharedPreferences("device", Context.MODE_PRIVATE).getString("EquipmentNo", "");
        int userId = getSharedPreferences("UserInfo", 0).getInt("UserID", 0);
        params.addBodyParameter("guid", "");
        params.addBodyParameter("equNo", equNo);
        params.addBodyParameter("Year", "");
        params.addBodyParameter("pageIndex", "1");
        params.addBodyParameter("pageSize", "1000000");
        params.addBodyParameter("userId", String.valueOf(userId));
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
                    infos.clear();
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        Intent intent = new Intent(ChronicleActivity1.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")) {
                        JSONArray array = new JSONArray(object1.getString("Data"));
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            ChronicleInfo info = new ChronicleInfo();
                            info.setDeviceId(object.get("DeviceName").toString());
                            // 两层条件：条件1——名称要一致
                            if(object.get("EventMessage").toString().equals(faultName)){
                                try{
                                    Date date1 = dateFormat.parse(startDate);  // 开始时间
                                    Date date2 = dateFormat.parse(endDate);  // 结束时间
                                    Date eventTime = dateFormat.parse(object.get("EventTime")
                                            .toString().replace("T", " "));
                                    // 条件2——时间要在开始时间和结束时间之间
                                    if(eventTime.getTime() >= date1.getTime() && eventTime.getTime() <= date2.getTime()){
                                        info.setContentString(object.get("EventMessage").toString());
                                        info.setTimeString(object.get("EventTime").toString());
                                        info.setFaultReason(object.getString("EventSource"));
                                        info.setAlarmType(object.getString("EventLevel"));
                                        info.setAlarmState(object.getString("State"));
                                        infos.add(info);
                                    }
                                }catch (ParseException e){
                                    e.printStackTrace();
                                }
                            }
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
