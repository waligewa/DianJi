package com.example.motor.workorderlist.repair;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.motor.R;
import com.example.motor.activity.DetailTaskActivity;
import com.example.motor.activity.LoginActivity;
import com.example.motor.adapter.TaskRvAdapter3;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.CommonResponseBean;
import com.example.motor.db.TaskItemBean2;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DoubleClickUtils;
import com.example.motor.util.Loadding;
import com.example.motor.util.NetWorkUtil;
import com.example.motor.util.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 维修执行中fragment
 *
 */
public class ExecutingFragment extends Fragment{
    String TAG="debug_ExecutingFragment";

    private RelativeLayout linearLayout;
    private RecyclerView recyclerView;
    private RadioGroup taskRadioGroup;
    private RadioButton radioReceived, radioFeedbacked, radioNohandled;
    private List<TaskItemBean2> datas = new ArrayList<>();
    private List<TaskItemBean2> dataSql = new ArrayList<>();
    private List<TaskItemBean2> gsonDatas = new ArrayList<>();
    private Loadding loadding;
    private String gatewayAddress, userId, fbId = "", username = "";
    private SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private int totalNumber, changeNumber;
    private SharedPreferences prefs1;
    private SharedPreferences.Editor editor;
    private Intent intent;
    private TaskRvAdapter3 mAdapter;
    private JSONArray jsonArray;
    private JSONObject jsonObject1, jsonObject2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        linearLayout = (RelativeLayout) inflater.inflate(R.layout.nohandlefragment, container, false);
        recyclerView = (RecyclerView) linearLayout.findViewById(R.id.recyclerView);
        taskRadioGroup = (RadioGroup) linearLayout.findViewById(R.id.taskRadioGroup);
        radioReceived = (RadioButton) linearLayout.findViewById(R.id.radio_received);
        radioFeedbacked = (RadioButton) linearLayout.findViewById(R.id.radio_feedbacked);
        radioNohandled = (RadioButton) linearLayout.findViewById(R.id.radio_nohandled);
        // RadioGroup的两个RadioButton的三个点击事件
        taskRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    if (checkedId == R.id.radio_nohandled) {
                        if(!NetWorkUtil.isNetworkConnected(getActivity())){
                            datas.clear();
                            List<TaskItemBean2> tibList = LitePal.findAll(TaskItemBean2.class);
                            for(int i = 0; i < tibList.size(); i++){
                                // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                                // IssuedUser就是admin跟userId是不同的，这样的话进入执行中 WOIssuedUser为派工人
                                if(tibList.get(i).getWOType().equals("1") &&
                                        tibList.get(i).getWOState().equals("false") &&
                                        !tibList.get(i).getWOIssuedUser().equals(userId) &&
                                        TextUtils.isEmpty(tibList.get(i).getWOReceiveDate()) &&
                                        TextUtils.isEmpty(tibList.get(i).getWOFeedback())){
                                    datas.add(tibList.get(i));
                                }
                            }
                            mAdapter.notifyDataSetChanged();
                        } else {
                            // 未处理
                            getNoHandledData();
                        }
                    } else if (checkedId == R.id.radio_received) {
                        if(!NetWorkUtil.isNetworkConnected(getActivity())){
                            datas.clear();
                            List<TaskItemBean2> tibList = LitePal.findAll(TaskItemBean2.class);
                            for(int i = 0; i < tibList.size(); i++){
                                // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                                // IssuedUser就是admin跟userId是不同的，这样的话进入执行中 WOIssuedUser为派工人
                                // 接收时间为不为空，反馈为空
                                if(tibList.get(i).getWOType().equals("1") &&
                                        tibList.get(i).getWOState().equals("false") &&
                                        !tibList.get(i).getWOIssuedUser().equals(userId) &&
                                        !TextUtils.isEmpty(tibList.get(i).getWOReceiveDate()) &&
                                        TextUtils.isEmpty(tibList.get(i).getWOFeedback())){
                                    datas.add(tibList.get(i));
                                }
                            }
                            mAdapter.notifyDataSetChanged();
                        } else {
                            // 已接收
                            getReceivedData();
                        }
                    } else if (checkedId == R.id.radio_feedbacked){
                        if(!NetWorkUtil.isNetworkConnected(getActivity())){
                            datas.clear();
                            List<TaskItemBean2> tibList = LitePal.findAll(TaskItemBean2.class);
                            for(int i = 0; i < tibList.size(); i++){
                                // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                                // IssuedUser就是admin跟userId是不同的，这样的话进入执行中 WOIssuedUser为派工人
                                // 只要保证反馈不为空就可以
                                if(tibList.get(i).getWOType().equals("1") &&
                                        tibList.get(i).getWOState().equals("false") &&
                                        !tibList.get(i).getWOIssuedUser().equals(userId) &&
                                        !TextUtils.isEmpty(tibList.get(i).getWOFeedback())){
                                    datas.add(tibList.get(i));
                                }
                            }
                            mAdapter.notifyDataSetChanged();
                        } else {
                            // 已反馈
                            getFeedbackedData();
                        }
                    }
                }
            }
        });
        loadding = new Loadding(getActivity());
        intent = new Intent();
        prefs1 = getActivity().getSharedPreferences("UserInfo", 0);
        editor = prefs1.edit();
        jsonObject1 = new JSONObject();
        jsonObject2 = new JSONObject();
        jsonArray = new JSONArray();
        return linearLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e(TAG, "onActivityCreated: ");
        prefs1 = getActivity().getSharedPreferences("UserInfo", 0);

        mAdapter = new TaskRvAdapter3(getActivity(), datas);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mAdapter);
        // 所有点击事件的初始化
        initRecyclerViewListener();
        gatewayAddress = prefs1.getString("add", "");
        userId = String.valueOf(prefs1.getInt("UserID", 0));
        username = prefs1.getString("userfullname", "");
        // 未处理
        //getNoHandledData();
    }

    // 所有点击事件的初始化
    private void initRecyclerViewListener() {

        // 这是整个子项控件的
        mAdapter.setOnItemClickListener(new TaskRvAdapter3.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // 这个判断直接复制的TaskListActivity里面的getData()里面的判断方法
                if(!DoubleClickUtils.isFastDoubleClick()){
                    TaskItemBean2 bean = datas.get(position);
                    editor.putString("ownid", bean.getWOID());
                    editor.apply();
                    intent.setClass(getActivity(), DetailTaskActivity.class);
                    intent.putExtra("taskitembean1", bean);
                    startActivity(intent);
                }
            }
        });

        // 接收按钮
        mAdapter.setOnReceiverClickEvent(new TaskRvAdapter3.onReceiverClickEvent() {
            @Override
            public void onReceiverClick(int position) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    TaskItemBean2 item = datas.get(position);
                    if(item.getWOFeedback() == null || item.getWOFeedback().equals("")){
                        // 通过WOID得到故障id
                        getFbId(item);
                    } else {
                        toast("已反馈工单不能进行接收");
                    }
                }
            }
        });

        // 左边显示摘要按钮
        mAdapter.setOnLeftButtonClickEvent(new TaskRvAdapter3.onLeftButtonClickEvent() {
            @Override
            public void onLeftButtonClick(int position) {
                TaskItemBean2 bean = datas.get(position);
                // toast("左边按钮，显示摘要" + position);
            }
        });

        // 右边 处理该任务按钮
        mAdapter.setOnRightButtonClickEvent(new TaskRvAdapter3.onRightButtonClickEvent() {
            @Override
            public void onRightButtonClick(int position) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    TaskItemBean2 bean = datas.get(position);
                    editor.putString("ownid", bean.getWOID());
                    editor.commit();
                    intent.setClass(getActivity(), DetailTaskActivity.class);
                    intent.putExtra("taskitembean1", bean);
                    startActivity(intent);
                }
            }
        });
    }

    // 已接收的数据
    private void getReceivedData() {
        Date te = new Date();
        SimpleDateFormat mat = new SimpleDateFormat("yyyy");
        String str = mat.format(te);
        loadding.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(getActivity(), ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        }
        params.addBodyParameter("userID", userId);
        params.addBodyParameter("pageSize", "10000");
        params.addBodyParameter("beginDate", str + "-01-01");
        params.addBodyParameter("endDate", str + "-12-31");
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
                        dataSql.clear();
                        Type listType = new TypeToken<List<TaskItemBean2>>() {}.getType();
                        Gson gson = new Gson();
                        gsonDatas = gson.fromJson(object1.getString("Data"), listType);
                        // 存储TaskItemBean对应的数据库
                        for(int i = 0; i < gsonDatas.size(); i++){
                            // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                            // IssuedUser就是admin跟userId是不同的，这样的话进入执行中
                            if(gsonDatas.get(i).getWOType().equals("1") &&
                                    gsonDatas.get(i).getWOState().equals("false") &&
                                    !gsonDatas.get(i).getWOIssuedUser().equals(userId) /*&&
                                    gsonDatas.get(i).getIsIssue().equals("false")*/){
                                dataSql.add(gsonDatas.get(i));
                            }
                        }
                        LitePal.deleteAll(TaskItemBean2.class);
                        // 将datas里面的数据存储到SQLite里面去
                        for(int i = 0; i < dataSql.size();i++){
                            TaskItemBean2 bean = dataSql.get(i);
                            TaskItemBean2 t = new TaskItemBean2();
                            t.setWOID(bean.getWOID());
                            t.setWOTitle(bean.getWOTitle());
                            t.setWOContent(bean.getWOContent());
                            t.setVoice(bean.getVoice());
                            t.setWOState(bean.getWOState());
                            t.setWOIssuedDate(bean.getWOIssuedDate());
                            t.setWOIssuedUser(bean.getWOIssuedUser());
                            t.setWOReceiveDate(bean.getWOReceiveDate());
                            t.setWOReceiveUser(bean.getWOReceiveUser());
                            t.setWOItemsNum(bean.getWOItemsNum());
                            t.setWOPerformNum(bean.getWOPerformNum());
                            t.setWOBeginDate(bean.getWOBeginDate());
                            t.setWOEndDate(bean.getWOEndDate());
                            t.setWOCreateDate(bean.getWOCreateDate());
                            t.setWOType(bean.getWOType());
                            t.setWOExpectedTime(bean.getWOExpectedTime());
                            t.setUserName(bean.getUserName());
                            t.setReceiveUser(bean.getReceiveUser());
                            t.setPFID(bean.getPFID());
                            t.setEquipmentID(bean.getEquipmentID());
                            t.setDeviceName(bean.getDeviceName());
                            t.setIsIssue(bean.getIsIssue());
                            t.setIssueName(bean.getIssueName());
                            t.setEquipmentNo(bean.getEquipmentNo());
                            t.setDevCheckID(bean.getDevCheckID());
                            t.save();
                        }
                        for(int i = 0; i < gsonDatas.size(); i++){
                            // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                            // IssuedUser就是admin跟userId是不同的，这样的话进入执行中 WOIssuedUser为派工人
                            // 接收时间不为空，反馈为空
                            if(gsonDatas.get(i).getWOType().equals("1") &&
                                    gsonDatas.get(i).getWOState().equals("false") &&
                                    !gsonDatas.get(i).getWOIssuedUser().equals(userId) &&
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
        Date te = new Date();
        SimpleDateFormat mat = new SimpleDateFormat("yyyy");
        String str = mat.format(te);
        loadding.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(getActivity(), ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        }
        params.addBodyParameter("userID", userId);
        params.addBodyParameter("pageSize", "10000");
        params.addBodyParameter("beginDate", str + "-01-01");
        params.addBodyParameter("endDate", str + "-12-31");
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
                        dataSql.clear();
                        Type listType = new TypeToken<List<TaskItemBean2>>() {}.getType();
                        Gson gson = new Gson();
                        gsonDatas = gson.fromJson(object1.getString("Data"), listType);
                        // 存储TaskItemBean对应的数据库
                        for(int i = 0; i < gsonDatas.size(); i++){
                            // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                            // IssuedUser就是admin跟userId是不同的，这样的话进入执行中
                            if(gsonDatas.get(i).getWOType().equals("1") &&
                                    gsonDatas.get(i).getWOState().equals("false") &&
                                    !gsonDatas.get(i).getWOIssuedUser().equals(userId) /*&&
                                    gsonDatas.get(i).getIsIssue().equals("false")*/){
                                dataSql.add(gsonDatas.get(i));
                            }
                        }
                        LitePal.deleteAll(TaskItemBean2.class);
                        // 将datas里面的数据存储到SQLite里面去
                        for(int i = 0; i < dataSql.size();i++){
                            TaskItemBean2 bean = dataSql.get(i);
                            TaskItemBean2 t = new TaskItemBean2();
                            t.setWOID(bean.getWOID());
                            t.setWOTitle(bean.getWOTitle());
                            t.setWOContent(bean.getWOContent());
                            t.setVoice(bean.getVoice());
                            t.setWOState(bean.getWOState());
                            t.setWOIssuedDate(bean.getWOIssuedDate());
                            t.setWOIssuedUser(bean.getWOIssuedUser());
                            t.setWOReceiveDate(bean.getWOReceiveDate());
                            t.setWOReceiveUser(bean.getWOReceiveUser());
                            t.setWOItemsNum(bean.getWOItemsNum());
                            t.setWOPerformNum(bean.getWOPerformNum());
                            t.setWOBeginDate(bean.getWOBeginDate());
                            t.setWOEndDate(bean.getWOEndDate());
                            t.setWOCreateDate(bean.getWOCreateDate());
                            t.setWOType(bean.getWOType());
                            t.setWOExpectedTime(bean.getWOExpectedTime());
                            t.setUserName(bean.getUserName());
                            t.setReceiveUser(bean.getReceiveUser());
                            t.setPFID(bean.getPFID());
                            t.setEquipmentID(bean.getEquipmentID());
                            t.setDeviceName(bean.getDeviceName());
                            t.setIsIssue(bean.getIsIssue());
                            t.setIssueName(bean.getIssueName());
                            t.setEquipmentNo(bean.getEquipmentNo());
                            t.setDevCheckID(bean.getDevCheckID());
                            t.save();
                        }
                        for(int i = 0; i < gsonDatas.size(); i++){
                            // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                            // IssuedUser就是admin跟userId是不同的，这样的话进入执行中 WOIssuedUser为派工人
                            // 只要保证反馈不为空就可以
                            if(gsonDatas.get(i).getWOType().equals("1") &&
                                    gsonDatas.get(i).getWOState().equals("false") &&
                                    !gsonDatas.get(i).getWOIssuedUser().equals(userId) &&
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

    // 未处理
    private void getNoHandledData() {
        Date te = new Date();
        SimpleDateFormat mat = new SimpleDateFormat("yyyy");
        String str = mat.format(te);
        loadding.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(getActivity(), ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        }
        params.addBodyParameter("userID", userId);
        params.addBodyParameter("pageSize", "10000");
        params.addBodyParameter("beginDate", str + "-01-01");
        params.addBodyParameter("endDate", str + "-12-31");
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
                        dataSql.clear();
                        Type listType = new TypeToken<List<TaskItemBean2>>() {}.getType();
                        Gson gson = new Gson();
                        gsonDatas = gson.fromJson(object1.getString("Data"), listType);
                        // 存储TaskItemBean对应的数据库
                        for(int i = 0; i < gsonDatas.size(); i++){
                            // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                            // IssuedUser就是admin跟userId是不同的，这样的话进入执行中
                            if(gsonDatas.get(i).getWOType().equals("1") &&
                                    gsonDatas.get(i).getWOState().equals("false") &&
                                    !gsonDatas.get(i).getWOIssuedUser().equals(userId) /*&&
                                    gsonDatas.get(i).getIsIssue().equals("false")*/){
                                dataSql.add(gsonDatas.get(i));
                            }
                        }
                        LitePal.deleteAll(TaskItemBean2.class);
                        // 将datas里面的数据存储到SQLite里面去
                        for(int i = 0; i < dataSql.size();i++){
                            TaskItemBean2 bean = dataSql.get(i);
                            TaskItemBean2 t = new TaskItemBean2();
                            t.setWOID(bean.getWOID());
                            t.setWOTitle(bean.getWOTitle());
                            t.setWOContent(bean.getWOContent());
                            t.setVoice(bean.getVoice());
                            t.setWOState(bean.getWOState());
                            t.setWOIssuedDate(bean.getWOIssuedDate());
                            t.setWOIssuedUser(bean.getWOIssuedUser());
                            t.setWOReceiveDate(bean.getWOReceiveDate());
                            t.setWOReceiveUser(bean.getWOReceiveUser());
                            t.setWOItemsNum(bean.getWOItemsNum());
                            t.setWOPerformNum(bean.getWOPerformNum());
                            t.setWOBeginDate(bean.getWOBeginDate());
                            t.setWOEndDate(bean.getWOEndDate());
                            t.setWOCreateDate(bean.getWOCreateDate());
                            t.setWOType(bean.getWOType());
                            t.setWOExpectedTime(bean.getWOExpectedTime());
                            t.setUserName(bean.getUserName());
                            t.setReceiveUser(bean.getReceiveUser());
                            t.setPFID(bean.getPFID());
                            t.setEquipmentID(bean.getEquipmentID());
                            t.setDeviceName(bean.getDeviceName());
                            t.setIsIssue(bean.getIsIssue());
                            t.setIssueName(bean.getIssueName());
                            t.setEquipmentNo(bean.getEquipmentNo());
                            t.setDevCheckID(bean.getDevCheckID());
                            t.save();
                        }
                        for(int i = 0; i < gsonDatas.size(); i++){
                            // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                            // IssuedUser就是admin跟userId是不同的，这样的话进入执行中 WOIssuedUser为派工人
                            if(gsonDatas.get(i).getWOType().equals("1") &&
                                    gsonDatas.get(i).getWOState().equals("false") &&
                                    !gsonDatas.get(i).getWOIssuedUser().equals(userId) &&
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

    // 通过WOID得到故障id
    private void getFbId(final TaskItemBean2 task) {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(getActivity(), ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadFBByWoID");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadFBByWoID");
        }
        params.addBodyParameter("guid", "");
        params.addBodyParameter("id", task.getWOID());
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {}

            @Override
            public void onError(Throwable ex, boolean arg1) { }

            @Override
            public void onFinished() {}

            @Override
            public void onSuccess(String arg0) {
                try {
                    Log.e("SendTaskActivity", arg0);
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        intent.setClass(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")){
                        JSONObject jsonObject = new JSONObject(object1.getString("Data"));
                        fbId = jsonObject.getString("ID");
                        if(TextUtils.isEmpty(fbId)){
                            toast("故障id为空不能接收维修工单");
                        } else {
                            try{
                                Date date2 = new Date();
                                String endDate = format1.format(date2);// 结束时间（年月日时分秒）
                                totalNumber = Integer.valueOf(task.getWOItemsNum()); // 巡检工单的总数量
                                changeNumber = Integer.valueOf(task.getWOPerformNum()); // 巡检工单的子数量
                                // workorder 字段    这个不用jsonArray
                                jsonObject1.put("WOID", task.getWOID());
                                jsonObject1.put("WOTitle", task.getWOTitle());
                                jsonObject1.put("WOContent", task.getWOContent());
                                jsonObject1.put("Voice", task.getVoice());
                                jsonObject1.put("WOState", 0);
                                jsonObject1.put("WOIssuedDate", task.getWOIssuedDate()); // 派工时间
                                jsonObject1.put("WOIssuedUser", task.getWOIssuedUser()); // 派工人
                                // 这个字段是activity进入的时候就获取的年月日时分秒
                                jsonObject1.put("WOReceiveDate", endDate);
                                jsonObject1.put("WOReceiveUser", task.getWOReceiveUser());
                                jsonObject1.put("WOItemsNum", totalNumber);
                                jsonObject1.put("WOPerformNum", changeNumber);
                                jsonObject1.put("WOBeginDate", task.getWOBeginDate());
                                // 这个字段是点击发送按钮获取的当前的年月日时分秒
                                jsonObject1.put("WOEndDate", task.getWOEndDate());
                                jsonObject1.put("WOCreateDate", task.getWOCreateDate());
                                jsonObject1.put("WOType", "1");
                                jsonObject1.put("WOExpectedTime", task.getWOExpectedTime());
                                jsonObject1.put("FBID", fbId);
                                jsonObject1.put("WOFeedback", task.getWOFeedback());
                                // detail  字段  这个用jsonArray
                                jsonObject2.put("WOID", task.getWOID());
                                jsonObject2.put("Num", "1");
                                jsonObject2.put("OrderContent", task.getWOContent());
                                // 这个一直没有改，直到18年5月25日才改成1,以前是“1”
                                jsonObject2.put("OrderState", 0);
                                jsonObject2.put("UserID", userId);
                                jsonObject2.put("UserName", username);// 用户名
                                jsonObject2.put("DateTime", endDate);
                                jsonArray.put(jsonObject2);
                            } catch (JSONException e){
                                e.printStackTrace();
                            }
                            // 如果没有WOReceiveDate的话才更新工单状态，有WOReceiveDate的话不更新工单状态，
                            // 这个更新工单状态主要是用于上报接收时间的
                            if("".equals(task.getWOReceiveDate()) || task.getWOReceiveDate() == null){
                                // 更新工单状态
                                getUpdateWorkOrder();
                            }
                        }
                    } else {
                        toast(object1.getString("Message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 更新工单状态
    private void getUpdateWorkOrder() {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(getActivity(), ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/UpdateWorkOrder");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/UpdateWorkOrder");
        }
        params.addBodyParameter("workorder", jsonObject1.toString());
        params.addBodyParameter("detail", jsonArray.toString());
        params.addBodyParameter("guid", "");
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                //toast(ex.getMessage());
                toast("服务器异常");
            }

            @Override
            public void onFinished() {
                if (loadding.isShow()) {
                    loadding.close();
                }
            }

            @Override
            public void onSuccess(String arg0) {
                try{
                    Intent intent = new Intent();
                    CommonResponseBean commonResponseBean = new CommonResponseBean().getcommenHit(arg0);
                    if (commonResponseBean.getCode() == 1) {
                        //toast(commonResponseBean.getMessage());
                        toast("维修工单已接收！");
                        // 这个用于点击接收按钮更新工单状态之后的刷新界面操作
                        refreshData();
                    } else if (commonResponseBean.getCode() == 0) {
                        intent.setClass(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    } else {
                        toast(commonResponseBean.getMessage());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    // 这个用于点击接收按钮更新工单状态之后的刷新界面操作
    private void refreshData() {
        Date te = new Date();
        SimpleDateFormat mat = new SimpleDateFormat("yyyy");
        String str = mat.format(te);
        loadding.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(getActivity(), ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        }
        params.addBodyParameter("userID", userId);
        params.addBodyParameter("pageSize", "10000");
        params.addBodyParameter("beginDate", str + "-01-01");
        params.addBodyParameter("endDate", str + "-12-31");
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
                        Type listType = new TypeToken<List<TaskItemBean2>>() {}.getType();
                        Gson gson = new Gson();
                        gsonDatas = gson.fromJson(object1.getString("Data"), listType);
                        for(int i = 0; i < gsonDatas.size(); i++){
                            // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                            // IssuedUser就是admin跟userId是不同的，这样的话进入执行中 WOIssuedUser为派工人
                            if(gsonDatas.get(i).getWOType().equals("1") &&
                                    gsonDatas.get(i).getWOState().equals("false") &&
                                    !gsonDatas.get(i).getWOIssuedUser().equals(userId) &&
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

    private void toast(String text){
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "onStart: " );
        if (radioReceived.isChecked()) {
            if(!NetWorkUtil.isNetworkConnected(getActivity())){
                datas.clear();
                List<TaskItemBean2> tibList = LitePal.findAll(TaskItemBean2.class);
                for(int i = 0; i < tibList.size(); i++){
                    // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                    // IssuedUser就是admin跟userId是不同的，这样的话进入执行中 WOIssuedUser为派工人
                    // 接收时间为不为空，反馈为空
                    if(tibList.get(i).getWOType().equals("1") &&
                            tibList.get(i).getWOState().equals("false") &&
                            !tibList.get(i).getWOIssuedUser().equals(userId) &&
                            !TextUtils.isEmpty(tibList.get(i).getWOReceiveDate()) &&
                            TextUtils.isEmpty(tibList.get(i).getWOFeedback())){
                        datas.add(tibList.get(i));
                    }
                }
                mAdapter.notifyDataSetChanged();
            } else {
                // 已接收
                getReceivedData();
            }
        } else if (radioFeedbacked.isChecked()){
            if(!NetWorkUtil.isNetworkConnected(getActivity())){
                datas.clear();
                List<TaskItemBean2> tibList = LitePal.findAll(TaskItemBean2.class);
                for(int i = 0; i < tibList.size(); i++){
                    // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                    // IssuedUser就是admin跟userId是不同的，这样的话进入执行中 WOIssuedUser为派工人
                    // 只要保证反馈不为空就可以
                    if(tibList.get(i).getWOType().equals("1") &&
                            tibList.get(i).getWOState().equals("false") &&
                            !tibList.get(i).getWOIssuedUser().equals(userId) &&
                            !TextUtils.isEmpty(tibList.get(i).getWOFeedback())){
                        datas.add(tibList.get(i));
                    }
                }
                mAdapter.notifyDataSetChanged();
            } else {
                // 已反馈
                getFeedbackedData();
            }
        } else if (radioNohandled.isChecked()) {
            if(!NetWorkUtil.isNetworkConnected(getActivity())){
                datas.clear();
                List<TaskItemBean2> tibList = LitePal.findAll(TaskItemBean2.class);
                for(int i = 0; i < tibList.size(); i++){
                    // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                    // IssuedUser就是admin跟userId是不同的，这样的话进入执行中 WOIssuedUser为派工人
                    if(tibList.get(i).getWOType().equals("1") &&
                            tibList.get(i).getWOState().equals("false") &&
                            !tibList.get(i).getWOIssuedUser().equals(userId) &&
                            TextUtils.isEmpty(tibList.get(i).getWOReceiveDate()) &&
                            TextUtils.isEmpty(tibList.get(i).getWOFeedback())){
                        datas.add(tibList.get(i));
                    }
                }
                mAdapter.notifyDataSetChanged();
            } else {
                // 未处理
                getNoHandledData();
            }
        }
    }
}
