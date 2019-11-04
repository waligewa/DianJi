package com.example.motor.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.audio.AudioManager;
import com.example.motor.audio.MediaManager;
import com.example.motor.base.BaseActivity;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.CommonResponseBean;
import com.example.motor.db.TaskItemBean;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DoubleClickUtils;
import com.example.motor.util.Loadding;
import com.example.motor.util.SPUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.common.task.PriorityExecutor;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 巡检任务的任务详情
 *
 */
public class DetailTaskActivity2 extends BaseActivity {

    private static final String TAG = "zbb_DetailTaskActivity2";
    private Intent intent;
    private TextView sendTask, processTask, tvTitle, tvDescription, tvInformation, timeLength,
            expertScheme, tvSendPerson, feedback, completeTime, contractName, deviceNumber;
    private Button btnPlayButton;
    private SharedPreferences prefs1;
    private String gatewayAddress, ownId, audioSuffix, equipmentId, workListId = "", videoString = "",
                    equipmentNo = "", userId, username = "";
    private Loadding loadding;
    private TaskItemBean taskItemBean;
    // 可取消的任务
    private Callback.Cancelable cancelable;
    // 进度条对话框
    private ProgressDialog progressDialog;
    private Dialog dialog;
    private AudioManager audioManager;
    private boolean b1, b2;
    private AnimationDrawable animationDrawable;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Activity mActivity;
    private AlertDialog alertDialog;
    private SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private int totalNumber, changeNumber;
    private JSONArray jsonArray;
    private JSONObject jsonObject1, jsonObject2;

    // layout文件
    @Override
    public int setLayout() {
        return R.layout.activity_detail_task2;
    }

    // 在这里面设置TitleBar相关的
    @Override
    public void setTitle() {
        mBaseTitleBarView.setTitleText("任务详情");
        mBaseTitleBarView.setLeftDrawable(-1);
    }

    @Override
    public void initWidget() {
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);
        init();
        // 文字内容的加载
        //getData();
        // 下载音频文件
        getData2();
        // Mp4名字的加载和设备编号的加载
        //getData4();
        // 通过WOID得到设备编号
        getData5();
    }

    // View的click事件
    @Override
    public void widgetClick(View v) {}

    // 返回键事件
    @Override
    public void onTitleLeftPressed() {
        onBackPressed();
    }

    private void init() {
        intent = new Intent();
        loadding = new Loadding(this);
        mActivity = this;
        jsonObject1 = new JSONObject();
        jsonObject2 = new JSONObject();
        jsonArray = new JSONArray();
        sendTask = (TextView) findViewById(R.id.sendTask);
        processTask = (TextView) findViewById(R.id.tvItemRightBtn);
        btnPlayButton = (Button) findView(R.id.btnPlayButton);
        tvDescription = (TextView) findViewById(R.id.tvDesc);
        tvSendPerson = (TextView) findViewById(R.id.tvSendPerson); // 派工人
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvInformation = (TextView) findViewById(R.id.tvInfo);
        timeLength = (TextView) findViewById(R.id.timeLength);
        expertScheme = (TextView) findViewById(R.id.expert_scheme); // 专家方案
        feedback = (TextView) findViewById(R.id.feedback); // 反馈
        completeTime = (TextView) findViewById(R.id.tv_complete_time); // 完成时间
        contractName = (TextView) findViewById(R.id.tvContractName); // 合同名称
        deviceNumber = (TextView) findViewById(R.id.tvDeviceNumber); // 设备编号
        prefs1 = getSharedPreferences("UserInfo", 0);
        gatewayAddress = prefs1.getString("add", "");
        ownId = prefs1.getString("ownid", "");
        // 偏好设置里面的username是id，userfullname是文字
        username = prefs1.getString("userfullname", "");
        userId = String.valueOf(prefs1.getInt("UserID", 0));
        // Log.e(TAG, ownId);
        taskItemBean = (TaskItemBean) getIntent().getSerializableExtra("taskitembean1");
        audioSuffix = taskItemBean.getVoice();
        equipmentId = taskItemBean.getWOID();
        // 标题
        tvTitle.setText(TextUtils.isEmpty(taskItemBean.getWOTitle()) ? "标题：" : "标题：" + taskItemBean.getWOTitle());
        contractName.setText(TextUtils.isEmpty(taskItemBean.getDeviceName()) ? "合同名称：" : "合同名称：" + taskItemBean.getDeviceName());
        deviceNumber.setText(TextUtils.isEmpty(taskItemBean.getEquipmentNo()) ? "设备编号：" : "设备编号：" + taskItemBean.getEquipmentNo());
        // 描述
        tvDescription.setText(TextUtils.isEmpty(taskItemBean.getWOIssuedDate()) ? "时间：" :
                ("时间：" + taskItemBean.getWOIssuedDate().split("T")[0] + " " +
                        taskItemBean.getWOIssuedDate().split("T")[1].substring(0, 8)));
        // 要求完成时间
        completeTime.setText(TextUtils.isEmpty(taskItemBean.getWOExpectedTime()) ? "要求完成时间：" :
                "要求完成时间：" + taskItemBean.getWOExpectedTime().replace("T", " "));
        // 派工人
        tvSendPerson.setText(TextUtils.isEmpty(taskItemBean.getUserName()) ? "派工人：" : "派工人：" + taskItemBean.getUserName());
        // 信息
        tvInformation.setText(TextUtils.isEmpty(taskItemBean.getWOContent()) ? "内容：" : "内容：" + taskItemBean.getWOContent());
        // 问题描述
        initListener();

        // 创建进度条对话框（这个是下载语音或者视频的时候的进度条）
        progressDialog = new ProgressDialog(this);
        // 设置标题
        progressDialog.setTitle("下载文件");
        // 设置信息
        progressDialog.setMessage("玩命下载中...");
        // 设置显示的格式
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // 设置按钮
        progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "暂停",
                new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击取消正在下载的操作
                        cancelable.cancel();
                    }});
        audioManager = AudioManager.getInstance();
        try{
            // 语音动画对话框的设计与实现
            dialog = new Dialog(DetailTaskActivity2.this, R.style.dialog_style);
            dialog.setContentView(R.layout.login_loading_dialog);
            dialog.setCancelable(true);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            ImageView animationIv = (ImageView) dialog.findViewById(R.id.animationIv);
            animationDrawable = (AnimationDrawable) animationIv.getDrawable();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initListener(){
        // 派发任务
        sendTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    List<TaskItemBean> list = new ArrayList<>();
                    list.add(taskItemBean);
                    intent.setClass(mActivity, SendTaskActivity2.class);
                    intent.putExtra("currentChooseItems", (Serializable) list);
                    startActivity(intent);
                    finish();
                }
            }
        });
        // 处理任务
        processTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    intent.setClass(mActivity, DeviceInspectionActivity2.class);
                    intent.putExtra("ID", workListId);
                    intent.putExtra("EquipmentNo", equipmentNo);
                    intent.putExtra("taskitembean1", taskItemBean);
                    startActivity(intent);
                    finish();
                }
            }
        });

        // 播放按钮
        btnPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    if (!b1) return;
                    dialog.show();
                    animationDrawable.start();
                    // 播放音频
                    MediaManager.playSound(Environment.getExternalStorageDirectory() + "/mine_audio.mp3",
                            new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    animationDrawable.stop();
                                    dialog.cancel();
                                    toast("播放完毕");
                                }
                            });
                }
            }
        });
        // 专家方案
        expertScheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    intent.setClass(mActivity, ExpertSchemeActivity.class);
                    intent.putExtra("expert_scheme", taskItemBean.getWOContent());
                    startActivity(intent);
                }
            }
        });
        // 反馈
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(taskItemBean.getWOReceiveDate())){
                    toast("工单已经接收，不能执行反馈操作");
                } else {
                    if(!DoubleClickUtils.isFastDoubleClick()){
                        View checkAdminView = LayoutInflater.from(getBaseContext()).inflate(R.layout.check_admin_layout4, null);
                        final EditText et = (EditText) checkAdminView.findViewById(R.id.feedback_dialog);
                        Button feedback = (Button) checkAdminView.findViewById(R.id.submit);
                        Button cancel = (Button) checkAdminView.findViewById(R.id.cancel);
                        feedback.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(!DoubleClickUtils.isFastDoubleClick()){
                                    if (et.getText().toString().trim().equals("")) {
                                        et.setText("");
                                        toast("请重新填写！");
                                    } else {
                                        // 反馈那个地方点击确定之后的提交
                                        submit(et.getText().toString().trim());
                                        alertDialog.dismiss();
                                    }
                                }
                            }
                        });
                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (alertDialog.isShowing()) {
                                    alertDialog.dismiss();
                                }
                            }
                        });
                        alertDialog = new AlertDialog.Builder(mActivity).create();
                        alertDialog.setCancelable(false);
                        alertDialog.setView(new EditText(mActivity)); //  不加这句代码EditText无法填入东西
                        alertDialog.show();
                        alertDialog.getWindow().setContentView(checkAdminView); //  这句代码的意思是将布局填入AlertDialog
                    }
                }
            }
        });
    }

    // 下载音频文件
    private void getData2() {
        // loadding.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "UploadImg/mp3/" + audioSuffix);
        } else {
            params = new RequestParams(gatewayAddress + "UploadImg/mp3/" + audioSuffix);
        }
        params.setAutoResume(true);  // 设置是否在下载时自动断点续传
        params.setAutoRename(false);  // 设置是否根据头信息自动命名文件
        params.setSaveFilePath(Environment.getExternalStorageDirectory() + "/mine_audio.mp3");
        // 自定义线程池,有效的值范围[1, 3], 设置为3时, 可能阻塞图片加载  priority 优先 优先权
        params.setExecutor(new PriorityExecutor(2, true));
        params.setCancelFast(true);  // 是否可以被立即停止
        cancelable = x.http().get(params, new Callback.ProgressCallback<File>() {
            @Override
            public void onCancelled(CancelledException arg0) {
                Log.e(TAG, "取消" + Thread.currentThread().getName());
            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                Log.e(TAG, "onError: 失败" + Thread.currentThread().getName());
                b1 = arg1;
                //toast("语音下载失败");
                progressDialog.dismiss();
            }

            @Override
            public void onFinished() {
                Log.e(TAG, "完成,每次取消下载也会执行该方法" + Thread.currentThread().getName());
                progressDialog.dismiss();
            }

            @Override
            public void onSuccess(File arg0) {
                try{
                    Log.e(TAG, "下载成功的时候执行" + Thread.currentThread().getName());
                    b1 = true;
                    toast("语音下载完成");
                    mediaPlayer.setDataSource(Environment.getExternalStorageDirectory() + "/mine_audio.mp3");
                    mediaPlayer.prepare();
                    timeLength.setText(mediaPlayer.getDuration() / 1000 + "s");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                if (isDownloading) {
                    progressDialog.setProgress((int) (current * 100 / total));
                    Log.e(TAG, "下载中,会不断的进行回调:" + Thread.currentThread().getName());
                }
            }

            @Override
            public void onStarted() {
                Log.e(TAG, "开始下载的时候执行" + Thread.currentThread().getName());
                progressDialog.show();
            }

            @Override
            public void onWaiting() {
                Log.e(TAG, "等待,在onStarted方法之前执行" + Thread.currentThread().getName());
            }
        });
    }

    // 通过WOID得到设备编号
    private void getData5() {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadWoRtuByWOID");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadWoRtuByWOID");
        }
        String guidString = prefs1.getString("guid", "");
        params.addBodyParameter("woId", equipmentId);
        params.addBodyParameter("guid", guidString);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {}

            @Override
            public void onError(Throwable ex, boolean arg1) {
                //toast(ex.getMessage());
                //toast("获取设备编号失败");
            }

            @Override
            public void onFinished() {}

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        intent.setClass(DetailTaskActivity2.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")){
                        JSONArray jsonArray = new JSONArray(object1.getString("Data"));
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        equipmentNo = jsonObject.getString("EquipmentNo");
                        Log.e("DetailTaskActivity2", equipmentNo);
                    } else {
                        toast(object1.getString("Message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 反馈那个地方点击确定之后的提交
    private void submit(String s){
        try{
            Date date2 = new Date();
            String endDate = format1.format(date2);// 结束时间（年月日时分秒）
            totalNumber = Integer.valueOf(taskItemBean.getWOItemsNum()); // 巡检工单的总数量
            changeNumber = Integer.valueOf(taskItemBean.getWOPerformNum()); // 巡检工单的子数量
            // workorder 字段  这个不用jsonArray
            jsonObject1.put("WOID", taskItemBean.getWOID());
            jsonObject1.put("WOTitle", taskItemBean.getWOTitle());
            jsonObject1.put("WOContent", taskItemBean.getWOContent());
            jsonObject1.put("Voice", taskItemBean.getVoice());
            jsonObject1.put("WOState", taskItemBean.getWOState());
            jsonObject1.put("WOIssuedDate", taskItemBean.getWOIssuedDate());// 派发时间
            jsonObject1.put("WOIssuedUser", taskItemBean.getWOIssuedUser());// 派发人
            // 这个字段是获取的对象里面的接收时间，因为一旦你和之前一样给WOReceiveDate赋值就相当于“强制接收”了
            jsonObject1.put("WOReceiveDate", taskItemBean.getWOReceiveDate());
            jsonObject1.put("WOReceiveUser", taskItemBean.getWOReceiveUser());
            jsonObject1.put("WOItemsNum", totalNumber); // 巡检工单的总数量
            jsonObject1.put("WOPerformNum", changeNumber); // 巡检工单的子数量
            jsonObject1.put("WOBeginDate", taskItemBean.getWOBeginDate());
            // 这个字段是点击发送按钮获取的当前的年月日时分秒
            jsonObject1.put("WOEndDate", taskItemBean.getWOEndDate());
            jsonObject1.put("WOCreateDate", taskItemBean.getWOCreateDate());
            // 这是转发的WOType要填2代表巡检任务
            jsonObject1.put("WOType", "2");
            jsonObject1.put("WOExpectedTime", taskItemBean.getWOExpectedTime());
            jsonObject1.put("FBID", "1");
            jsonObject1.put("WOFeedback", s);
            // detail  字段      这个用jsonArray
            jsonObject2.put("WOID", taskItemBean.getWOID());
            jsonObject2.put("Num", "1");
            jsonObject2.put("OrderContent", taskItemBean.getWOContent());
            jsonObject2.put("OrderState", 0);
            jsonObject2.put("UserID", userId);
            jsonObject2.put("UserName", username);
            jsonObject2.put("DateTime", endDate);
            jsonArray.put(jsonObject2);
        } catch (JSONException e){
            e.printStackTrace();
        }
        // 更新工单状态
        getUpdateWorkOrder();
    }

    // 更新工单状态
    private void getUpdateWorkOrder() {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(mActivity, ConstantsField.ADDRESS))){
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
                toast("工单反馈失败");
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
                        toast("反馈成功");
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

    private void toast(String text){
        Toast.makeText(DetailTaskActivity2.this, text, Toast.LENGTH_SHORT).show();
    }
    // 如果媒体播放器还存在就释放了它
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (MediaManager.mMediaPlayer != null) {
            MediaManager.mMediaPlayer.stop();
            MediaManager.mMediaPlayer.release();
            MediaManager.mMediaPlayer = null;
        }
    }

    // 手机返回按钮返回功能
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                break;
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }
}
