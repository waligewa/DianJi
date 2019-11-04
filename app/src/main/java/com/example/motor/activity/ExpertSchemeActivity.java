package com.example.motor.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.adapter.ExpertSchemeAdapter;
import com.example.motor.base.BaseActivity;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.DeviceStateInfo;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.Loadding;
import com.example.motor.util.SPUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.LinkedList;
import java.util.List;

public class ExpertSchemeActivity extends BaseActivity {

    private Loadding loading;
    private Intent intent;
    private Activity mActivity;
    private String expertScheme = "", gatewayAddress = "";
    private SharedPreferences prefs1;
    private List<DeviceStateInfo> list = new LinkedList<>();
    private ExpertSchemeAdapter adapter;
    @ViewInject(R.id.listview)
    ListView listView;

    // layout文件
    @Override
    public int setLayout() {
        return R.layout.activity_expert_scheme;
    }

    // 在这里面设置TitleBar相关的
    @Override
    public void setTitle() {
        mBaseTitleBarView.setTitleText("专家方案");
        mBaseTitleBarView.setLeftDrawable(-1);
    }

    // 初始化控件，设置监听
    @Override
    public void initWidget() {
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);
        init();
    }

    // View的click事件
    @Override
    public void widgetClick(View v) {}

    // 返回键事件
    @Override
    public void onTitleLeftPressed() {
        onBackPressed();
    }

    private void init(){
        intent = new Intent();
        mActivity = this;
        loading = new Loadding(this);
        prefs1 = getSharedPreferences("UserInfo", 0);
        gatewayAddress = prefs1.getString("add", "");
        expertScheme = getIntent().getStringExtra("expert_scheme");
        adapter = new ExpertSchemeAdapter(mActivity, list);
        listView.setAdapter(adapter);
        // 获取专家数据
        getExpertScheme();
    }

    // 获取专家数据
    private void getExpertScheme() {
        loading.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/GetExpertSystem");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/GetExpertSystem");
        }
        params.addBodyParameter("WOContent", expertScheme);
        x.http().get(params, new Callback.CommonCallback<String>() {

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
                    if (object1.getString("Code").equals("0")) {
                        intent.setClass(mActivity, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")){
                        list.clear();
                        JSONArray array = new JSONArray(object1.getString("Data"));
                        for(int i = 0; i < array.length(); i++){
                            DeviceStateInfo item = new DeviceStateInfo();
                            item.setIdString(array.getJSONObject(i).getString("Num"));
                            item.setRunStateString(array.getJSONObject(i).getString("Title"));
                            item.setControlStateString(array.getJSONObject(i).getString("CreatTime"));
                            item.setElectricString(array.getJSONObject(i).getString("ProDis"));
                            item.setNameString(array.getJSONObject(i).getString("PlanContent"));
                            list.add(item);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        toast(object1.getString("Message"));
                    }
                } catch (Exception e) {
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
}
