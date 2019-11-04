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
 * 维修任务的派发activity，从主页直接点击进入DetailTaskActivity3，然后进入SendTaskActivity3
 *
 */
public class SendTaskActivity3 extends BaseActivity {

    @ViewInject(R.id.recordBtn)
    Button recordBtn; // 录音按钮
    @ViewInject(R.id.record_contentLayout)
    LinearLayout record_contentLayout; // 播放按钮的container
    @ViewInject(R.id.record_detailView)
    ImageView recordDetailView; // 播放按钮的图片
    @ViewInject(R.id.tv_time)
    TextView tv_time; // 播放按钮的时间
    private static final String TAG = "SendTaskActivity";
    private Loadding loadding;
    private TextView tvCenter;
    private EditText questionDescription;
    private SpinerPopWindow mSpinerPopWindow;
    // 下拉框
    private CustemSpinerAdapter mAdapter;
    private List<CustemInfo> mCustomInfors = new ArrayList<CustemInfo>();
    private int click = 0, totalNumber, changeNumber;
    private SharedPreferences prefs1;
    private Button sendTask;
    //private Recoder mRecoder;
    private String gatewayAddress, timeRecord, regionId = "", regionString = "",
            teamString = "", personString = "", voiceName = "", receiveDate = "", endDate = "",
            currentDate = "", guidString, voicePath = "", equipmentId = "", fbId = "",
            userId = "", username, flag = "a", tag = "0";
    private JSONObject jsonObject1, jsonObject2;
    private Date date = new Date();
    private SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
    private ArrayList<TaskItemBean> transferList;
    private JSONArray jsonArray;
    private Intent intent;
    private boolean audioRecorder = false;
    private AudioPlayerUtil player;
    private String ROOT_PATH;  // 根路径
    private ImageView mImageView;
    private TextView mTextView;
    private PopupWindowFactory mPop;
    private View view;
    private AnimationDrawable animationDrawable;

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
        if("b".equals(flag)){
            setResult(RESULT_OK, intent);  // 返回的时候给一个RESULT_OK
        }
        onBackPressed();
    }

    private void init() {
        intent = new Intent();
        loadding = new Loadding(this);
        prefs1 = getSharedPreferences("UserInfo", 0);
        jsonObject1 = new JSONObject();
        jsonObject2 = new JSONObject();
        //dialogManager = new DialogManager(this);
        //audioManager = AudioManager.getInstance();
        guidString = prefs1.getString("guid", "");
        gatewayAddress = prefs1.getString("add", "");
        userId = String.valueOf(prefs1.getInt("UserID", 0));
        username = prefs1.getString("userfullname", "");
        tvCenter = (TextView) findViewById(R.id.tv_center);
        tvCenter.setText("");
        questionDescription = (EditText) findViewById(R.id.question_description);
        //audioButton = (AudioRecordButton) findViewById(R.id.audioButton);
        //btnPlayButton = (Button) findViewById(R.id.btnPlayButton);
        sendTask = (Button) findViewById(R.id.send_task);
        initListener();
        // 接收从TaskListActivity里传递过来的集合数据
        transferList = (ArrayList<TaskItemBean>) getIntent().getSerializableExtra("currentChooseItems");
        // 一进入这个界面就获取到它的接收时间
        receiveDate = format1.format(date);  // 接收时间
        currentDate = format2.format(date);  // 当前时间
        Log.e("接收时间", receiveDate);
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
        initAudioRecorderBtn();
    }

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

        // 中心，这个给深圳做的，在10月29日改成一个接口全权弄的样子，目前注释掉的东西估计以后还会用得着，
        /*tvCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click = 1;
                // 每次点击tvCenter按钮就将这三个字符串清空一下，有利于后面对这三个字符串进行传值操作，
                // 并且防止点击popWindow外面发生不想要的TextView赋值错误问题。
                regionString = "";
                teamString = "";
                personString = "";
                codeValue = 0;
                tvCenter.setText("请选择人员");
                // 显示下拉窗体区域
                showSpinWindowArea();
            }
        });*/

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
                        intent.setClass(SendTaskActivity3.this, LoginActivity.class);
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
                    for(int i = 0; i < transferList.size(); i++){
                        jsonArray = new JSONArray();
                        totalNumber = Integer.valueOf(transferList.get(i).getWOItemsNum());// 维修工单的总数
                        changeNumber = Integer.valueOf(transferList.get(i).getWOPerformNum());// 维修工单的子数
                        // 往getData1接口里面传入WOID，往getData2接口里面传入WOID和实体类，先通过getData1
                        // 得到设备id，再通过getData2得到故障id，然后才能进行上传（通过AddWo接口）
                        getData1(transferList.get(i).getWOID(), transferList.get(i));
                        //Log.e(TAG, equipmentId);
                    }
                }
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });
        dialog.show();
    }

    // 发送工作任务的3个接口之一  AddWo接口   http://47.93.6.250:10041/Service/C_WNMS_API.asmx/AddWo
    private void getAddWo(String string1, String string2, String string3) {
        loadding.show("上传数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/AddWo");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/AddWo");
        }
        params.addBodyParameter("WOID", string1);
        params.addBodyParameter("WOTitle", string2);
        params.addBodyParameter("WOContent", string3);
        // 如果语音名称有值的话就附加值，如果没有值的话就赋值为空字符串
        if("".equals(voiceName)){
            params.addBodyParameter("Voice", "");
        } else {
            params.addBodyParameter("Voice", voiceName + ".mp3");
        }
        // 这个字段是发送者的id
        params.addBodyParameter("WOIssuedUser", userId);
        params.addBodyParameter("WOType", "1");
        params.addBodyParameter("UserID", regionId);
        /*// 根据现在情况来看，肯定有teamString，所以仅仅来判断personString就可以，如果personString
        // 为空字符串，就用teamId，如果personString不为空字符串，就用tvPersonId
        if ("".equals(personString)){
            params.addBodyParameter("UserID", teamId);
        } else {
            params.addBodyParameter("UserID", personId);
        }*/
        // 这个是根据WOID得到的设备ID
        params.addBodyParameter("EquipmentID", equipmentId);
        params.addBodyParameter("DetailContent", questionDescription.getText().toString().trim());
        params.addBodyParameter("FBID", fbId);
        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {}

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
                    // code是1的时候有添加失败和添加成功不同的Message
                    if (commonResponseBean.getCode() == 1) {
                        toast(commonResponseBean.getMessage());
                        // 这样写的目的在于防止上报成功之后再次重复上报
                        questionDescription.setText("");
                        flag = "b";
                    } else if (commonResponseBean.getCode() == 0) {
                        intent.setClass(SendTaskActivity3.this, LoginActivity.class);
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

    // 通过WOID得到设备id
    private void getData1(final String st1, final TaskItemBean bean1) {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadWoRtuByWOID");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadWoRtuByWOID");
        }
        params.addBodyParameter("woId", st1);
        params.addBodyParameter("guid", guidString);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {}

            @Override
            public void onError(Throwable ex, boolean arg1) {
                //toast(ex.getMessage());
                toast("设备id取得失败");
            }

            @Override
            public void onFinished() {}

            @Override
            public void onSuccess(String arg0) {
                try {
                    Log.e("SendTaskActivity", arg0);
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        intent.setClass(SendTaskActivity3.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")){
                        JSONArray jsonArray = new JSONArray(object1.getString("Data"));
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        equipmentId = jsonObject.getString("EquipmentID");
                        // 如果设备id是空字符串的话就不允许提报
                        if(TextUtils.isEmpty(equipmentId)){
                            toast("设备id不存在，不能提报");
                        } else {
                            // 通过WOID得到故障id
                            getData2(st1, bean1);
                        }
                        //Log.e("SendTaskActivity", equipmentId);
                    } else {
                        toast(object1.getString("Message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 通过WOID得到故障id
    private void getData2(final String st1, final TaskItemBean bean1) {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadFBByWoID");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadFBByWoID");
        }
        params.addBodyParameter("guid", guidString);
        params.addBodyParameter("id", st1);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {}

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast("故障id取得失败");
            }

            @Override
            public void onFinished() {}

            @Override
            public void onSuccess(String arg0) {
                try {
                    Log.e("SendTaskActivity", arg0);
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        intent.setClass(SendTaskActivity3.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")){
                        JSONObject jsonObject = new JSONObject(object1.getString("Data"));
                        fbId = jsonObject.getString("ID");
                        if(TextUtils.isEmpty(fbId)){
                            toast("故障id为空不能提报");
                        } else {
                            //先取得equipmentID和fbId才能上报下面这个接口
                            getAddWo(st1, bean1.getWOTitle(), bean1.getWOContent());  //  成功的话会显示更新工单成功
                            // 以下代码已经没有什么作用了
                            try{
                                // workorder 字段    这个不用jsonArray
                                jsonObject1.put("WOID", bean1.getWOID());
                                jsonObject1.put("WOTitle", bean1.getWOTitle());
                                jsonObject1.put("WOContent", bean1.getWOContent());
                                jsonObject1.put("Voice", voiceName);
                                jsonObject1.put("WOState", 1);// 代表已处理完成
                                jsonObject1.put("WOIssuedDate", endDate);// 派工时间
                                jsonObject1.put("WOIssuedUser", userId); // 派工人
                                // 这个字段是activity进入的时候就获取的年月日时分秒
                                jsonObject1.put("WOReceiveDate", receiveDate);
                                // 根据现在情况来看，肯定有teamString，所以仅仅来判断personString就
                                // 可以，如果personString为空字符串，就用teamId，如果personString不为
                                // 空字符串，就用tvPersonId
                                jsonObject1.put("WOReceiveUser", regionId);
                                /*if ("".equals(personString)){
                                    jsonObject1.put("WOReceiveUser", teamId);
                                } else {
                                    jsonObject1.put("WOReceiveUser", personId);
                                }*/
                                //jsonObject1.put("WOReceiveUser", userId);
                                jsonObject1.put("WOItemsNum", "1"); // 写死
                                jsonObject1.put("WOPerformNum", bean1.getWOPerformNum());
                                jsonObject1.put("WOBeginDate", "");
                                // 这个字段是点击发送按钮获取的当前的年月日时分秒
                                jsonObject1.put("WOEndDate", endDate);
                                jsonObject1.put("WOCreateDate", "");
                                jsonObject1.put("WOType", "1");
                                jsonObject1.put("WOExpectedTime", "");
                                jsonObject1.put("FBID", fbId);
                                jsonObject1.put("WOFeedback", "");
                                //Log.e("workorder字段", jsonObject1.toString());
                                // detail  字段      这个用jsonArray
                                jsonObject2.put("WOID", bean1.getWOID());
                                jsonObject2.put("Num", "1");
                                jsonObject2.put("OrderContent", voiceName);
                                // 这个一直没有改，直到18年5月25日才改成1,以前是“1”
                                jsonObject2.put("OrderState", 1);// 代表已处理完成
                                jsonObject2.put("UserID", userId);
                                jsonObject2.put("UserName", username);// 用户名
                                jsonObject2.put("DateTime", currentDate);
                                jsonArray.put(jsonObject2);
                                //Log.e("detail字段", jsonArray.toString());
                            } catch (JSONException e){
                                e.printStackTrace();
                            }
                            //Log.e("workorder字段2", jsonObject1.toString());
                            //Log.e("detail字段2", jsonArray.toString());
                            //  这里写3个接口方法
                            //getUpdateWorkOrder();  //  成功的话会显示添加成功
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

    // 数据的非空判断
    private boolean isEmpty() {
        if (tvCenter.getText().length() < 1) {
            toast("请选择维修人员");
            return false;
        }
        if(questionDescription.getText().length() < 1){
            toast("请填写问题描述");
            return false;
        }
        return true;
    }

    // 对Toast进行一下封装
    private void toast(String text) {
        Toast.makeText(SendTaskActivity3.this, text, Toast.LENGTH_SHORT).show();
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
                if("b".equals(flag)){
                    setResult(RESULT_OK, intent);// 返回的时候给一个RESULT_OK
                }
                finish();
                break;
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    // 发送工作任务的3个接口之一  UpdateWorkOrder接口
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
                    } else if (commonResponseBean.getCode() == 0) {
                        intent.setClass(SendTaskActivity3.this, LoginActivity.class);
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

    /*private void showSpinWindowArea() {
        switch (click) {
            case 1: // 点击区域
                getRegin();
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
        DisplayMetrics dm = new DisplayMetrics();   //  metrics  韵律学
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        mSpinerPopWindow.setWidth(width / 2);
        mSpinerPopWindow.showAsDropDown(tvCenter);
        mSpinerPopWindow.removeFooterView();
    }*/

    // 设置下拉框内容
    /*private void setHero(int pos) {

        CustemInfo value = mCustomInfors.get(pos);
        String id = value.getId();
        String nameString = value.getDeviceName();
        switch (click) {
            case 1: // 点击区域
                // 将得到的nameString传值给regionString，将2赋值给click，然后调用getTeam方法。
                regionId = id;
                tvCenter.setText(nameString);
                regionString = nameString;
                click = 2;
                // 点击区域人员之后就进入选择班组的方法里面
                getTeam(regionId);
                break;
            case 2: // 点击班组或者大区客服
                if(nameString.equals(username)){
                    toast("不能给自己派工");
                } else {
                    teamId = id;
                    tvCenter.setText(regionString + " " + nameString);
                    teamString = nameString;
                    // 如果codeValue为0或者1的话就代表有班组这个中间选项，那就走getPerson()方法，获取第三列的内容
                    if((codeValue == 0) ||(codeValue == 1)){
                        click = 3;
                        getPerson(teamId);
                    } else {
                        // 如果codeValue不为0也不为1的话就代表没有中间班组这个选项，在getTeam()方法里面走的是getInfoByRegion()方法
                        mSpinerPopWindow.dismiss();
                    }
                }
                break;
            case 3: // 点击人员
                if(nameString.equals(username)){
                    toast("不能给自己派工");
                } else {
                    personId = id;
                    tvCenter.setText(regionString + " " + teamString + " " + nameString);
                    personString = nameString;
                    mSpinerPopWindow.dismiss();
                }
                break;
        }
    }*/

    // 加载的区域，10月29日以下注释的部分可能还会用的着，在付连军的项目中，所以留着
    /*private void getRegin() {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadRegion");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadRegion");
        }
        params.addBodyParameter("userId", userId);
        params.addBodyParameter("guid", guidString);

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
                    Intent intent = new Intent();
                    CommonResponseBean commonResponseBean = new CommonResponseBean().getcommenHit(arg0);
                    if (commonResponseBean.getCode() == 1) {
                        mCustomInfors.clear();
                        JSONArray array = new JSONArray(commonResponseBean.getData());
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            CustemInfo info = new CustemInfo();
                            info.setId(object.getString("ID"));
                            info.setDeviceName(object.getString("Name"));
                            mCustomInfors.add(info);
                        }
                        mAdapter.notifyDataSetChanged();
                    } else if (commonResponseBean.getCode() == 0) {
                        intent.setClass(SendTaskActivity3.this, LoginActivity.class);
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

    // 加载班组
    private void getTeam(final String id) {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadTeam");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadTeam");
        }
        params.addBodyParameter("regionId", id);  // 区域id
        params.addBodyParameter("guid", guidString);

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
                    codeValue = commonResponseBean.getCode();
                    if (commonResponseBean.getCode() == 1) {
                        mCustomInfors.clear();
                        JSONArray array = new JSONArray(commonResponseBean.getData());
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            CustemInfo info = new CustemInfo();
                            info.setId(object.getString("TeamID"));
                            info.setDeviceName(object.getString("TeamName"));
                            mCustomInfors.add(info);
                        }
                        mAdapter.notifyDataSetChanged();
                    } else if (commonResponseBean.getCode() == 0) {
                        Intent intent = new Intent();
                        intent.setClass(SendTaskActivity3.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // 如果Code不为1和0的话就表明没有中间班组，就直接进入getInfoByRegion方法里面。
                        getInfoByRegion(id);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 加载大区客服和加载人员是一样的。分两种情况：如果有中间班组的话就加载人员，如果没有中间班组的话
    // 就加载大区客服
    private void getInfoByRegion(String id1) {
        loadding.show("获取数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadCrsInfoByRegion");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadCrsInfoByRegion");
        }
        params.addBodyParameter("regionId", id1); //  区域id
        params.addBodyParameter("guid", guidString);

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
                            info.setId(object.getString("ID"));
                            info.setDeviceName(object.getString("UserFullName"));
                            mCustomInfors.add(info);
                        }
                        mAdapter.notifyDataSetChanged();
                    } else if (commonResponseBean.getCode() == 0) {
                        Intent intent = new Intent();
                        intent.setClass(SendTaskActivity3.this, LoginActivity.class);
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

    // 加载人员和加载大区客服是一样的。分两种情况：一旦没有中间班组，就直接加载大区客服，如果有中间
    // 班组的话，就接着加载人员。
    private void getPerson(String id) {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadCrsInfo");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadCrsInfo");
        }
        params.addBodyParameter("teamId", id); // 班组id
        params.addBodyParameter("guid", guidString);

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
                    Intent intent = new Intent();
                    CommonResponseBean commonResponseBean = new CommonResponseBean().getcommenHit(arg0);
                    if (commonResponseBean.getCode() == 1) {
                        mCustomInfors.clear();
                        JSONArray array = new JSONArray(commonResponseBean.getData());
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            CustemInfo info = new CustemInfo();
                            info.setId(object.getString("ID"));
                            info.setDeviceName(object.getString("UserName"));
                            mCustomInfors.add(info);
                        }
                        mAdapter.notifyDataSetChanged();
                    } else if (commonResponseBean.getCode() == 0) {
                        intent.setClass(SendTaskActivity3.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        toast(commonResponseBean.getMessage());
                        mSpinerPopWindow.dismiss();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }*/
}
