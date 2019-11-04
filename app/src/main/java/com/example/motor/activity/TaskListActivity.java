package com.example.motor.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.adapter.TaskRvAdapter3;
import com.example.motor.base.BaseActivity;
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
import org.litepal.tablemanager.Connector;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Author : yanftch
 * Date   : 2018/3/21
 * Time   : 09:46
 * Desc   : 维修任务
 *
 */

public class TaskListActivity extends BaseActivity {

    private static final String TAG = "TaskListActivity";
    private RadioGroup taskRadioGroup;
    private RecyclerView recyclerView;
    private boolean MultipleChoice = false;  // 多选模式
    private List<TaskItemBean2> datas = new ArrayList<>();
    private List<TaskItemBean2> gsonDatas = new ArrayList<>();
    private TaskRvAdapter3 mAdapter;
    private LinearLayout llBottomPanel;  // 底部控制面板
    private Loadding loadding;
    private SharedPreferences prefs1;
    private SharedPreferences.Editor editor;
    private String gatewayAddress, userId, guidString, username = "", fbId = "", workOrderNumber = "";
    private SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private int totalNumber, changeNumber;

    // 底部俩按钮
    private TextView tvBottomCancel;
    private TextView tvBottomSendTask;
    private Intent intent;
    private Activity mActivity;
    private JSONArray jsonArray;
    private JSONObject jsonObject1, jsonObject2;

    @Override
    public int setLayout() {
        return R.layout.activity_task_list;
    }

    @Override
    public void setTitle() {
        mBaseTitleBarView.setTitleText("维修待办任务");
        mBaseTitleBarView.setRightTitleGone();
        mBaseTitleBarView.setLeftDrawable(-1);
    }

    @Override
    public void initWidget() {
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);
        initView();
        if(!NetWorkUtil.isNetworkConnected(mActivity)){
            datas.clear();
            List<TaskItemBean2> tibList = LitePal.findAll(TaskItemBean2.class);
            datas.addAll(tibList);
            mAdapter.notifyDataSetChanged();
        } else {
            http_getUnDoData();
        }
        initListener();
    }

    @Override
    public void widgetClick(View v) {}

    @Override
    public void onTitleLeftPressed() {
        if(!DoubleClickUtils.isFastDoubleClick()){
            onBackPressed();
        }
    }

    @Override
    public void onTitleRightTextPressed() {
        // 如果集合的数量等于0的话就不允许它往下执行
        if(datas.size() > 0 && !DoubleClickUtils.isFastDoubleClick()){
            mAdapter.changeModel(!MultipleChoice);
            showOrHiddenControlPanel(!MultipleChoice);
            MultipleChoice = !MultipleChoice;
            //toast("点击右边的多选---" + MultipleChoice);
        }
    }

    private void initView(){
        intent = new Intent();
        mActivity = this;
        loadding = new Loadding(this);
        prefs1 = getSharedPreferences("UserInfo", 0);
        editor = prefs1.edit();
        jsonObject1 = new JSONObject();
        jsonObject2 = new JSONObject();
        jsonArray = new JSONArray();
        workOrderNumber = getIntent().getStringExtra("workorder_number");
        // 偏好设置里面的username是id，userfullname是文字
        username = prefs1.getString("userfullname", "");
        gatewayAddress = prefs1.getString("add", "");
        userId = String.valueOf(prefs1.getInt("UserID", 0));
        guidString = prefs1.getString("guid", "");
        tvBottomCancel = (TextView) findViewById(R.id.tvBottomCancel);  // 取消按钮
        tvBottomSendTask = (TextView) findViewById(R.id.tvBottomSendTask);  // 派发任务按钮
        llBottomPanel = (LinearLayout) findViewById(R.id.llBottomPanel);   // 取消按钮和派发任务按钮的容器
        taskRadioGroup = (RadioGroup) findViewById(R.id.taskRadioGroup);
        // 以下为recyclerView的一些代码
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new TaskRvAdapter3(context, datas);
        recyclerView.setAdapter(mAdapter);
        // 创建SQLite数据库
        Connector.getDatabase();
    }

    private void initListener() {

        // RadioGroup的两个RadioButton的两个点击事件
        taskRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    if (checkedId == R.id.radioButtonUnDo) {
                        // 如果无网络就走SQLite
                        if(!NetWorkUtil.isNetworkConnected(mActivity)){
                            datas.clear();
                            List<TaskItemBean2> tibList = LitePal.findAll(TaskItemBean2.class);
                            datas.addAll(tibList);
                            mAdapter.notifyDataSetChanged();
                        } else {
                            // 执行中
                            http_getUnDoData();
                        }
                        // 如果是多选的话那就让下面的面板显示出来
                        if(MultipleChoice){
                            llBottomPanel.setVisibility(View.VISIBLE);
                        }
                    } else if (checkedId == R.id.radioButtonForward){
                        // 如果没有网络就直接清空集合并通知适配器改变
                        if(!NetWorkUtil.isNetworkConnected(mActivity)){
                            toast("无网络");
                            datas.clear();
                            mAdapter.notifyDataSetChanged();
                        } else {
                            // 已转发
                            http_getForwardData();
                        }
                        // 只要进入已转发界面直接就将其下面的面板给GONE
                        llBottomPanel.setVisibility(View.GONE);
                    } else if (checkedId == R.id.radioButtonDone) {
                        // 如果没有网络就直接清空集合并通知适配器改变
                        if(!NetWorkUtil.isNetworkConnected(mActivity)){
                            toast("无网络");
                            datas.clear();
                            mAdapter.notifyDataSetChanged();
                        } else {
                            // 已处理
                            http_getDoneData();
                        }
                        // 只要进入已处理界面直接就将其下面的面板给GONE
                        llBottomPanel.setVisibility(View.GONE);
                    }
                }
            }
        });

        initRecyclerViewListener();

        // 取消按钮的点击事件
        tvBottomCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    //Log.e(TAG, "onClick: MultipleChoice====" + MultipleChoice);
                    MultipleChoice = !MultipleChoice;
                    showOrHiddenControlPanel(MultipleChoice);
                    mAdapter.changeModel(MultipleChoice);
                    //toast("取消");
                }
            }
        });

        // 派发任务的点击事件
        tvBottomSendTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    MultipleChoice = !MultipleChoice;
                    showOrHiddenControlPanel(MultipleChoice);
                    mAdapter.changeModel(MultipleChoice);
                    //getCurrentChoosenItems()返回的是一个集合，将这个集合赋值为currentChoosenItems
                    List<TaskItemBean2> currentChoosenItems = mAdapter.getCurrentChoosenItems();
                    if(currentChoosenItems.size() == 0) return;
                    intent.setClass(TaskListActivity.this, SendTaskActivity.class);
                    intent.putExtra("currentChooseItems", (Serializable) currentChoosenItems);
                    startActivity(intent);
                    finish();
                    mAdapter.resetSelectedStatus();
                }
            }
        });
    }

    private void initRecyclerViewListener() {

        // 这是整个子项控件的
        mAdapter.setOnItemClickListener(new TaskRvAdapter3.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // 这个判断直接复制的TaskListActivity里面的getData()里面的判断方法
                if(datas.get(position).getWOType().equals("1") &&
                        datas.get(position).getWOState().equals("false") &&
                        !datas.get(position).getWOIssuedUser().equals(userId) &&
                        !DoubleClickUtils.isFastDoubleClick()){
                    TaskItemBean2 bean = datas.get(position);
                    editor.putString("ownid", bean.getWOID());
                    editor.apply();
                    intent.setClass(TaskListActivity.this, DetailTaskActivity.class);
                    intent.putExtra("taskitembean1", bean);
                    startActivity(intent);
                    finish();
                }
            }
        });

        // 接收按钮
        mAdapter.setOnReceiverClickEvent(new TaskRvAdapter3.onReceiverClickEvent() {
            @Override
            public void onReceiverClick(int position) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    TaskItemBean2 item = datas.get(position);
                    // 通过WOID得到故障id
                    getFbId(item);
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
                    editor.apply();
                    intent.setClass(TaskListActivity.this, DetailTaskActivity.class);
                    intent.putExtra("taskitembean1", bean);
                    startActivity(intent);
                    finish();
                }
            }
        });

        // 最右边的圆形图片的点击事件
        mAdapter.setOnImageClickEvent(new TaskRvAdapter3.onImageClickEvent() {
            @Override
            public void onImageClick(int position) {
                TaskItemBean2 bean = datas.get(position);
                if (MultipleChoice) {
                    bean.setMultipleChoiceSelected(!bean.isMultipleChoiceSelected());
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * 获取执行中的列表数据
     */
    private void http_getUnDoData() {
        getData();
    }

    /**
     * 获取已处理的列表数据
     */
    private void http_getDoneData() {
        getData2();
    }

    /**
     * 获取已转发的列表数据
     */
    private void http_getForwardData() {
        getData3();
    }

    /**
     * 是否显示下边的俩按钮。如果集合的第一个元素的WOState是true就是已处理的界面，将其隐藏，这样的目的就
     * 是直接让它不能够点击；如果集合的第一个元素的WOState是false就是未处理的界面，将其显示，这样的目的是
     * 让它能够被点击
     * @param show TRUE-显示
     */
    private void showOrHiddenControlPanel(boolean show) {
        if (show) {
            // 这个判断直接复制的TaskListActivity里面的getData()里面的判断方法
            if(datas.get(0).getWOType().equals("1") &&
                    datas.get(0).getWOState().equals("false") &&
                    (!datas.get(0).getWOIssuedUser().equals(userId))){
                llBottomPanel.setVisibility(View.VISIBLE);
            } else {
                llBottomPanel.setVisibility(View.GONE);
            }
        } else {
            llBottomPanel.setVisibility(View.GONE);
        }
    }

    // 执行中  如果派工人与当前账号userId不一样的话就是执行中的任务
    private void getData() {
        datas.clear();
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String time = format.format(date);
        Log.e("时间", time);
        loadding.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        }
        params.addBodyParameter("userID", userId);
        params.addBodyParameter("pageSize", "100");
        params.addBodyParameter("beginDate", "1900-01-01");
        params.addBodyParameter("endDate", "2200-12-31");
        params.addBodyParameter("guid", guidString);
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
                        intent.setClass(TaskListActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")){
                        Type listType = new TypeToken<List<TaskItemBean2>>() {}.getType();
                        Gson gson = new Gson();
                        gsonDatas = gson.fromJson(object1.getString("Data"), listType);
                        for(int i = 0; i < gsonDatas.size(); i++){
                            // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                            // IssuedUser就是admin跟userId是不同的，这样的话进入执行中
                            if(gsonDatas.get(i).getWOType().equals("1") &&
                                    gsonDatas.get(i).getWOState().equals("false") &&
                                    !gsonDatas.get(i).getWOIssuedUser().equals(userId) /*&&
                                    gsonDatas.get(i).getIsIssue().equals("false")*/){
                                datas.add(gsonDatas.get(i));
                            }
                        }
                        LitePal.deleteAll(TaskItemBean2.class);
                        // 将datas里面的数据存储到SQLite里面去
                        for(int i = 0; i < datas.size();i++){
                            TaskItemBean2 bean = datas.get(i);
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
                        mAdapter.notifyDataSetChanged();
                        // 目的：实现点击推送更新接收时间的功能。
                        // 点击推送跳转到TaskListActivity之后在生成datas之后再更新它的工单状态，
                        // 因为如果直接在initView里面进行如下操作的话会出现一个问题：工单号码与datas
                        // 里面的比对不到，因为可能datas还没有生成，这样就导致有和无的比对，从而造成异常。
                        /*if(workOrderNumber != null && !workOrderNumber.equals("")){
                            // 通过WOID得到故障id，点击推送之后的
                            getFbId2(workOrderNumber);
                        }*/
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

    // 已转发  如果派工人与当前账号userId一样的话就是已转发的任务
    private void getData3() {
        datas.clear();
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String time = format.format(date);
        loadding.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        }
        params.addBodyParameter("userID", userId);
        params.addBodyParameter("pageSize", "100");
        params.addBodyParameter("beginDate", "1900-01-01");
        params.addBodyParameter("endDate", "2100-12-31");
        params.addBodyParameter("guid", guidString);
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
            public void onFinished() { }

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        intent.setClass(TaskListActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")){
                        Type listType = new TypeToken<List<TaskItemBean2>>() {}.getType();
                        Gson gson = new Gson();
                        gsonDatas = gson.fromJson(object1.getString("Data"), listType);
                        for(int i = 0; i < gsonDatas.size(); i++){
                            // 1代表维修任务，第一个false代表是执行中，第三个判断就是如果转发的话IssuedUser
                            // 是跟userId一样的，这样的工单是进入已转发的，这样就对了
                            if(gsonDatas.get(i).getWOType().equals("1") &&
                                    gsonDatas.get(i).getWOState().equals("false") &&
                                    gsonDatas.get(i).getWOIssuedUser().equals(userId) /*&&
                                    gsonDatas.get(i).getIsIssue().equals("true")*/){
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

    // 已处理
    private void getData2() {
        datas.clear();
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String time = format.format(date);
        loadding.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        }
        params.addBodyParameter("userID", userId);
        params.addBodyParameter("pageSize", "100");
        params.addBodyParameter("beginDate", "1900-01-01");
        params.addBodyParameter("endDate", "2100-12-31");
        params.addBodyParameter("guid", guidString);
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
            public void onFinished() { }

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        intent.setClass(TaskListActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")){
                        Type listType = new TypeToken<List<TaskItemBean2>>() {}.getType();
                        Gson gson = new Gson();
                        gsonDatas = gson.fromJson(object1.getString("Data"), listType);
                        //toast(String.valueOf(gsonDatas.size()));
                        for(int i = 0; i < gsonDatas.size(); i++){
                            // 1代表维修任务，第一个true代表是已处理
                            if(gsonDatas.get(i).getWOType().equals("1") &&
                                    gsonDatas.get(i).getWOState().equals("true")){
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
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadFBByWoID");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadFBByWoID");
        }
        params.addBodyParameter("guid", guidString);
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
                        intent.setClass(mActivity, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")){
                        JSONObject jsonObject = new JSONObject(object1.getString("Data"));
                        fbId = jsonObject.getString("ID");
                        if(TextUtils.isEmpty(fbId)){
                            toast("故障id为空不能提报");
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
                                jsonObject1.put("WOFeedback", "");
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

    // 通过WOID得到故障id，点击推送之后的
    private void getFbId2(final String s) {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadFBByWoID");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadFBByWoID");
        }
        params.addBodyParameter("guid", guidString);
        params.addBodyParameter("id", s);
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
                        intent.setClass(mActivity, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")){
                        JSONObject jsonObject = new JSONObject(object1.getString("Data"));
                        fbId = jsonObject.getString("ID");
                        if(TextUtils.isEmpty(fbId)){
                            toast("故障id为空不能提报");
                        } else {
                            Date date2 = new Date();
                            String endDate = format1.format(date2);// 结束时间（年月日时分秒）
                            // 目的：取出对象用于getUpdateWorkOrder的数据上传。
                            // 将推送采集的WOID与datas里面的实体对象的WOID进行比对，相同就拿出来使用它的数据。
                            for(int i = 0; i < datas.size(); i++){
                                if(datas.get(i).getWOID().equals(s)){
                                    TaskItemBean2 bean = datas.get(i);
                                    try{
                                        totalNumber = Integer.valueOf(bean.getWOItemsNum()); // 工单的总数量
                                        changeNumber = Integer.valueOf(bean.getWOPerformNum()); // 工单的子数量
                                        // workorder 字段    这个不用jsonArray
                                        jsonObject1.put("WOID", bean.getWOID());
                                        jsonObject1.put("WOTitle", bean.getWOTitle());
                                        jsonObject1.put("WOContent", bean.getWOContent());
                                        jsonObject1.put("Voice", bean.getVoice());
                                        jsonObject1.put("WOState", 0);
                                        jsonObject1.put("WOIssuedDate", bean.getWOIssuedDate()); // 派工时间
                                        jsonObject1.put("WOIssuedUser", bean.getWOIssuedUser()); // 派工人
                                        // 这个字段是activity进入的时候就获取的年月日时分秒
                                        jsonObject1.put("WOReceiveDate", endDate);
                                        jsonObject1.put("WOReceiveUser", bean.getWOReceiveUser());
                                        jsonObject1.put("WOItemsNum", totalNumber);
                                        jsonObject1.put("WOPerformNum", changeNumber);
                                        jsonObject1.put("WOBeginDate", bean.getWOBeginDate());
                                        // 这个字段是点击发送按钮获取的当前的年月日时分秒
                                        jsonObject1.put("WOEndDate", bean.getWOEndDate());
                                        jsonObject1.put("WOCreateDate", bean.getWOCreateDate());
                                        jsonObject1.put("WOType", "1");
                                        jsonObject1.put("WOExpectedTime", bean.getWOExpectedTime());
                                        jsonObject1.put("FBID", fbId);
                                        jsonObject1.put("WOFeedback", "");
                                        // detail  字段      这个用jsonArray
                                        jsonObject2.put("WOID", bean.getWOID());
                                        jsonObject2.put("Num", "1");
                                        jsonObject2.put("OrderContent", bean.getWOContent());
                                        // 这个一直没有改，直到18年5月25日才改成1,以前是“1”
                                        jsonObject2.put("OrderState", 0);
                                        jsonObject2.put("UserID", userId);
                                        jsonObject2.put("UserName", username);// 用户名
                                        jsonObject2.put("DateTime", endDate);
                                        jsonArray.put(jsonObject2);
                                    } catch (JSONException e){
                                        e.printStackTrace();
                                    }
                                    // 更新工单状态
                                    getUpdateWorkOrder();
                                }
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
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/UpdateWorkOrder");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/UpdateWorkOrder");
        }
        params.addBodyParameter("workorder", jsonObject1.toString());
        params.addBodyParameter("detail", jsonArray.toString());
        params.addBodyParameter("guid", guidString);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                //toast(ex.getMessage());
                toast("上传失败" + "updateworkorder");
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
                        toast(commonResponseBean.getMessage());
                        // 目的：解决点击“已转发”再回来“执行中”界面的时候会再次重复执行getUpdateWorkOrder()方法的问题
                        workOrderNumber = "";
                        // 这个用于点击接收按钮更新工单数据之后的刷新界面操作
                        refreshData();
                    } else if (commonResponseBean.getCode() == 0) {
                        intent.setClass(mActivity, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        toast(commonResponseBean.getMessage());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    // 这个用于点击接收按钮更新工单数据之后的刷新界面操作
    private void refreshData() {
        datas.clear();
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String time = format.format(date);
        loadding.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        }
        params.addBodyParameter("userID", userId);
        params.addBodyParameter("pageSize", "100");
        params.addBodyParameter("beginDate", "1900-01-01");
        params.addBodyParameter("endDate", "2200-12-31");
        params.addBodyParameter("guid", guidString);
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
                        intent.setClass(TaskListActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")){
                        Type listType = new TypeToken<List<TaskItemBean2>>() {}.getType();
                        Gson gson = new Gson();
                        gsonDatas = gson.fromJson(object1.getString("Data"), listType);
                        for(int i = 0; i < gsonDatas.size(); i++){
                            // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                            // IssuedUser就是admin跟userId是不同的，这样的话进入执行中
                            if(gsonDatas.get(i).getWOType().equals("1") &&
                                    gsonDatas.get(i).getWOState().equals("false") &&
                                    !gsonDatas.get(i).getWOIssuedUser().equals(userId) /*&&
                                    gsonDatas.get(i).getIsIssue().equals("false")*/){
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

    // Toast提醒的封装
    private void toast(String text){
        Toast.makeText(TaskListActivity.this, text, Toast.LENGTH_SHORT).show();
    }
}
