package com.example.motor.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.adapter.AbstractSpinerAdapter;
import com.example.motor.adapter.CustemSpinerAdapter;
import com.example.motor.base.BaseActivity;
import com.example.motor.base.CustemInfo;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.CommonResponseBean;
import com.example.motor.db.TaskItemBean;
import com.example.motor.util.AudioPlayerUtil;
import com.example.motor.util.AudioRecorderUtil;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DoubleClickUtils;
import com.example.motor.util.Loadding;
import com.example.motor.util.PopupWindowFactory;
import com.example.motor.util.SPUtils;
import com.example.motor.util.TimeUtils;
import com.example.motor.widget.SpinerPopWindow;
import com.example.motor.workorderlist.inspection.InspectionWorkOrderActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 巡检任务的派发activity
 *
 */
public class SendTaskActivity2 extends BaseActivity {

    @ViewInject(R.id.recordBtn)
    Button recordBtn; // 录音按钮
    @ViewInject(R.id.record_contentLayout)
    LinearLayout record_contentLayout; // 播放按钮的container
    @ViewInject(R.id.record_detailView)
    ImageView recordDetailView; // 播放按钮的图片
    @ViewInject(R.id.tv_time)
    TextView tv_time; // 播放按钮的时间
    private static final String TAG = "SendTaskActivity2";
    private Loadding loadding;
    private TextView tvCenter;
    // 在xml文件中GONE掉了
    private EditText questionDescription;
    private SpinerPopWindow mSpinerPopWindow;
    // 下拉框
    private CustemSpinerAdapter mAdapter;
    private List<CustemInfo> mCustomInfors = new ArrayList<CustemInfo>();
    private int click = 0;
    private SharedPreferences prefs1;
    private SharedPreferences.Editor editor;
    private Button sendTask;
    private String gatewayAddress, regionId, regionString = "", teamString = "",
            personString = "", voiceName = "", receiveDate = "", endDate = "", currentDate = "",
            guidString, voicePath = "", fbId = "", userId = "", username, tag = "0";
    private int totalNumber, changeNumber;
    private JSONObject jsonObject1, jsonObject2;
    private Date date = new Date();
    private SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
    private ArrayList<TaskItemBean> transferList;
    private JSONArray jsonArray;
    private Intent intent;
    private StringBuilder equipmentIdString = new StringBuilder();
    private boolean audioRecorder = false;
    private AudioPlayerUtil player;
    private String ROOT_PATH; // 根路径
    private ImageView mImageView;
    private TextView mTextView;
    private PopupWindowFactory mPop;
    private View view;
    private AnimationDrawable animationDrawable;
    private int codeValue = 0;

    // layout文件
    @Override
    public int setLayout() {
        return R.layout.activity_send_task2;
    }

    // 在这里面设置TitleBar相关的
    @Override
    public void setTitle() {
        mBaseTitleBarView.setTitleText("派发任务");
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

    private void init() {
        intent = new Intent();
        loadding = new Loadding(this);
        prefs1 = getSharedPreferences("UserInfo", 0);
        editor = prefs1.edit();
        jsonObject1 = new JSONObject();
        jsonObject2 = new JSONObject();
        guidString = prefs1.getString("guid", "");
        gatewayAddress = prefs1.getString("add", "");
        userId = String.valueOf(prefs1.getInt("UserID", 0));
        username = prefs1.getString("userfullname", "");
        //toast(totalNumber + "婷婷" + changeNumber);
        tvCenter = (TextView) findViewById(R.id.tv_center);
        // 在xml文件中GONE掉了
        questionDescription = (EditText) findViewById(R.id.question_description);
        questionDescription.setText("");
        sendTask = (Button) findViewById(R.id.send_task);
        initListener();
        //taskItemBean = (TaskItemBean) getIntent().getSerializableExtra("taskitembean1");
        // 接收从TaskListActivity2里传递过来的集合数据
        transferList = (ArrayList<TaskItemBean>) getIntent().getSerializableExtra("currentChooseItems");
        // 一进入这个界面就获取到它的接收时间
        receiveDate = format1.format(date);
        currentDate = format2.format(date);
        totalNumber = Integer.valueOf(transferList.get(0).getWOItemsNum());// 巡检工单的总数
        changeNumber = Integer.valueOf(transferList.get(0).getWOPerformNum());// 巡检工单的子数
        //toast(totalNumber + "哈哈" + changeNumber);
        //Log.e("接收时间", receiveDate);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        try {
            ROOT_PATH = context.getExternalFilesDir(null).getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage() + "");
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() +
                        File.separator + context.getPackageName();
            } else {
                ROOT_PATH = context.getFilesDir().getAbsolutePath();
            }
        } catch (Throwable e) {
            ROOT_PATH = context.getFilesDir().getAbsolutePath();
        }
        // 初始化音频录制按钮
        initAudioRecorderBtn();
    }

    // 初始化音频录制按钮
    private void initAudioRecorderBtn() {
        view = View.inflate(this, R.layout.layout_microphone, null);
        mPop = new PopupWindowFactory(this, view);
        // PopupWindow布局文件里面的控件
        mImageView = (ImageView) view.findViewById(R.id.iv_recording_icon);
        mTextView = (TextView) view.findViewById(R.id.tv_recording_time);
        final AudioRecorderUtil audioRecorderUtil = new AudioRecorderUtil(ROOT_PATH +
                File.separator + "audio");
        // 录制的时候的监听方法
        audioRecorderUtil.setOnAudioStatusUpdateListener(
                new AudioRecorderUtil.OnAudioStatusUpdateListener() {
                    @Override
                    public void onStart() {}

                    @Override
                    public void onProgress(double db, long time) {
                        // 根据分贝值来设置录音时话筒图标的上下波动,同时设置录音时间
                        mImageView.getDrawable().setLevel((int) (3000 + 6000 * db / 100));
                        mTextView.setText(TimeUtils.long2String(time));
                    }

                    @Override
                    public void onError(Exception e) { }

                    @Override
                    public void onCancel() { }

                    @Override
                    public void onStop(String filePath) {
                        mPop.dismiss();
                        record_contentLayout.setVisibility(View.VISIBLE);
                        voicePath = filePath;
                        voiceName = filePath.split("audio/")[1].split("\\.")[0];
                        Log.e("===path", voicePath);
                        // TODO 上传音频文件
                        upload();
                    }
                });

        recordBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 停止播放
                if (player != null) {
                    player.stop();
                }
                audioRecorder = true;// 正在录音
                // 处理动作
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        audioRecorderUtil.start();
                        mPop.showAtLocation(view.getRootView(), Gravity.CENTER, 0, 0);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        long time = audioRecorderUtil.getSumTime();
                        if (time < 1000) {
                            audioRecorderUtil.cancel();
                            toast("录音时间太短！");
                            voicePath = "";
                            voiceName = "";
                        } else {
                            tv_time.setText(time / 1000 + "s");
                        }
                        mImageView.getDrawable().setLevel(0);
                        mTextView.setText(TimeUtils.long2String(0));
                        audioRecorderUtil.stop();
                        mPop.dismiss();
                        break;
                }
                return true;
            }
        });

        // 播放按钮的点击事件
        record_contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(voicePath) || !audioRecorder) {
                    return;
                }
                if (player == null) {
                    player = new AudioPlayerUtil();
                } else {
                    player.stop();
                }
                recordDetailView.setImageResource(R.drawable.play_voice_right_anim);
                animationDrawable = (AnimationDrawable) recordDetailView.getDrawable();
                animationDrawable.start();
                player.start(voicePath, new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        animationDrawable.stop();
                        // 这个绿色的扩音器是在第一次播放完毕之后才出现，语音显现的时候是出现的
                        // 白色的那个扩音器
                        recordDetailView.setImageResource(R.drawable.adj);
                    }
                });
            }
        });
    }

    private void initListener() {
        // 开始点击人员
        tvCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regionId = prefs1.getString("RegionNo", "");
                click = 1;
                // 每次点击tvCenter按钮就将这三个字符串清空一下，有利于后面对这三个字符串进行传值操作，
                // 并且防止点击popWindow外面发生不想要的TextView赋值错误问题。
                regionString = "";
                tag = regionId.equals("") ? "0" : "1";
                tvCenter.setText("请选择人员");
                // 显示下拉窗体区域
                showSpinWindowArea();
            }
        });

        // 发送工作任务的点击事件
        sendTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpDialog();
            }
        });
    }

    private void showSpinWindowArea() {
        switch (click) {
            case 1:
                // 一个接口解决一切
                getInfoByRegion();
                break;
        }
        mAdapter = new CustemSpinerAdapter(this);
        mAdapter.refreshData(mCustomInfors, 0);
        mSpinerPopWindow = new SpinerPopWindow(this);
        mSpinerPopWindow.setAdatper(mAdapter);
        mSpinerPopWindow.setItemListener(new AbstractSpinerAdapter.IOnItemSelectListener() {
            @Override
            public void onItemClick(int pos) {
                setHero(pos);
            }
        });
        DisplayMetrics dm = new DisplayMetrics();  // metrics  韵律学
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        mSpinerPopWindow.setWidth(width / 2);
        mSpinerPopWindow.showAsDropDown(tvCenter);
        mSpinerPopWindow.removeFooterView();
    }

    // 设置下拉框内容
    private void setHero(int pos) {

        CustemInfo value = mCustomInfors.get(pos);
        String id = value.getId();
        String nameString = value.getDeviceName();
        String isRtu = value.getCity();  // 这个用于查看isRTU为true还是false
        switch (click) {
            case 1:
                if(nameString.contains(username)){
                    toast("不能给自己派工");
                } else {
                    // 将得到的nameString传值给regionString，将2赋值给click，然后调用getTeam方法。
                    regionId = id;
                    tvCenter.setText(nameString);
                    regionString = nameString;
                    tag = "1";  // 做一下标记，目的是使用同一个网络请求接口，请求里面分两种
                    click = 2;
                    // 如果isRtu为true的话说明没有后一级了，那就将窗体关闭，如果不是的话那就继续请求接口
                    if(isRtu.equals("true")){
                        mSpinerPopWindow.dismiss();
                    } else {
                        // 一个接口解决一切
                        getInfoByRegion();
                    }
                }
                break;
            case 2:
                if(nameString.contains(username)){
                    toast("不能给自己派工");
                } else {
                    regionId = id;
                    tvCenter.setText(regionString + " " + nameString);
                    teamString = nameString;
                    click = 3;
                    // 如果isRtu为true的话说明没有后一级了，那就将窗体关闭，如果不是的话那就继续请求接口
                    if(isRtu.equals("true")){
                        mSpinerPopWindow.dismiss();
                    } else {
                        // 一个接口解决一切
                        getInfoByRegion();
                    }
                }
                break;
            case 3:
                if(nameString.contains(username)){
                    toast("不能给自己派工");
                } else {
                    regionId = id;
                    tvCenter.setText(regionString + " " + teamString + " " + nameString);
                    personString = nameString;
                    mSpinerPopWindow.dismiss();
                }
                break;
        }
    }

    // 一个接口解决一切
    private void getInfoByRegion() {
        loadding.show("获取数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadCrsInfoByRegion");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadCrsInfoByRegion");
        }
        params.addBodyParameter("regionId", ""); //  区域id
        params.addBodyParameter("guid", "");

        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast(ex.getMessage());
            }

            @Override
            public void onFinished() {
                if (loadding.isShow()) {
                    loadding.close();
                }
            }

            @Override
            public void onSuccess(String arg0) {
                try {
                    CommonResponseBean commonResponseBean = new CommonResponseBean().getcommenHit(arg0);
                    if (commonResponseBean.getCode() == 1) {
                        mCustomInfors.clear();
                        JSONArray array = new JSONArray(commonResponseBean.getData());
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            CustemInfo info = new CustemInfo();
                            if(tag.equals("0")){
                                // 这是第一级别的数据集合
                                if(object.getString("pId").equals("0")){
                                    info.setId(object.getString("id"));
                                    info.setDeviceName(object.getString("name"));
                                    info.setCity(object.getString("isRTU"));  // 这个用于查看isRTU为true还是false
                                    mCustomInfors.add(info);
                                }
                            } else if(tag.equals("1")){
                                // 这是第二级别和第三级别的数据集合
                                if(object.getString("pId").equals(regionId)){
                                    info.setId(object.getString("id"));
                                    info.setDeviceName(object.getString("name"));
                                    info.setCity(object.getString("isRTU"));  // 这个用于查看isRTU为true还是false
                                    mCustomInfors.add(info);
                                }
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    } else if (commonResponseBean.getCode() == 0) {
                        Intent intent = new Intent();
                        intent.setClass(SendTaskActivity2.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        toast(commonResponseBean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 跳转一个对话框
    private void jumpDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        //dialog.setTitle("请选择");
        dialog.setMessage("您确定提交吗？");
        dialog.setCancelable(true);
        dialog.setPositiveButton("是的", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isEmpty() && !DoubleClickUtils.isFastDoubleClick()) {
                    Date date2 = new Date();
                    endDate = format1.format(date2);
                    // 用这个for循环来取出所有的equipmentId
                    for(int i = 0; i < transferList.size(); i++){
                        jsonArray = new JSONArray();
                        equipmentIdString.append(transferList.get(i).getEquipmentID()).append(",");
                    }
                    // 成功的话会显示添加成功，这个地方在18年5月27号一直有一个bug，那就是设备id
                    // 可以直接从这个子项里面取到，而通过工单id取设备id是总是取一个相同的设备id的，
                    // 因为巡检工单发出来的工单id都是一样的
                    try{
                        if(changeNumber + 1 < totalNumber){
                            // workorder 字段    这个是json
                            jsonObject1.put("WOID", transferList.get(0).getWOID()); //
                            jsonObject1.put("WOTitle", transferList.get(0).getWOTitle());
                            jsonObject1.put("WOContent", transferList.get(0).getWOContent());  //
                            jsonObject1.put("Voice", voiceName);
                            jsonObject1.put("WOState", 0);// 代表未处理完成
                            jsonObject1.put("WOIssuedDate", endDate);// 派工时间
                            jsonObject1.put("WOIssuedUser", userId);// 派工人
                            // 这个字段是activity进入的时候就获取的年月日时分秒
                            jsonObject1.put("WOReceiveDate", receiveDate);
                            jsonObject1.put("WOReceiveUser", regionId);
                            /*// 根据现在情况来看，肯定有teamString，所以仅仅来判断personString就
                            // 可以，如果personString为空字符串，就用teamId，如果personString不为
                            // 空字符串，就用tvPersonId
                            if ("".equals(personString)){
                                jsonObject1.put("WOReceiveUser", teamId);
                            } else {
                                jsonObject1.put("WOReceiveUser", personId);
                            }*/
                            jsonObject1.put("WOItemsNum", totalNumber); // 巡检工单的总数量
                            jsonObject1.put("WOPerformNum", transferList.get(0).getWOPerformNum()); // 巡检工单的子数量
                            jsonObject1.put("WOBeginDate", "");
                            // 这个字段是点击发送按钮获取的当前的年月日时分秒
                            jsonObject1.put("WOEndDate", endDate);
                            jsonObject1.put("WOCreateDate", "");
                            // 这是转发的WOType要填2代表巡检任务
                            jsonObject1.put("WOType", "2");
                            jsonObject1.put("WOExpectedTime", "");
                            jsonObject1.put("FBID", "1");
                            jsonObject1.put("WOFeedback", "");
                            // Log.e("workorder字段", jsonObject1.toString());
                            // detail  字段      这个用jsonArray
                            jsonObject2.put("WOID", transferList.get(0).getWOID());
                            jsonObject2.put("Num", "1");
                            jsonObject2.put("OrderContent", transferList.get(0).getWOContent());
                            // 这个一直没有改，直到18年5月25日才改成0,以前是“0”
                            jsonObject2.put("OrderState", 0);// 代表未处理完成
                            jsonObject2.put("UserID", userId);
                            jsonObject2.put("UserName", username);
                            jsonObject2.put("DateTime", currentDate);
                            jsonArray.put(jsonObject2);
                        } else if ((changeNumber + 1) == totalNumber){
                            // workorder 字段    这个是json
                            jsonObject1.put("WOID", transferList.get(0).getWOID());
                            jsonObject1.put("WOTitle", transferList.get(0).getWOTitle());
                            jsonObject1.put("WOContent", transferList.get(0).getWOContent());
                            jsonObject1.put("Voice", voiceName);
                            jsonObject1.put("WOState", 1);// 代表已处理完成
                            jsonObject1.put("WOIssuedDate", endDate);// 派工时间
                            jsonObject1.put("WOIssuedUser", userId); // 派工人
                            // 这个字段是activity进入的时候就获取的年月日时分秒
                            jsonObject1.put("WOReceiveDate", receiveDate);
                            jsonObject1.put("WOReceiveUser", regionId);
                            /*// 根据现在情况来看，肯定有teamString，所以仅仅来判断personString就
                            // 可以，如果personString为空字符串，就用teamId，如果personString不为
                            // 空字符串，就用tvPersonId
                            if ("".equals(personString)){
                                jsonObject1.put("WOReceiveUser", teamId);
                            } else {
                                jsonObject1.put("WOReceiveUser", personId);
                            }*/
                            jsonObject1.put("WOItemsNum", totalNumber);// 巡检工单的总数
                            jsonObject1.put("WOPerformNum", transferList.get(0).getWOPerformNum());// 巡检工单的子数量
                            jsonObject1.put("WOBeginDate", "");
                            // 这个字段是点击发送按钮获取的当前的年月日时分秒
                            jsonObject1.put("WOEndDate", endDate);
                            jsonObject1.put("WOCreateDate", "");
                            // 这是转发的WOType要填2代表巡检任务
                            jsonObject1.put("WOType", "2");
                            jsonObject1.put("WOExpectedTime", "");
                            jsonObject1.put("FBID", "1");
                            jsonObject1.put("WOFeedback", "");
                            //Log.e("workorder字段", jsonObject1.toString());
                            // detail  字段      这个用jsonArray
                            jsonObject2.put("WOID", transferList.get(0).getWOID());
                            jsonObject2.put("Num", "1");
                            jsonObject2.put("OrderContent", transferList.get(0).getWOContent());
                            // 这个一直没有改，直到18年5月25日才改成1,以前是“1”
                            jsonObject2.put("OrderState", 1);// 代表已处理完成
                            jsonObject2.put("UserID", userId);
                            jsonObject2.put("UserName", username);
                            jsonObject2.put("DateTime", currentDate);
                            jsonArray.put(jsonObject2);
                        }
                        //Log.e("detail字段", jsonArray.toString());
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                    //getUpdateWorkOrder();  // 成功的话会显示更新状态成功
                    getAddWo(equipmentIdString.substring(0, equipmentIdString.length() - 1));
                }
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.show();
    }

    // 发送工作任务的3个接口之一  AddWo接口   http://47.93.6.250:10041/Service/C_WNMS_API.asmx/AddWo
    private void getAddWo(String string1) {
        loadding.show("上传数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/AddWo");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/AddWo");
        }
        // 巡检工单的WOID是一样的，因此集合第一个元素的WOID就是大家的WOID
        params.addBodyParameter("WOID", transferList.get(0).getWOID());
        // 巡检工单的WOTitle也是一样的，因此集合第一个元素的title就是大家的title
        params.addBodyParameter("WOTitle", transferList.get(0).getWOTitle());
        // 巡检工单的WOContent也是一样的，因此集合第一个元素的WOContent就是大家的WOContent
        params.addBodyParameter("WOContent", transferList.get(0).getWOContent());
        // 如果语音名称有值的话就附加值，如果没有值的话就赋值为空字符串
        if("".equals(voiceName)){
            params.addBodyParameter("Voice", "");
        } else {
            params.addBodyParameter("Voice", voiceName + ".mp3");
        }
        // 这个字段是发送者的id
        params.addBodyParameter("WOIssuedUser", userId);
        params.addBodyParameter("WOType", "2");
        params.addBodyParameter("UserID", regionId);
        /*// 根据现在情况来看，肯定有teamString，所以仅仅来判断personString就可以，如果personString
        // 为空字符串，就用teamId，如果personString不为空字符串，就用tvPersonId
        if ("".equals(personString)){
            params.addBodyParameter("UserID", teamId);
        } else {
            params.addBodyParameter("UserID", personId);
        }*/
        // 这个是根据集合里面的子项，拿出来的设备id
        params.addBodyParameter("EquipmentID", string1);
        params.addBodyParameter("DetailContent", questionDescription.getText().toString().trim());
        params.addBodyParameter("FBID", "1");
        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                //toast(ex.getMessage());
                // 不加上传失败是因为曾经给王五转发工单，但是王五这个别名没有生成，导致走onError方法，
                // 但是那个工单还给处理了
                //toast("上传失败");
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
                    //code是1的时候有添加失败和添加成功不同的Message
                    if (commonResponseBean.getCode() == 1) {
                        toast(commonResponseBean.getMessage());
                        // 这样做的目的在于防止提交完毕之后的二次点击提交
                        tvCenter.setText("请选择人员");
                    } else if (commonResponseBean.getCode() == 0) {
                        intent.setClass(SendTaskActivity2.this, LoginActivity.class);
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

    // 发送工作任务的3个接口之一  GetYuYin接口      上传文件
    private void upload(){
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/GetYuYin");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/GetYuYin");
        }
        try {
            params.setMultipart(true);
            params.addBodyParameter("file", new File(voicePath)); //  设置上传的文件路径
        }catch (Exception e){
            e.printStackTrace();
        }
        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                CommonResponseBean commonResponseBean = new CommonResponseBean().getcommenHit(result);
                //toast(commonResponseBean.getMessage());
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                toast(ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) { }

            @Override
            public void onFinished() {
                if (loadding.isShow()) {
                    loadding.close();
                }
            }
        });
    }

    // 数据的非空判断
    private boolean isEmpty() {
        if (tvCenter.getText().equals("请选择人员")) {
            toast("请选择维修人员");
            return false;
        }
        /*if(questionDescription.getText().length() < 1){
            toast("请填写问题描述");
            return false;
        }*/
        return true;
    }

    // 对Toast进行一下封装
    private void toast(String text) {
        Toast.makeText(SendTaskActivity2.this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.stop();
        }
    }

    // 手机返回按钮返回功能
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    // 发送工作任务的3个接口之一  UpdateWorkOrder接口  更新工单状态
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
                        intent.setClass(SendTaskActivity2.this, InspectionWorkOrderActivity.class);
                        startActivity(intent);
                        finish();
                        toast(commonResponseBean.getMessage());
                    } else if (commonResponseBean.getCode() == 0) {
                        intent.setClass(SendTaskActivity2.this, LoginActivity.class);
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
}
