package com.example.motor.workorderlist.inspection;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.motor.R;
import com.example.motor.activity.LoginActivity;
import com.example.motor.adapter.TaskRvAdapter2;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.TaskItemBean;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DoubleClickUtils;
import com.example.motor.util.Loadding;
import com.example.motor.util.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 巡检已转发的fragment
 *
 */
public class InspectionTransmitedFragment extends Fragment{

    private LinearLayout linearLayout;
    private RecyclerView recyclerView;
    private RadioGroup taskRadioGroup;
    private RadioButton radioReceived, radioFeedbacked, radioNohandled, radioHandled;
    private List<TaskItemBean> datas = new ArrayList<>();
    private List<TaskItemBean> gsonDatas;
    private Loadding loadding;
    private String gatewayAddress, userId;
    private SharedPreferences prefs1;
    private Intent intent;
    private TaskRvAdapter2 mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        linearLayout = (LinearLayout) inflater.inflate(R.layout.transmited_fragment, container, false);
        recyclerView = (RecyclerView) linearLayout.findViewById(R.id.recyclerView);
        taskRadioGroup = (RadioGroup) linearLayout.findViewById(R.id.taskRadioGroup);
        radioNohandled = (RadioButton) linearLayout.findViewById(R.id.radio_nohandled);  // 未处理
        radioReceived = (RadioButton) linearLayout.findViewById(R.id.radio_received);  // 已接收
        radioFeedbacked = (RadioButton) linearLayout.findViewById(R.id.radio_feedbacked);  // 已反馈
        radioHandled = (RadioButton) linearLayout.findViewById(R.id.radio_handled);  // 已处理
        // RadioGroup的两个RadioButton的三个点击事件
        taskRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    if (checkedId == R.id.radio_handled){
                        // 已处理
                        getHandledData();
                    } else if (checkedId == R.id.radio_received) {
                        // 已接收
                        getReceivedData();
                    } else if (checkedId == R.id.radio_feedbacked) {
                        // 已反馈
                        getFeedbackedData();
                    } else if (checkedId == R.id.radio_nohandled) {
                        // 未处理
                        getNoHandledData();
                    }
                }
            }
        });
        loadding = new Loadding(getActivity());
        intent = new Intent();
        return linearLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prefs1 = getActivity().getSharedPreferences("UserInfo", 0);

        mAdapter = new TaskRvAdapter2(getActivity(), datas);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mAdapter);
        gatewayAddress = prefs1.getString("add", "");
        userId = String.valueOf(prefs1.getInt("UserID", 0));
        // 未处理
        getNoHandledData();
    }

    // 未处理的数据
    private void getNoHandledData() {
        loadding.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(getActivity(), ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        }
        params.addBodyParameter("userID", userId);
        params.addBodyParameter("pageSize", "100");
        params.addBodyParameter("beginDate", "1900-01-01");
        params.addBodyParameter("endDate", "2200-12-31");
        params.addBodyParameter("guid", "");
        params.addBodyParameter("pageIndex", "1");
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast(ex.getMessage());
                if (loadding.isShow()) {
                    loadding.close();
                }
            }

            @Override
            public void onFinished() {}

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        intent.setClass(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")){
                        datas.clear();
                        Type listType = new TypeToken<List<TaskItemBean>>() {}.getType();
                        Gson gson = new Gson();
                        gsonDatas = gson.fromJson(object1.getString("Data"), listType);
                        for(int i = 0; i < gsonDatas.size(); i++){
                            // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                            // IssuedUser就是admin跟userId是不同的，这样的话进入执行中 WOIssuedUser为派工人
                            if(gsonDatas.get(i).getWOType().equals("2") &&
                                    gsonDatas.get(i).getWOState().equals("false") &&
                                    gsonDatas.get(i).getWOIssuedUser().equals(userId) &&
                                    TextUtils.isEmpty(gsonDatas.get(i).getWOReceiveDate()) &&
                                    TextUtils.isEmpty(gsonDatas.get(i).getWOFeedback())){
                                datas.add(gsonDatas.get(i));
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        toast(object1.getString("Message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (loadding.isShow()) {
                    loadding.close();
                }
            }
        });
    }

    // 已接收的数据
    private void getReceivedData() {
        loadding.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(getActivity(), ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        }
        params.addBodyParameter("userID", userId);
        params.addBodyParameter("pageSize", "100");
        params.addBodyParameter("beginDate", "1900-01-01");
        params.addBodyParameter("endDate", "2200-12-31");
        params.addBodyParameter("guid", "");
        params.addBodyParameter("pageIndex", "1");
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast(ex.getMessage());
                if (loadding.isShow()) {
                    loadding.close();
                }
            }

            @Override
            public void onFinished() {}

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        intent.setClass(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")){
                        datas.clear();
                        Type listType = new TypeToken<List<TaskItemBean>>() {}.getType();
                        Gson gson = new Gson();
                        gsonDatas = gson.fromJson(object1.getString("Data"), listType);
                        for(int i = 0; i < gsonDatas.size(); i++){
                            // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                            // IssuedUser就是admin跟userId是不同的，这样的话进入执行中 WOIssuedUser为派工人
                            // 接收时间不为空，反馈为空
                            if(gsonDatas.get(i).getWOType().equals("2") &&
                                    gsonDatas.get(i).getWOState().equals("false") &&
                                    gsonDatas.get(i).getWOIssuedUser().equals(userId) &&
                                    !TextUtils.isEmpty(gsonDatas.get(i).getWOReceiveDate()) &&
                                    TextUtils.isEmpty(gsonDatas.get(i).getWOFeedback())){
                                datas.add(gsonDatas.get(i));
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        toast(object1.getString("Message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (loadding.isShow()) {
                    loadding.close();
                }
            }
        });
    }

    // 已反馈的数据
    private void getFeedbackedData() {
        loadding.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(getActivity(), ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        }
        params.addBodyParameter("userID", userId);
        params.addBodyParameter("pageSize", "100");
        params.addBodyParameter("beginDate", "1900-01-01");
        params.addBodyParameter("endDate", "2200-12-31");
        params.addBodyParameter("guid", "");
        params.addBodyParameter("pageIndex", "1");
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast(ex.getMessage());
                if (loadding.isShow()) {
                    loadding.close();
                }
            }

            @Override
            public void onFinished() {}

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        intent.setClass(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")){
                        datas.clear();
                        Type listType = new TypeToken<List<TaskItemBean>>() {}.getType();
                        Gson gson = new Gson();
                        gsonDatas = gson.fromJson(object1.getString("Data"), listType);
                        for(int i = 0; i < gsonDatas.size(); i++){
                            // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                            // IssuedUser就是admin跟userId是不同的，这样的话进入执行中 WOIssuedUser为派工人
                            // 已反馈只要保证反馈不为空就可以
                            if(gsonDatas.get(i).getWOType().equals("2") &&
                                    gsonDatas.get(i).getWOState().equals("false") &&
                                    gsonDatas.get(i).getWOIssuedUser().equals(userId) &&
                                    !TextUtils.isEmpty(gsonDatas.get(i).getWOFeedback())){
                                datas.add(gsonDatas.get(i));
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        toast(object1.getString("Message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (loadding.isShow()) {
                    loadding.close();
                }
            }
        });
    }

    // 已处理的数据
    private void getHandledData() {
        loadding.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(getActivity(), ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        }
        params.addBodyParameter("userID", userId);
        params.addBodyParameter("pageSize", "100");
        params.addBodyParameter("beginDate", "1900-01-01");
        params.addBodyParameter("endDate", "2200-12-31");
        params.addBodyParameter("guid", "");
        params.addBodyParameter("pageIndex", "1");
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast(ex.getMessage());
                if (loadding.isShow()) {
                    loadding.close();
                }
            }

            @Override
            public void onFinished() {}

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        intent.setClass(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")){
                        datas.clear();
                        Type listType = new TypeToken<List<TaskItemBean>>() {}.getType();
                        Gson gson = new Gson();
                        gsonDatas = gson.fromJson(object1.getString("Data"), listType);
                        for(int i = 0; i < gsonDatas.size(); i++){
                            // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                            // IssuedUser就是admin跟userId是不同的，这样的话进入执行中 WOIssuedUser为派工人
                            if(gsonDatas.get(i).getWOType().equals("2") &&
                                    gsonDatas.get(i).getWOState().equals("true") &&
                                    gsonDatas.get(i).getWOIssuedUser().equals(userId)){
                                datas.add(gsonDatas.get(i));
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        toast(object1.getString("Message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (loadding.isShow()) {
                    loadding.close();
                }
            }
        });
    }

    private void toast(String text){
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (radioReceived.isChecked()) {
            // 已接收
            getReceivedData();
        } else if (radioFeedbacked.isChecked()){
            // 已反馈
            getFeedbackedData();
        } else if (radioNohandled.isChecked()) {
            // 未处理
            getNoHandledData();
        } else if (radioHandled.isChecked()) {
            // 已处理
            getHandledData();
        }
    }
}
