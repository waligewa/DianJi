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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.motor.R;
import com.example.motor.activity.DetailTaskActivity2;
import com.example.motor.activity.LoginActivity;
import com.example.motor.activity.SendTaskActivity2;
import com.example.motor.adapter.TaskRvAdapter2;
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
 * 巡检未处理fragment
 *
 */
public class InspectionExecutingFragment extends Fragment{

    private RelativeLayout linearLayout;
    private RecyclerView recyclerView;
    private RadioGroup taskRadioGroup;
    private RadioButton radioReceived, radioFeedbacked, radioNohandled;
    private List<TaskItemBean> datas = new ArrayList<>();
    private List<TaskItemBean> dataSql = new ArrayList<>();
    private List<TaskItemBean> gsonDatas;
    private Loadding loadding;
    private String gatewayAddress, userId, username = "";
    private SharedPreferences prefs1;
    private SharedPreferences.Editor editor;
    private Intent intent;
    private TaskRvAdapter2 mAdapter;
    private SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private int totalNumber, changeNumber, flag = 1;
    private JSONArray jsonArray;
    private JSONObject jsonObject1, jsonObject2;
    private boolean MultipleChoice = false; // 多选模式
    private LinearLayout llBottomPanel; // 底部控制面板
    // 底部俩按钮
    private TextView tvBottomCancel;
    private TextView tvBottomSendTask;
    private TextView multipleSelection;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        linearLayout = (RelativeLayout) inflater.inflate(R.layout.nohandlefragment, container, false);
        recyclerView = (RecyclerView) linearLayout.findViewById(R.id.recyclerView);
        taskRadioGroup = (RadioGroup) linearLayout.findViewById(R.id.taskRadioGroup);
        radioReceived = (RadioButton) linearLayout.findViewById(R.id.radio_received);
        radioFeedbacked = (RadioButton) linearLayout.findViewById(R.id.radio_feedbacked);
        radioNohandled = (RadioButton) linearLayout.findViewById(R.id.radio_nohandled);
        multipleSelection = (TextView) linearLayout.findViewById(R.id.multiple_selection);
        tvBottomCancel = (TextView) linearLayout.findViewById(R.id.tvBottomCancel);  // 取消按钮
        tvBottomSendTask = (TextView) linearLayout.findViewById(R.id.tvBottomSendTask);  // 派发任务按钮
        llBottomPanel = (LinearLayout) linearLayout.findViewById(R.id.llBottomPanel);   // 取消按钮和派发任务按钮的容器
        // RadioGroup的两个RadioButton的三个点击事件
        taskRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    if (checkedId == R.id.radio_received) {
                        if(!NetWorkUtil.isNetworkConnected(getActivity())){
                            datas.clear();
                            List<TaskItemBean> tibList = LitePal.findAll(TaskItemBean.class);
                            for(int i = 0; i < tibList.size(); i++){
                                // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                                // IssuedUser就是admin跟userId是不同的，这样的话进入执行中 WOIssuedUser为派工人
                                // 接收时间为不为空，反馈为空
                                if(tibList.get(i).getWOType().equals("2") &&
                                        tibList.get(i).getWOState().equals("false") &&
                                        tibList.get(i).getIsIssue().equals("false") &&
                                        !tibList.get(i).getWOIssuedUser().equals(userId) &&
                                        TextUtils.isEmpty(tibList.get(i).getDevCheckID()) &&
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
                            List<TaskItemBean> tibList = LitePal.findAll(TaskItemBean.class);
                            for(int i = 0; i < tibList.size(); i++){
                                // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                                // IssuedUser就是admin跟userId是不同的，这样的话进入执行中 WOIssuedUser为派工人
                                // 只要保证反馈不为空就可以
                                if(tibList.get(i).getWOType().equals("2") &&
                                        tibList.get(i).getWOState().equals("false") &&
                                        tibList.get(i).getIsIssue().equals("false") &&
                                        !tibList.get(i).getWOIssuedUser().equals(userId) &&
                                        TextUtils.isEmpty(tibList.get(i).getDevCheckID()) &&
                                        !TextUtils.isEmpty(tibList.get(i).getWOFeedback())){
                                    datas.add(tibList.get(i));
                                }
                            }
                            mAdapter.notifyDataSetChanged();
                        } else {
                            // 已反馈
                            getFeedbackedData();
                        }
                    } else if (checkedId == R.id.radio_nohandled) {
                        if(!NetWorkUtil.isNetworkConnected(getActivity())){
                            datas.clear();
                            List<TaskItemBean> tibList = LitePal.findAll(TaskItemBean.class);
                            for(int i = 0; i < tibList.size(); i++){
                                // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                                // IssuedUser就是admin跟userId是不同的，这样的话进入执行中 WOIssuedUser为派工人
                                if(tibList.get(i).getWOType().equals("2") &&
                                        tibList.get(i).getWOState().equals("false") &&
                                        tibList.get(i).getIsIssue().equals("false") &&
                                        !tibList.get(i).getWOIssuedUser().equals(userId) &&
                                        TextUtils.isEmpty(tibList.get(i).getDevCheckID()) &&
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
        });
        // 多选按钮的点击事件
        multipleSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 如果集合的长度等于0的话就不让它往下执行
                if(datas.size() > 0 && !DoubleClickUtils.isFastDoubleClick()){
                    // changeModel里面有通知适配器改变的代码
                    mAdapter.changeModel(!MultipleChoice);
                    showOrHiddenControlPanel(!MultipleChoice);
                    MultipleChoice = !MultipleChoice;
                    //toast("---" + MultipleChoice + "---");
                    if(MultipleChoice){
                        flag = 0;
                    } else {
                        flag = 1;
                    }
                    //toast(String.valueOf(flag));
                }
            }
        });
        // 取消按钮的点击事件
        tvBottomCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    //Log.e(TAG, "onClick: MultipleChoice====" + MultipleChoice);
                    MultipleChoice = !MultipleChoice;
                    showOrHiddenControlPanel(MultipleChoice);
                    // changeModel里面有通知适配器改变的代码
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
                    MultipleChoice = !MultipleChoice;
                    showOrHiddenControlPanel(MultipleChoice);
                    // changeModel里面有通知适配器改变的代码
                    mAdapter.changeModel(MultipleChoice);
                    // getCurrentChoosenItems()返回的是一个集合，将这个集合赋值为currentChoosenItems
                    List<TaskItemBean> currentChoosenItems = mAdapter.getCurrentChoosenItems();
                    if(currentChoosenItems == null || (currentChoosenItems.size() == 0)) return;
                    intent.setClass(getActivity(), SendTaskActivity2.class);
                    intent.putExtra("currentChooseItems", (Serializable) currentChoosenItems);
                    toast(String.valueOf(currentChoosenItems.size()));
                    startActivity(intent);
                    mAdapter.resetSelectedStatus();
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
        // 创建SQLite数据库
        Connector.getDatabase();
        return linearLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 偏好设置里面的username是id，userfullname是文字
        username = prefs1.getString("userfullname", "");
        mAdapter = new TaskRvAdapter2(getActivity(), datas);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mAdapter);
        gatewayAddress = prefs1.getString("add", "");
        userId = String.valueOf(prefs1.getInt("UserID", 0));
        // 获取未处理的列表数据
        //getNoHandledData();
        // 初始化所有的点击事件
        initRecyclerViewListener();
    }

    private void initRecyclerViewListener() {

        // 这是整个子项控件的
        mAdapter.setOnItemClickListener(new TaskRvAdapter2.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // 这个判断直接复制的TaskListActivity2里面的getData()里面的判断方法
                if(!DoubleClickUtils.isFastDoubleClick()){
                    // flag为1代表是非多选状态
                    if (flag == 1) {
                        TaskItemBean bean = datas.get(position);
                        String voiceString = bean.getVoice();
                        editor.putString("ownid", bean.getWOID());
                        editor.apply();
                        intent.setClass(getActivity(), DetailTaskActivity2.class);
                        intent.putExtra("taskitembean1", bean);
                        startActivity(intent);
                    // flag为0代表是多选状态，我将对图片的点击操作挪到对整个子项控件的点击操作里面进行分类别操作
                    } else if (flag == 0) {
                        // 先根据位置获取这个实体类
                        TaskItemBean bean = datas.get(position);
                        // 如果data2的集合数量是0的话，就让它增加一个，并且实体类的MultipelChoiceSelected赋值
                        // 为true（也就是选择状态），通知适配器发生改变
                        if(datas.size() == 0){
                            datas.add(bean);
                            bean.setMultipleChoiceSelected(!bean.isMultipleChoiceSelected());// 括号里面为true
                            mAdapter.notifyDataSetChanged();
                        } else {
                            // 如果data2的集合数量不是0的话，说明第一个实体类已经加入到了data2里面去了，这就是
                            // 有一个样本数据了，下面就要根据这个实体类进行判断。如果后来的实体类的WOID与第一个
                            // 实体类的WOID一致，就将其MultipleChoice赋值为true，这样就表示在通知适配器变化的
                            // 时候会将圆圈的背景图片赋值为红色背景。
                            if(bean.getWOID().equals(datas.get(0).getWOID())){
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
                                        //datas.add(bean);// 将总集合里面的所有选中的放入一个集合当中
                                    } else if (datas.contains(bean)){
                                        //datas.remove(bean);
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
                }
            }
        });

        // 接收按钮
        mAdapter.setOnReceiverClickEvent(new TaskRvAdapter2.onReceiverClickEvent() {
            @Override
            public void onReceiverClick(int position) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    TaskItemBean item = datas.get(position);
                    if(item.getWOFeedback() == null || item.getWOFeedback().equals("")){
                        try{
                            Date date2 = new Date();
                            String endDate = format1.format(date2); // 结束时间（年月日时分秒）
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
                    } else {
                        toast("已反馈工单不能进行接收");
                    }
                }
            }
        });

        // 左边 显示摘要按钮
        mAdapter.setOnLeftButtonClickEvent(new TaskRvAdapter2.onLeftButtonClickEvent() {
            @Override
            public void onLeftButtonClick(int position) { }
        });

        // 右边 处理该任务按钮
        mAdapter.setOnRightButtonClickEvent(new TaskRvAdapter2.onRightButtonClickEvent() {
            @Override
            public void onRightButtonClick(int position) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    TaskItemBean bean = datas.get(position);
                    editor.putString("ownid", bean.getWOID());
                    editor.apply();
                    intent.setClass(getActivity(), DetailTaskActivity2.class);
                    intent.putExtra("taskitembean1", bean);
                    startActivity(intent);
                }
            }
        });
        // 最右边的圆形图片（负责多选的图片）的点击事件
        /*mAdapter.setOnImageClickEvent(new TaskRvAdapter2.onImageClickEvent() {
            @Override
            public void onImageClick(int position) {}
        });*/
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

    // 已接收的数据
    private void getReceivedData() {
        flag = 1;
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
                        Type listType = new TypeToken<List<TaskItemBean>>() {}.getType();
                        Gson gson = new Gson();
                        gsonDatas = gson.fromJson(object1.getString("Data"), listType);
                        // 存储TaskItemBean对应的数据库
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
                                dataSql.add(gsonDatas.get(i));
                            }
                        }
                        LitePal.deleteAll(TaskItemBean.class);
                        // 将datas里面的数据存储到SQLite里面去
                        for(int i = 0; i < dataSql.size();i++){
                            TaskItemBean bean = dataSql.get(i);
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
                        for(int i = 0; i < gsonDatas.size(); i++){
                            // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                            // IssuedUser就是admin跟userId是不同的，这样的话进入执行中 WOIssuedUser为派工人
                            // 接收时间为不为空，反馈为空
                            if(gsonDatas.get(i).getWOType().equals("2") &&
                                    gsonDatas.get(i).getWOState().equals("false") &&
                                    gsonDatas.get(i).getIsIssue().equals("false") &&
                                    !gsonDatas.get(i).getWOIssuedUser().equals(userId) &&
                                    TextUtils.isEmpty(gsonDatas.get(i).getDevCheckID()) &&
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
        flag = 1;
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
                        Type listType = new TypeToken<List<TaskItemBean>>() {}.getType();
                        Gson gson = new Gson();
                        gsonDatas = gson.fromJson(object1.getString("Data"), listType);
                        // 存储TaskItemBean对应的数据库
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
                                dataSql.add(gsonDatas.get(i));
                            }
                        }
                        LitePal.deleteAll(TaskItemBean.class);
                        // 将datas里面的数据存储到SQLite里面去
                        for(int i = 0; i < dataSql.size();i++){
                            TaskItemBean bean = dataSql.get(i);
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
                        for(int i = 0; i < gsonDatas.size(); i++){
                            // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                            // IssuedUser就是admin跟userId是不同的，这样的话进入执行中 WOIssuedUser为派工人
                            // 只要保证反馈不为空就可以
                            if(gsonDatas.get(i).getWOType().equals("2") &&
                                    gsonDatas.get(i).getWOState().equals("false") &&
                                    gsonDatas.get(i).getIsIssue().equals("false") &&
                                    !gsonDatas.get(i).getWOIssuedUser().equals(userId) &&
                                    TextUtils.isEmpty(gsonDatas.get(i).getDevCheckID()) &&
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
        flag = 1;
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
                        Type listType = new TypeToken<List<TaskItemBean>>() {}.getType();
                        Gson gson = new Gson();
                        gsonDatas = gson.fromJson(object1.getString("Data"), listType);
                        // 存储TaskItemBean对应的数据库
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
                                dataSql.add(gsonDatas.get(i));
                            }
                        }
                        LitePal.deleteAll(TaskItemBean.class);
                        // 将datas里面的数据存储到SQLite里面去
                        for(int i = 0; i < dataSql.size();i++){
                            TaskItemBean bean = dataSql.get(i);
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
                        for(int i = 0; i < gsonDatas.size(); i++){
                            // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                            // IssuedUser就是admin跟userId是不同的，这样的话进入执行中 WOIssuedUser为派工人
                            if(gsonDatas.get(i).getWOType().equals("2") &&
                                    gsonDatas.get(i).getWOState().equals("false") &&
                                    gsonDatas.get(i).getIsIssue().equals("false") &&
                                    !gsonDatas.get(i).getWOIssuedUser().equals(userId) &&
                                    TextUtils.isEmpty(gsonDatas.get(i).getDevCheckID()) &&
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
        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                //toast(ex.getMessage());
                toast("工单更新失败");
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
                        toast("巡检工单已接收！");
                        // 这个用于点击接收按钮更新工单数据之后的刷新界面操作
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

    // 这个用于点击接收按钮更新工单数据之后的刷新界面操作
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
            public void onFinished() { }

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
                            // 最后一个判断是因为五月三十一号的时候我不转发工单直接去处理，发现处理完毕
                            // 工单不会去（已处理）里面去，还是停留在（执行中）当中（这里还有个问题是如
                            // 果你去处理一个工单是不会生成新的工单的，而是对原有工单再次处理），后来经
                            // 过讨论，后台在子项里面又添加一个DevCheckID子项，根据它判断此工单是在执行
                            // 中还是在已处理当中 z
                            if(gsonDatas.get(i).getWOType().equals("2") &&
                                    gsonDatas.get(i).getWOState().equals("false") &&
                                    gsonDatas.get(i).getIsIssue().equals("false") &&
                                    !gsonDatas.get(i).getWOIssuedUser().equals(userId) &&
                                    TextUtils.isEmpty(gsonDatas.get(i).getDevCheckID()) &&
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
        if (radioReceived.isChecked()) {
            if(!NetWorkUtil.isNetworkConnected(getActivity())){
                datas.clear();
                List<TaskItemBean> tibList = LitePal.findAll(TaskItemBean.class);
                for(int i = 0; i < tibList.size(); i++){
                    // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                    // IssuedUser就是admin跟userId是不同的，这样的话进入执行中 WOIssuedUser为派工人
                    // 接收时间为不为空，反馈为空
                    if(tibList.get(i).getWOType().equals("2") &&
                            tibList.get(i).getWOState().equals("false") &&
                            tibList.get(i).getIsIssue().equals("false") &&
                            !tibList.get(i).getWOIssuedUser().equals(userId) &&
                            TextUtils.isEmpty(tibList.get(i).getDevCheckID()) &&
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
                List<TaskItemBean> tibList = LitePal.findAll(TaskItemBean.class);
                for(int i = 0; i < tibList.size(); i++){
                    // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                    // IssuedUser就是admin跟userId是不同的，这样的话进入执行中 WOIssuedUser为派工人
                    // 只要保证反馈不为空就可以
                    if(tibList.get(i).getWOType().equals("2") &&
                            tibList.get(i).getWOState().equals("false") &&
                            tibList.get(i).getIsIssue().equals("false") &&
                            !tibList.get(i).getWOIssuedUser().equals(userId) &&
                            TextUtils.isEmpty(tibList.get(i).getDevCheckID()) &&
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
                List<TaskItemBean> tibList = LitePal.findAll(TaskItemBean.class);
                for(int i = 0; i < tibList.size(); i++){
                    // 1代表维修任务，第一个false代表是执行中, 第三个判断就是admin派发工单过来，
                    // IssuedUser就是admin跟userId是不同的，这样的话进入执行中 WOIssuedUser为派工人
                    if(tibList.get(i).getWOType().equals("2") &&
                            tibList.get(i).getWOState().equals("false") &&
                            tibList.get(i).getIsIssue().equals("false") &&
                            !tibList.get(i).getWOIssuedUser().equals(userId) &&
                            TextUtils.isEmpty(tibList.get(i).getDevCheckID()) &&
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
