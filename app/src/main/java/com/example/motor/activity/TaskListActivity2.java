package com.example.motor.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.adapter.TaskRvAdapter2;
import com.example.motor.base.BaseActivity;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.CommonResponseBean;
import com.example.motor.db.TaskItemBean;
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
 * Desc   : 巡检任务
 * 要保证显示正常的话就要改动是否隐藏控制面板、适配器里面的多选按钮改动、列表子项的点击事件
 * 后台派发巡检工单之后，生成很多时间一样的工单，我转发之后老的工单去了（已处理）里面，新的巡检工单去了
 * （已转发）里面，然后我登录另一个账号处理这个工单，处理完毕之后这个工单跑到（已处理）里面
 *
 */

public class TaskListActivity2 extends BaseActivity {

    private static final String TAG = "TaskListActivity2";
    private RadioGroup taskRadioGroup;
    private RecyclerView recyclerView;
    private boolean MultipleChoice = false; // 多选模式
    private List<TaskItemBean> datas = new ArrayList<>();
    private List<TaskItemBean> datas2 = new ArrayList<>();
    private List<TaskItemBean> gsonDatas;
    private TaskRvAdapter2 mAdapter;
    private LinearLayout llBottomPanel; // 底部控制面板
    private Loadding loadding;
    private SharedPreferences prefs1;
    private SharedPreferences.Editor editor;
    private String gatewayAddress, userId, guidString, username = "", workOrderNumber = "";
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
        mBaseTitleBarView.setTitleText("巡检待办任务");
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
            List<TaskItemBean> tibList = LitePal.findAll(TaskItemBean.class);
            datas.addAll(tibList);
            mAdapter.notifyDataSetChanged();
        } else {
            http_getUnDoData();
        }
        initListener();
    }

    @Override
    public void widgetClick(View v) { }

    @Override
    public void onTitleLeftPressed() {
        if(!DoubleClickUtils.isFastDoubleClick()){
            onBackPressed();
        }
    }

    // 右边多选按钮的点击事件
    @Override
    public void onTitleRightTextPressed() {
        // 如果集合的长度等于0的话就不让它往下执行
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
        mAdapter = new TaskRvAdapter2(context, datas);
        recyclerView.setAdapter(mAdapter);
        // 创建SQLite数据库
        Connector.getDatabase();
    }

    private void initListener() {

        // RadioGroup的三个RadioButton的两个点击事件
        taskRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    if (checkedId == R.id.radioButtonUnDo) {
                        // 如果无网络就走SQLite
                        if(!NetWorkUtil.isNetworkConnected(mActivity)){
                            datas.clear();
                            List<TaskItemBean> tibList = LitePal.findAll(TaskItemBean.class);
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
                    datas2.clear();
                    //Log.e(TAG, "onClick: MultipleChoice====" + MultipleChoice);
                    MultipleChoice = !MultipleChoice;
                    showOrHiddenControlPanel(MultipleChoice);
                    mAdapter.changeModel(MultipleChoice);
                    //toast("取消");
                }
            }
        });

        // 派发任务的点击事件，传递过去的是集合List<TaskItemBean>
        tvBottomSendTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    datas2.clear();
                    MultipleChoice = !MultipleChoice;
                    showOrHiddenControlPanel(MultipleChoice);
                    mAdapter.changeModel(MultipleChoice);
                    // getCurrentChoosenItems()返回的是一个集合，将这个集合赋值为currentChoosenItems
                    List<TaskItemBean> currentChoosenItems = mAdapter.getCurrentChoosenItems();
                    if(currentChoosenItems.size() == 0) return;
                    intent.setClass(TaskListActivity2.this, SendTaskActivity2.class);
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
        mAdapter.setOnItemClickListener(new TaskRvAdapter2.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // 这个判断直接复制的TaskListActivity2里面的getData()里面的判断方法
                if(datas.get(position).getWOType().equals("2") &&
                        datas.get(position).getWOState().equals("false") &&
                        datas.get(position).getIsIssue().equals("false") &&
                        !datas.get(position).getWOIssuedUser().equals(userId)&&
                        !DoubleClickUtils.isFastDoubleClick()){
                    TaskItemBean bean = datas.get(position);
                    String voiceString = bean.getVoice();
                    editor.putString("ownid", bean.getWOID());
                    editor.commit();
                    intent.setClass(TaskListActivity2.this,
                            DetailTaskActivity2.class);
                    intent.putExtra("taskitembean1", bean);
                    startActivity(intent);
                    finish();
                }
            }
        });

        // 接收按钮
        mAdapter.setOnReceiverClickEvent(new TaskRvAdapter2.onReceiverClickEvent() {
            @Override
            public void onReceiverClick(int position) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    try{
                        Date date2 = new Date();
                        String endDate = format1.format(date2);// 结束时间（年月日时分秒）
                        totalNumber = Integer.valueOf(datas.get(position).getWOItemsNum()); // 巡检工单的总数量
                        changeNumber = Integer.valueOf(datas.get(position).getWOPerformNum()); // 巡检工单的子数量
                        // workorder 字段  这个不用jsonArray
                        jsonObject1.put("WOID", datas.get(position).getWOID());
                        jsonObject1.put("WOTitle", datas.get(position).getWOTitle());
                        jsonObject1.put("WOContent", datas.get(position).getWOContent());
                        jsonObject1.put("Voice", datas.get(position).getVoice());
                        jsonObject1.put("WOState", 0);
                        jsonObject1.put("WOIssuedDate", datas.get(position).getWOIssuedDate());// 派发时间
                        jsonObject1.put("WOIssuedUser", datas.get(position).getWOIssuedUser());// 派发人
                        // 这个字段是activity进入的时候就获取的年月日时分秒
                        jsonObject1.put("WOReceiveDate", endDate);
                        jsonObject1.put("WOReceiveUser", datas.get(position).getWOReceiveUser());
                        jsonObject1.put("WOItemsNum", totalNumber); // 巡检工单的总数量
                        jsonObject1.put("WOPerformNum", changeNumber); // 巡检工单的子数量
                        jsonObject1.put("WOBeginDate", datas.get(position).getWOBeginDate());
                        // 这个字段是点击发送按钮获取的当前的年月日时分秒
                        jsonObject1.put("WOEndDate", datas.get(position).getWOEndDate());
                        jsonObject1.put("WOCreateDate", datas.get(position).getWOCreateDate());
                        // 这是转发的WOType要填2代表巡检任务
                        jsonObject1.put("WOType", "2");
                        jsonObject1.put("WOExpectedTime", datas.get(position).getWOExpectedTime());
                        jsonObject1.put("FBID", "1");
                        jsonObject1.put("WOFeedback", "");
                        // detail  字段      这个用jsonArray
                        jsonObject2.put("WOID", datas.get(position).getWOID());
                        jsonObject2.put("Num", "1");
                        jsonObject2.put("OrderContent", datas.get(position).getWOContent());
                        jsonObject2.put("OrderState", 0);
                        jsonObject2.put("UserID", userId);
                        jsonObject2.put("UserName", username);
                        jsonObject2.put("DateTime", endDate);
                        jsonArray.put(jsonObject2);
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                    // 如果没有WOReceiveDate的话才更新工单状态，有WOReceiveDate的话不更新工单状态，
                    // 这个更新工单状态主要是用于上报接收时间的
                    if("".equals(datas.get(position).getWOReceiveDate()) || datas.get(position).getWOReceiveDate() == null){
                        // 更新工单状态
                        getUpdateWorkOrder();
                    }
                }
            }
        });

        // 左边 显示摘要按钮
        mAdapter.setOnLeftButtonClickEvent(new TaskRvAdapter2.onLeftButtonClickEvent() {
            @Override
            public void onLeftButtonClick(int position) {
                TaskItemBean bean = datas.get(position);
                //toast("左边按钮，显示摘要" + position);
            }
        });

        // 右边 处理该任务按钮
        mAdapter.setOnRightButtonClickEvent(new TaskRvAdapter2.onRightButtonClickEvent() {
            @Override
            public void onRightButtonClick(int position) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    TaskItemBean bean = datas.get(position);
                    editor.putString("ownid", bean.getWOID());
                    editor.commit();
                    intent.setClass(TaskListActivity2.this, DetailTaskActivity2.class);
                    intent.putExtra("taskitembean1", bean);
                    startActivity(intent);
                    finish();
                }
            }
        });

        // 最右边的圆形图片（负责多选的图片）的点击事件
        mAdapter.setOnImageClickEvent(new TaskRvAdapter2.onImageClickEvent() {
            @Override
            public void onImageClick(int position) {
                // 先根据位置获取这个实体类
                TaskItemBean bean = datas.get(position);
                // 如果data2的集合数量是0的话，就让它增加一个，并且实体类的MultipelChoiceSelected赋值
                // 为true（也就是选择状态），通知适配器发生改变
                if(datas2.size() == 0){
                    datas2.add(bean);
                    bean.setMultipleChoiceSelected(!bean.isMultipleChoiceSelected());// 括号里面为true
                    mAdapter.notifyDataSetChanged();
                } else {
                    // 如果data2的集合数量不是0的话，说明第一个实体类已经加入到了data2里面去了，这就是
                    // 有一个样本数据了，下面就要根据这个实体类进行判断。如果后来的实体类的WOID与第一个
                    // 实体类的WOID一致，就将其MultipleChoice赋值为true，这样就表示在通知适配器变化的
                    // 时候会将圆圈的背景图片赋值为红色背景。
                    if(bean.getWOID().equals(datas2.get(0).getWOID())){
                        if (MultipleChoice) {
                            bean.setMultipleChoiceSelected(!bean.isMultipleChoiceSelected());
                            // 我已经将实体类的MultipleChoiceSelected赋值为true了，所以会走第一if，
                            // 将实体类添加到结合data2中去，当第二次点击这个item的时候，上面这句代码就将
                            // MultipleChoiceSelected赋值为false了（因为
                            // bean.isMultipleChoiceSelected()是true，这个true是上一次的点击事件给的，
                            // 加上“！”当然会变为false），然后就会走第二个if，第二个if还有个判断是（集合
                            // 是否包含bean，包含的话才会让它移除实体类，实际这句代码不用要，
                            // 走到第二步肯定有实体类），然后移除实体类，就成功了。
                            if(bean.isMultipleChoiceSelected()){
                                datas2.add(bean);// 将总集合里面的所有选中的放入一个集合当中
                                toast(datas2.size() + "狗蛋");
                            } else if (datas2.contains(bean)){
                                datas2.remove(bean);
                                toast(datas2.size() + "婷婷");
                            }
                            mAdapter.notifyDataSetChanged();
                        }
                    } else {
                        // 如果后来的实体类的WOID与第一个实体类的WOID不一致，那就说明是不同工单号的工单，
                        // 那就不让它得以选择，也就是不给它的MultipleChoice赋值为true
                        toast("请选择工单号码一致的巡检工单");
                    }
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
     * 获取已转发的列表数据
     */
    private void http_getForwardData() {
        getData3();
    }


    /**
     * 获取已处理的列表数据
     */
    private void http_getDoneData() {
        getData2();
    }

    /**
     * 是否显示下边的俩按钮。如果集合的第一个元素的WOState是true就是已处理的界面，将其隐藏，这样的目的就
     * 是直接让它不能够点击；如果集合的第一个元素的WOState是false就是未处理的界面，将其显示，这样的目的是
     * 让它能够被点击
     * @param show TRUE-显示
     */
    private void showOrHiddenControlPanel(boolean show) {
        if (show) {
            // 这个判断直接复制的TaskListActivity2里面的getData()里面的判断方法
            if(datas.get(0).getWOType().equals("2") &&
                    datas.get(0).getWOState().equals("false") &&
                    datas.get(0).getIsIssue().equals("false") &&
                    !datas.get(0).getWOIssuedUser().equals(userId)){
                llBottomPanel.setVisibility(View.VISIBLE);
            } else {
                llBottomPanel.setVisibility(View.GONE);
            }
        } else {
            llBottomPanel.setVisibility(View.GONE);
        }
    }

    // 执行中
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
            public void onFinished() { }

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        intent.setClass(TaskListActivity2.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")){
                        Type listType = new TypeToken<List<TaskItemBean>>() {}.getType();
                        Gson gson = new Gson();
                        gsonDatas = gson.fromJson(object1.getString("Data"), listType);
                        for(int i = 0; i < gsonDatas.size(); i++){
                            // 最后一个判断是因为五月三十一号的时候我不转发工单直接去处理，发现处理完毕
                            // 工单不会去（已处理）里面去，还是停留在（执行中）当中（这里还有个问题是如
                            // 果你去处理一个工单是不会生成新的工单的，而是对原有工单再次处理），后来经
                            // 过讨论，后台在子项里面又添加一个DevCheckID子项，根据它判断此工单是在执行
                            // 中还是在已处理当中 z
                            if(gsonDatas.get(i).getWOType().equals("2") &&
                                    gsonDatas.get(i).getWOState().equals("false") &&
                                    gsonDatas.get(i).getIsIssue().equals("false") &&
                                    !gsonDatas.get(i).getWOIssuedUser().equals(userId) &&
                                    TextUtils.isEmpty(gsonDatas.get(i).getDevCheckID())){
                                datas.add(gsonDatas.get(i));
                            }
                        }
                        LitePal.deleteAll(TaskItemBean.class);
                        // 将datas里面的数据存储到SQLite里面去
                        for(int i = 0; i < datas.size();i++){
                            TaskItemBean bean = datas.get(i);
                            TaskItemBean t = new TaskItemBean();
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
                        // 点击推送跳转到TaskListActivity2之后在生成datas之后再更新它的工单状态，
                        // 因为如果直接在initView里面进行如下操作的话会出现一个问题：工单id与datas
                        // 里面的比对不到，因为可能datas还没有生成，这样就导致有和无的比对，从而造成异常。
                        /*if(workOrderNumber != null && !workOrderNumber.equals("")){
                            try{
                                Date date2 = new Date();
                                String endDate = format1.format(date2);// 结束时间（年月日时分秒）

                                // 目的：取出对象用于getUpdateWorkOrder的数据上传。
                                // 将推送采集的WOID与datas里面的实体对象的WOID进行比对，相同就拿出来使用它的数据。
                                for(int i = 0; i < datas.size(); i++){
                                    if(datas.get(i).getWOID().equals(workOrderNumber)){
                                        TaskItemBean taskBean = datas.get(i);
                                        totalNumber = Integer.valueOf(taskBean.getWOItemsNum()); // 巡检工单的总数量
                                        changeNumber = Integer.valueOf(taskBean.getWOPerformNum()); // 巡检工单的子数量
                                        // workorder 字段  这个不用jsonArray
                                        jsonObject1.put("WOID", taskBean.getWOID());
                                        jsonObject1.put("WOTitle", taskBean.getWOTitle());
                                        jsonObject1.put("WOContent", taskBean.getWOContent());
                                        jsonObject1.put("Voice", taskBean.getVoice());
                                        jsonObject1.put("WOState", 0);
                                        jsonObject1.put("WOIssuedDate", taskBean.getWOIssuedDate());// 派发时间
                                        jsonObject1.put("WOIssuedUser", taskBean.getWOIssuedUser());// 派发人
                                        // 这个字段是activity进入的时候就获取的年月日时分秒
                                        jsonObject1.put("WOReceiveDate", endDate);
                                        jsonObject1.put("WOReceiveUser", taskBean.getWOReceiveUser());
                                        jsonObject1.put("WOItemsNum", totalNumber); // 巡检工单的总数量
                                        jsonObject1.put("WOPerformNum", changeNumber); // 巡检工单的子数量
                                        jsonObject1.put("WOBeginDate", taskBean.getWOBeginDate());
                                        // 这个字段是点击发送按钮获取的当前的年月日时分秒
                                        jsonObject1.put("WOEndDate", taskBean.getWOEndDate());
                                        jsonObject1.put("WOCreateDate", taskBean.getWOCreateDate());
                                        // 这是转发的WOType要填2代表巡检任务
                                        jsonObject1.put("WOType", "2");
                                        jsonObject1.put("WOExpectedTime", taskBean.getWOExpectedTime());
                                        jsonObject1.put("FBID", "1");
                                        // detail  字段      这个用jsonArray
                                        jsonObject2.put("WOID", taskBean.getWOID());
                                        jsonObject2.put("Num", "1");
                                        jsonObject2.put("OrderContent", taskBean.getWOContent());
                                        jsonObject2.put("OrderState", 0);
                                        jsonObject2.put("UserID", userId);
                                        jsonObject2.put("UserName", username);
                                        jsonObject2.put("DateTime", endDate);
                                        jsonArray.put(jsonObject2);
                                        // 更新工单状态
                                        getUpdateWorkOrder();
                                        // 跳出循环体，继续执行循环外的函数体
                                        break;
                                    }
                                }
                            } catch (JSONException e){
                                e.printStackTrace();
                            }
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

    // 已转发
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
        String guidString = prefs1.getString("guid", "");
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
                        intent.setClass(TaskListActivity2.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")){
                        Type listType = new TypeToken<List<TaskItemBean>>() {}.getType();
                        Gson gson = new Gson();
                        gsonDatas = gson.fromJson(object1.getString("Data"), listType);
                        for(int i = 0; i < gsonDatas.size(); i++){
                            /*if(gsonDatas.get(i).getWOType().equals("2") &&
                                    gsonDatas.get(i).getWOState().equals("false") &&
                                    gsonDatas.get(i).getWOIssuedUser().equals(userId)){
                                datas.add(gsonDatas.get(i));
                            }*/
                            // 这个地方不能加getIsIssue()的判断是因为巡检工单是如果子项与总数不相等的话就
                            // WOState和OrderState都为false，所以虽然转发了，但是Issue一直都为false，因此
                            // 这里不能对getIsIssue进行限定
                            if(gsonDatas.get(i).getWOType().equals("2") &&
                                    gsonDatas.get(i).getWOState().equals("false") &&
                                    gsonDatas.get(i).getWOIssuedUser().equals(userId)/* &&
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
            public void onCancelled(CancelledException arg0) {}

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
                        intent.setClass(TaskListActivity2.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")){
                        Type listType = new TypeToken<List<TaskItemBean>>() {}.getType();
                        Gson gson = new Gson();
                        gsonDatas = gson.fromJson(object1.getString("Data"), listType);
                        for(int i = 0; i < gsonDatas.size(); i++){
                            // 2代表巡检任务，如果WOState是2或者是老的工单就让它进入已处理里面，WOState
                            // 毫无疑问，后者的话就是后台发过来的工单，我转发之后生成一个新的转发工单之
                            // 后老的工单也存在，只不过IsIssue成了true
                            if(gsonDatas.get(i).getWOType().equals("2")){
                                if(gsonDatas.get(i).getWOState().equals("true") ||
                                        (gsonDatas.get(i).getWOState().equals("false") &&
                                                gsonDatas.get(i).getIsIssue().equals("true")) ||
                                        !TextUtils.isEmpty(gsonDatas.get(i).getDevCheckID())){
                                    datas.add(gsonDatas.get(i));
                                }
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
        x.http().post(params, new Callback.CommonCallback<String>() {

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
                        // 目的：解决点击“已转发”再回来“执行中”界面的时候会再次重复执行getUpdateWorkOrder()方法,然后报“工单更新失败”的问题
                        workOrderNumber = "";
                        // 这个用于点击推送更新工单数据之后的刷新界面操作
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

    // 这个用于点击推送更新工单数据之后的刷新界面操作
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
            public void onFinished() { }

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        intent.setClass(TaskListActivity2.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")){
                        Type listType = new TypeToken<List<TaskItemBean>>() {}.getType();
                        Gson gson = new Gson();
                        gsonDatas = gson.fromJson(object1.getString("Data"), listType);
                        for(int i = 0; i < gsonDatas.size(); i++){
                            // 最后一个判断是因为五月三十一号的时候我不转发工单直接去处理，发现处理完毕
                            // 工单不会去（已处理）里面去，还是停留在（执行中）当中（这里还有个问题是如
                            // 果你去处理一个工单是不会生成新的工单的，而是对原有工单再次处理），后来经
                            // 过讨论，后台在子项里面又添加一个DevCheckID子项，根据它判断此工单是在执行
                            // 中还是在已处理当中 z
                            if(gsonDatas.get(i).getWOType().equals("2") &&
                                    gsonDatas.get(i).getWOState().equals("false") &&
                                    gsonDatas.get(i).getIsIssue().equals("false") &&
                                    !gsonDatas.get(i).getWOIssuedUser().equals(userId) &&
                                    TextUtils.isEmpty(gsonDatas.get(i).getDevCheckID())){
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
        Toast.makeText(TaskListActivity2.this, text, Toast.LENGTH_SHORT).show();
    }
}
