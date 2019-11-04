package com.example.motor.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.motor.R;
import com.example.motor.adapter.ChronicleAdapter;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.ChronicleInfo;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DoubleClickUtils;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 1、这个activity是点击推送之后显示故障记录的activity，写于18.11.6，因为在此之前点击报警推送之后直接跳转到
 * 故障记录界面，显示的不一定是推送上面的设备，因为我直接写的跳转到故障记录，如果前期选择的大和路泵站，
 * 那么推送是DTU泵站，那么点击DTU泵站的推送就直接跳入大和路泵站的故障记录界面了
 * 2、故障记录的一级界面
 *
 */
public class ChronicleActivity3 extends Activity implements AutoListView.OnRefreshListener,
        AutoListView.OnLoadListener {

    View view;
    @ViewInject(R.id.ic_a_title)
    TextView mtitleTextView;
    @ViewInject(R.id.lv_chronicle)
    AutoListView mListView;
    @ViewInject(R.id.content)
    TextView content;
    @ViewInject(R.id.current_time)
    TextView currentTime;
    @ViewInject(R.id.happen)
    LinearLayout happen;
    List<ChronicleInfo> infos = new ArrayList<ChronicleInfo>();
    ChronicleAdapter mAdapter;
    Loadding loading;
    private int pageIndex = 1, pageSize = 20;
    private SharedPreferences prefs1, prefs2;
    private String gatewayAddress, type, deviceName = "", equNum = "", pushContent = "", dateString = "";
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chronicle3);
        x.view().inject(this);
    }

    private void initData() {
        prefs1 = getSharedPreferences("UserInfo", 0);
        prefs2 = getSharedPreferences("device", Context.MODE_PRIVATE);
        gatewayAddress = prefs1.getString("add", "");
        loading = new Loadding(this);
        intent = new Intent();
        mAdapter = new ChronicleAdapter(this, infos);
        mListView.setAdapter(mAdapter);
        mListView.setOnRefreshListener(this);
        mListView.setOnLoadListener(this);

        type = getIntent().getStringExtra("workorder_type");  // 得到从MyReceiver过来的json字符串
        pushContent = getIntent().getStringExtra("workorder_number");  // 得到从MyReceiver过来的内容
        String s = pushContent.replace("|", "\n");
        content.setText(s);
        try{
            // 将字符串做成JSONArray，然后得到JSONObject，从而得到设备编号和设备名称
            JSONArray jsonArray = new JSONArray("[" + type + "]");
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            equNum = jsonObject.getString("deviceid");  // 设备编号
            deviceName = jsonObject.getString("devicename");  // 设备名称
            mtitleTextView.setText(deviceName + " 报警详情");
            List<String> list = DoubleClickUtils.getDatime(pushContent);
            dateString = list.get(0);  // 将集合中的日期字符串传递出来
            currentTime.setText(dateString + "的报警信息为");
            // 获取所有设备故障记录
            getData(dateString);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Event(value = { R.id.iv_back, R.id.happen }, type = View.OnClickListener.class)
    private void btnClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.happen:
                if(!DoubleClickUtils.isFastDoubleClick()){
                    intent.setClass(this, ChronicleActivity2.class);
                    startActivity(intent);
                }
                break;
        }
    }

    // 获取所有设备故障记录
    private void getData(String s) {
        loading.show("加载数据中...");
        String tomorrowTime = "";
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
            Date dNow = format.parse(s);
            Calendar calendar = Calendar.getInstance();
            Date dTomorrow = new Date();
            calendar.setTime(dNow); // 把当前时间赋给日历
            calendar.add(Calendar.DAY_OF_MONTH, 1); // 设置为后一天
            dTomorrow = calendar.getTime(); // 得到前一天的时间。getTime()返回一个Date对象
            tomorrowTime = format.format(dTomorrow); // 格式化后一天
        } catch(ParseException px) {
            px.printStackTrace();
        }
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadTodayEventHistoryInfo");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadTodayEventHistoryInfo");
        }
        int userId = getSharedPreferences("UserInfo", 0).getInt("UserID", 0);
        if(userId == 0){
            toast("请先登录软件");
            finish();
            return;
        }
        params.addBodyParameter("guid", "");
        params.addBodyParameter("equNo", equNum);
        params.addBodyParameter("BeginTime", s);
        params.addBodyParameter("EndTime", tomorrowTime);
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
                toast(ex.getMessage());
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
                        Intent intent = new Intent(ChronicleActivity3.this, LoginActivity.class);
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
        getData(dateString);
    }

    @Override
    public void onRefresh() {
        pageIndex = 1;
        // 获取所有设备故障记录
        getData(dateString);
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
