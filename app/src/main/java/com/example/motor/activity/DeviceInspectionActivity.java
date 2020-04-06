package com.example.motor.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.base.BaseActivity;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.CommonResponseBean;
import com.example.motor.db.DeviceInspectionData;
import com.example.motor.db.EquipInsBean;
import com.example.motor.db.InspectionOffineItem;
import com.example.motor.db.InspectionOffineStateItem;
import com.example.motor.nfc.ByteArrayChange;
import com.example.motor.nfc.ToStringHex;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DateTimePickDialogUtil;
import com.example.motor.util.DoubleClickUtils;
import com.example.motor.util.Loadding;
import com.example.motor.util.NetWorkUtil;
import com.example.motor.util.SPUtils;
import com.google.gson.Gson;
import com.xys.libzxing.zxing.activity.CaptureActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 这个activity是服务里面点击设备巡检再点击子项跳转过来的，与activity2的区别在于返回按钮和底下
 * 返回键返回的地方不同
 *
 */
public class DeviceInspectionActivity extends BaseActivity {

    @ViewInject(R.id.tv_device_GKMC)
    EditText GKMC;  // 顾客名称
    @ViewInject(R.id.tv_device_SBBH)
    EditText SBBH;  // 设备编号
    @ViewInject(R.id.tv_device_SBXH)
    EditText SBXH;  // 设备型号
    @ViewInject(R.id.tv_device_XJRY)
    EditText XJRY;  // 巡检人员
    @ViewInject(R.id.tv_device_XJRQ)
    TextView XJRQ;  // 巡检日期
    @ViewInject(R.id.tv_device_HJQK)
    EditText HJQK;  // 环境情况
    @ViewInject(R.id.tv_device_SRDY1)
    EditText SRDY1;  // 输入电源1
    @ViewInject(R.id.tv_device_SRDY2)
    EditText SRDY2;  // 输入电源2
    @ViewInject(R.id.tv_pump_SBXH)
    EditText pumpSBXH;  // 水泵型号
    @ViewInject(R.id.tv_pump_SBZX)
    EditText pumpSBZX;  // 水泵转向
    @ViewInject(R.id.tv_SDYL)
    EditText SDYL;  // 设定压力
    @ViewInject(R.id.tv_SJYL)
    EditText SJYL;  // 实际压力
    @ViewInject(R.id.tv_CYBH)
    EditText CYBH;  // 超压保护
    @ViewInject(R.id.tv_JKFS)
    EditText JKFS;  // 监控方式
    @ViewInject(R.id.tv_device_YSKJYL)
    EditText YSKJYL;  // 有水开机压力值
    @ViewInject(R.id.tv_device_WSTJBH)
    EditText WSTJBH;  // 无水停机压力值
    @ViewInject(R.id.tv_device_XLLBY)
    EditText XLLBY;  // 小流量保压值
    @ViewInject(R.id.tv_device_SBYX)
    EditText SBYX;  // 水泵运行情况
    @ViewInject(R.id.tv_device_DJDL1)
    EditText DJDL1;  // 电机额定电流1
    @ViewInject(R.id.tv_device_DJDL2)
    EditText DJDL2;  // 电机额定电流2
    @ViewInject(R.id.tv_device_BPQDL)
    EditText BPQDL;  // 变频器动作电流值
    @ViewInject(R.id.tv_device_RE11)
    EditText RE11;  // 热继电器动作值11
    @ViewInject(R.id.tv_device_RE21)
    EditText RE21;  // 热继电器动作值21
    @ViewInject(R.id.tv_device_RE31)
    EditText RE31;  // 热继电器动作值31
    @ViewInject(R.id.tv_device_RE41)
    EditText RE41;  // 热继电器动作值41
    @ViewInject(R.id.tv_device_RE12)
    EditText RE12;  // 热继电器动作值12
    @ViewInject(R.id.tv_device_RE22)
    EditText RE22;  // 热继电器动作值22
    @ViewInject(R.id.tv_device_RE32)
    EditText RE32;  // 热继电器动作值32
    @ViewInject(R.id.tv_device_RE42)
    EditText RE42;  // 热继电器动作值42
    @ViewInject(R.id.tv_device_BENG1)
    EditText BENG1;  // 恒压各泵频率1
    @ViewInject(R.id.tv_device_BENG2)
    EditText BENG2;  // 恒压各泵频率2
    @ViewInject(R.id.tv_device_BENG3)
    EditText BENG3;  // 恒压各泵频率3
    @ViewInject(R.id.tv_device_BENG4)
    EditText BENG4;  // 恒压各泵频率4
    @ViewInject(R.id.tv_device_KZGBY)
    EditText KZGBY;  // 控制柜保养记录
    @ViewInject(R.id.tv_device_SBGDBY)
    EditText SBGDBY;  // 水泵及管道等保养记录
    @ViewInject(R.id.tv_device_ZJYXJL)
    EditText ZJYXJL;  // 整机运行记录
    @ViewInject(R.id.tv_device_YHDZ)
    EditText YHDZ;  // 用户地址
    @ViewInject(R.id.tv_device_YHDH)
    EditText YHDH;  // 用户电话
    @ViewInject(R.id.tv_device_YHQZ)
    EditText YHQZ;  // 用户签字
    @ViewInject(R.id.radiobutton1)
    EditText radioButton1;  // 前级开关压线情况
    @ViewInject(R.id.radiobutton2)
    EditText radioButton2;  // 电机压线情况
    @ViewInject(R.id.radiobutton3)
    EditText radioButton3;  // 监控连接线第三个
    @ViewInject(R.id.radiobutton4)
    EditText radioButton4;  // 主线路触电压线情况
    @ViewInject(R.id.radiobutton5)
    EditText radioButton5;  // 水泵填料密封漏水在范围内
    @ViewInject(R.id.radiobutton6)
    EditText radioButton6;  // 水泵机械密封是否有漏水现象
    @ViewInject(R.id.radiobutton7)
    EditText radioButton7;  // 接触器是否灵活复位
    @ViewInject(R.id.radiobutton8)
    EditText radioButton8;  // 控制柜内螺丝是否松动
    @ViewInject(R.id.radiobutton9)
    EditText radioButton9;  // 微机控制系统接线是否良好
    @ViewInject(R.id.radiobutton10)
    EditText radioButton10;  // 各水泵手动是否正常工作
    @ViewInject(R.id.radiobutton11)
    EditText radioButton11;  // 自动能否正常启动
    @ViewInject(R.id.radiobutton12)
    EditText radioButton12;  // 开停机是否正常
    @ViewInject(R.id.radiobutton13)
    EditText radioButton13;  // 止回阀是否正常
    @ViewInject(R.id.radiobutton14)
    EditText radioButton14;  // 自动交换是否正常
    @ViewInject(R.id.radiobutton15)
    EditText radioButton15;  // 液位计工作是否正常
    @ViewInject(R.id.radiobutton16)
    EditText radioButton16;  // 真空补偿器各指示灯是否正常
    @ViewInject(R.id.radiobutton17)
    EditText radioButton17;  // 变频器工作是否正常
    @ViewInject(R.id.radiobutton18)
    EditText radioButton18;  // 设备是否产生负压
    @ViewInject(R.id.radiobutton19)
    EditText radioButton19;  // 过滤器是否进行清洗
    @ViewInject(R.id.radiobutton20)
    EditText radioButton20;  // 消防水池是否有水
    @ViewInject(R.id.radiobutton21)
    EditText radioButton21;  // 消防控制中心状态是否正常
    @ViewInject(R.id.radiobutton22)
    EditText radioButton22;  // 无压巡检是否正常
    @ViewInject(R.id.radiobutton23)
    EditText radioButton23;  // 管网所有螺丝是否全面紧固
    @ViewInject(R.id.radiobutton24)
    EditText radioButton24;  // 浮球阀是否检查保养正常
    @ViewInject(R.id.radiobutton25)
    EditText radioButton25;  // 巡检人员是否按以上试验功能
    @ViewInject(R.id.radiobutton26)
    RadioButton radioButton26;  // 用户能否正常操作

    private DeviceInspectionData data = null;
    private Loadding loadding;
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 设置日期格式
    private DateTimePickDialogUtil dateTimePicKDialog;
    private SharedPreferences prefs1, prefs2;
    private SharedPreferences.Editor editor;
    private Intent intent;
    private String address, guid, userId, receiveDate, endDate,
            currentDate, fbId = "", username;
    private int totalNumber, changeNumber;
    private Date date = new Date();
    private SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
    private String string1, string2, string3, string4, string5, string6, string7, string8,
            string9, string10, string11, string12, string13, string14, string15, string16, string17, string18,
            string19, string20, string21, string22, string23, string24, string25, string26;
    private JSONObject jsonObject1, jsonObject2;
    private JSONArray jsonArray;
    private LocationClient mLocationClient;
    private StringBuilder currentPosition;
    private EquipInsBean equipInsBean;

    // nfc相关
    private NfcAdapter nfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private String info = "";

    private List<String> result = new ArrayList<>();
    private List<String> list = new ArrayList<>();

    // layout文件
    @Override
    public int setLayout() {
        // LocationClient构建方法接收Context参数，getApplicationContext获取一个全局Context参数
        mLocationClient = new LocationClient(getApplicationContext());
        // 注册定位监听器，获取到位置信息时会回调
        mLocationClient.registerLocationListener(new DeviceInspectionActivity.MyLocationListener());
        return R.layout.activity_decive_inspection;
    }

    // 在这里面设置TitleBar相关的
    @Override
    public void setTitle() {
        mBaseTitleBarView.setTitleText("设备巡检详情");
        mBaseTitleBarView.setLeftDrawable(-1);
    }

    // 初始化控件，设置监听
    @Override
    public void initWidget() {
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);
        requestLocation();
        initView();
        // 因为MainActivity里面已经对NFC进行了处理，因此在这里去掉
        //initNFC();
    }

    // View的click事件
    @Override
    public void widgetClick(View v) {}

    // 返回键事件
    @Override
    public void onTitleLeftPressed() {
        setResult(RESULT_OK, intent);
        finish();
    }

    private void initView() {
        loadding = new Loadding(this);
        prefs1 = getSharedPreferences("UserInfo", 0);
        prefs2 = getSharedPreferences("device", Context.MODE_PRIVATE);
        editor = prefs1.edit();
        intent = new Intent();
        address = prefs1.getString("add", "");
        guid = prefs1.getString("guid", "");
        userId = String.valueOf(prefs1.getInt("UserID", 0));
        // 偏好设置里面的username是id，userfullname是文字
        username = prefs1.getString("userfullname", "");
        jsonArray = new JSONArray();
        jsonObject1 = new JSONObject();
        jsonObject2 = new JSONObject();
        // 传过来的实体类
        equipInsBean = (EquipInsBean) getIntent().getSerializableExtra("taskitembean1");
        totalNumber = Integer.valueOf(equipInsBean.getWOItemsNum());// 巡检工单的总数量
        changeNumber = Integer.valueOf(equipInsBean.getWOPerformNum());// 巡检工单的子数量
        receiveDate = format1.format(date); // 开始时间
        currentDate = format2.format(date);// 当前时间（年月日）
        dateTimePicKDialog = new DateTimePickDialogUtil(this, df.format(new Date()));
        XJRQ.setText(df.format(new Date()));
        GKMC.setText("");
        SBBH.setText("");
        SBXH.setText("");
        SBXH.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                // 先移除当前监听，避免死循环。
                SBXH.removeTextChangedListener(this);
                String string = SBXH.getText().toString().toUpperCase();
                SBXH.setText(string);
                SBXH.setSelection(string.length());// 让光标定位最后位置。
                // 操作完当前显示内容之后，再添加监听。
                SBXH.addTextChangedListener(this);
            }
        });
        // 将小写字母变成大写字母
        //SBXH.setTransformationMethod(new UpperCaseTransform());
        XJRY.setText("");
        SRDY1.setText("");
        SRDY2.setText("");
        pumpSBXH.setText("");
        pumpSBXH.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                // 先移除当前监听，避免死循环。
                pumpSBXH.removeTextChangedListener(this);
                String string = pumpSBXH.getText().toString().toUpperCase();
                pumpSBXH.setText(string);
                pumpSBXH.setSelection(string.length());// 让光标定位最后位置。
                // 操作完当前显示内容之后，再添加监听。
                pumpSBXH.addTextChangedListener(this);
            }
        });
        // 将小写字母变成大写字母
        //pumpSBXH.setTransformationMethod(new UpperCaseTransform());
        SDYL.setText("");
        SJYL.setText("");
        CYBH.setText("");
        XLLBY.setText("");
        DJDL1.setText("");
        JKFS.setText("");
        radioButton5.setText("");
        radioButton6.setText("");
        RE11.setText("");
        //RE12.setText("");
        RE21.setText("");
        //RE22.setText("");
        RE31.setText("");
        //RE32.setText("");
        RE41.setText("");
        //RE42.setText("");
        BENG1.setText("");
        //BENG2.setText("");
        //BENG3.setText("");
        //BENG4.setText("");
        RE11.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });

        RE21.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });

        RE31.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });

        RE41.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    RE12.setSelection(RE12.getText().length());
                }
                return false;
            }
        });

        RE12.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    RE22.setSelection(RE22.getText().length());
                }
                return false;
            }
        });

        RE22.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    RE32.setSelection(RE32.getText().length());
                }
                return false;
            }
        });

        RE32.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    RE42.setSelection(RE42.getText().length());
                }
                return false;
            }
        });

        RE42.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    BPQDL.setSelection(BPQDL.getText().length());
                }
                return false;
            }
        });
        BENG1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    BENG2.setSelection(BENG2.getText().length());
                }
                return false;
            }
        });
        BENG2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    BENG3.setSelection(BENG3.getText().length());
                }
                return false;
            }
        });
        BENG3.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    BENG4.setSelection(BENG4.getText().length());
                }
                return false;
            }
        });
        BENG4.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    KZGBY.setSelection(KZGBY.getText().length());
                }
                return false;
            }
        });
        radioButton26.setChecked(true);
        YHDH.setText("");
        YHDZ.setText("");
        YHQZ.setText("");
        GKMC.setText(equipInsBean.getDeviceName());// 顾客名称
        XJRY.setText(username);// 巡检人员
        SBBH.setText(equipInsBean.getEquipmentNo());// 设备编号
        // 通过设备编号得到设备信息
        getData3();
        // EditText获取焦点状态从而实现一些操作
        SBBH.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // 设备编号不为空字符串的话才进行操作
                    if(!TextUtils.isEmpty(SBBH.getText())){
                        // 通过设备编号得到设备信息
                        getData3();
                    }
                }
            }
        });
    }

    @Event(value = {R.id.tv_submit, R.id.tv_device_XJRQ, R.id.scan_scan}, type = View.OnClickListener.class)
    private void btnClick(View view) {
        switch (view.getId()) {
            case R.id.tv_device_XJRQ:
                if(!DoubleClickUtils.isFastDoubleClick()){
                    dateTimePicKDialog.dateTimePicKDialog(XJRQ);
                }
                break;
            case R.id.tv_submit:
                if(!DoubleClickUtils.isFastDoubleClick() && isEmpty()){
                    // 跳转一个对话框
                    jumpDialog();
                }
                break;
            case R.id.scan_scan:
                if(!DoubleClickUtils.isFastDoubleClick()){
                    startActivityForResult(new Intent(DeviceInspectionActivity.this,
                            CaptureActivity.class), 1);
                }
                break;
        }
    }

    // 在initWidget()方法里面有这个方法的调用
    private void requestLocation(){
        initLocation();
        mLocationClient.start();//开始定位，定位的结果会回调到监听器中
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);// 设置更新间隔，5s更新一下当前位置  scan扫描span跨度
        // 表示要获取当前位置的详细信息，获取地址信息一定要用网络，所以即使时Device_Sensors模式
        // 也会自动开启网络定位功能
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    // 这个经纬度位置5秒刷新一次
    private class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            currentPosition = new StringBuilder();
            currentPosition.append(bdLocation.getProvince()).append(" ");
            currentPosition.append(bdLocation.getCity()).append(" ");
            currentPosition.append(bdLocation.getDistrict()).append(" ");
            currentPosition.append(bdLocation.getStreet()).append(" ");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String result = bundle.getString("result");
                    SBBH.setText(result);
                }
                break;
        }
    }

    // 跳转一个对话框
    private void jumpDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("您确定提交吗？");
        dialog.setCancelable(true);
        dialog.setPositiveButton("是的", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    // 开始提交数据
                    submit();
                }
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });
        dialog.show();
    }

    // 开始提交数据
    private void submit(){
        // 用户能否正常操作
        if(radioButton26.isChecked()){
            string26 = "能";
        } else {
            string26 = "否";
        }
        data = new DeviceInspectionData(
                GKMC.getText().toString().trim(),
                SBBH.getText().toString().trim(),
                SBXH.getText().toString().trim(),
                XJRY.getText().toString().trim(),
                XJRQ.getText().toString().trim(),
                SRDY1.getText().toString().trim(),
                SRDY2.getText().toString().trim(),
                HJQK.getText().toString().trim(),
                pumpSBXH.getText().toString().trim(),
                pumpSBZX.getText().toString().trim(),
                SDYL.getText().toString().trim(),
                SJYL.getText().toString().trim(),
                CYBH.getText().toString().trim(),
                JKFS.getText().toString().trim(),
                YSKJYL.getText().toString().trim(),
                WSTJBH.getText().toString().trim(),
                XLLBY.getText().toString().trim(),
                SBYX.getText().toString().trim(),
                DJDL1.getText().toString().trim(),
                DJDL2.getText().toString().trim(),
                BPQDL.getText().toString().trim(),
                RE11.getText().toString().trim(),
                RE21.getText().toString().trim(),
                RE31.getText().toString().trim(),
                RE41.getText().toString().trim(),
                RE12.getText().toString().trim(),
                RE22.getText().toString().trim(),
                RE32.getText().toString().trim(),
                RE42.getText().toString().trim(),
                radioButton1.getText().toString().trim(), radioButton2.getText().toString().trim(),
                radioButton3.getText().toString().trim(), radioButton4.getText().toString().trim(),
                radioButton5.getText().toString().trim(), radioButton6.getText().toString().trim(),
                radioButton7.getText().toString().trim(), radioButton8.getText().toString().trim(),
                radioButton9.getText().toString().trim(), radioButton10.getText().toString().trim(),
                radioButton11.getText().toString().trim(), radioButton12.getText().toString().trim(),
                radioButton13.getText().toString().trim(), radioButton14.getText().toString().trim(),
                radioButton15.getText().toString().trim(), radioButton16.getText().toString().trim(),
                radioButton17.getText().toString().trim(), radioButton18.getText().toString().trim(),
                radioButton19.getText().toString().trim(), radioButton20.getText().toString().trim(),
                radioButton21.getText().toString().trim(), radioButton22.getText().toString().trim(),
                radioButton23.getText().toString().trim(), radioButton24.getText().toString().trim(),
                BENG1.getText().toString().trim(), BENG2.getText().toString().trim(), BENG3.getText().toString().trim(),
                BENG4.getText().toString().trim(), KZGBY.getText().toString().trim(), SBGDBY.getText().toString().trim(),
                ZJYXJL.getText().toString().trim(), radioButton25.getText().toString().trim(), string26, YHDZ.getText().toString().trim(),
                YHDH.getText().toString().trim(), YHQZ.getText().toString().trim());

        // 无网存储
        if (!NetWorkUtil.isNetworkConnected(this)) {
            // 这两句是为了下面的m.setData()的存储
            Gson gson = new Gson();
            String d = gson.toJson(data, DeviceInspectionData.class);
            // 这个是提报无网情况下提报数据的第一个接口的存储
            InspectionOffineItem m = new InspectionOffineItem();
            // 对象赋值
            m.setGuid(guid);
            m.setData(d);
            m.setUserId(userId);
            m.setWOID(equipInsBean.getWOID());
            m.setEquipmentID(equipInsBean.getEquipmentID());
            m.setEquipmentNo(SBBH.getText().toString().trim());
            m.setWorker(username);
            m.setGISLocation(currentPosition.toString());
            m.setEID("20");
            // 通过Gson得到m对象的json字符串，然后添加进去result集合，这样保存进sp文件就有[]
            // 括号了，没有[]在PendingTaskActivity里面没法转成集合
            String s = gson.toJson(m, InspectionOffineItem.class);
            result.add(s);
            editor.putString("map1", result.toString()).apply();
            // 这个是提报无网情况下提报数据的第二个接口的存储
            submitData2();
            InspectionOffineStateItem ins = new InspectionOffineStateItem();
            ins.setWorkorder(jsonObject1.toString());
            ins.setDetail(jsonArray.toString());
            ins.setGuid(guid);
            // 通过Gson得到ins对象的json字符串，然后添加进去result集合，这样保存进sp文件就有[]
            // 括号了，没有[]在PendingTaskActivity里面没法转成集合
            String s2 = gson.toJson(ins, InspectionOffineStateItem.class);
            list.add(s2);
            // 这个是提报无网情况下改变工单状态的存储
            editor.putString("map2", list.toString()).apply();
            toast("有网时自动提交");
            intent.setClass(DeviceInspectionActivity.this, EquipmentInspectionActivity.class);
            startActivity(intent);
            finish();
        } else {
            // 有网络的话
            submitData();// 提报数据
            getUpdateWorkOrder();// 改变工单状态
        }
    }

    // 提报数据
    private void submitData() {
        Gson gson = new Gson();
        String DIData = gson.toJson(data, DeviceInspectionData.class);
        loadding.show("上传数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/AddDeviceCheckInfo");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/AddDeviceCheckInfo");
        }
        params.addBodyParameter("guid", guid);
        params.addBodyParameter("data", DIData);
        params.addBodyParameter("userId", userId);
        params.addBodyParameter("WOID", equipInsBean.getWOID());
        params.addBodyParameter("EquipmentID", equipInsBean.getEquipmentID());
        params.addBodyParameter("EquipmentNo", SBBH.getText().toString().trim());
        params.addBodyParameter("Worker", username);
        params.addBodyParameter("GISLocation", currentPosition.toString());
        params.addBodyParameter("EID", "20");
        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
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
                Log.e("jsonXunJian ", arg0);
                CommonResponseBean commonResponseBean = new CommonResponseBean().getcommenHit(arg0);
                if (commonResponseBean.getCode() == 0) {
                    Intent intent = new Intent(DeviceInspectionActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                toast(commonResponseBean.getMessage());
            }
        });
    }

    // 改变工单状态
    private void submitData2(){
        try{
            Date date2 = new Date();
            endDate = format1.format(date2);// 结束时间（年月日时分秒）
            if((changeNumber + 1) < totalNumber){
                // workorder 字段    这个不用jsonArray
                jsonObject1.put("WOID", equipInsBean.getWOID());
                jsonObject1.put("WOTitle", equipInsBean.getWOTitle());
                jsonObject1.put("WOContent", equipInsBean.getWOContent());
                jsonObject1.put("Voice", "无语音");
                jsonObject1.put("WOState", 0);
                jsonObject1.put("WOIssuedDate", endDate);  // 派发时间
                jsonObject1.put("WOIssuedUser", equipInsBean.getWOIssuedUser());  // 派发人
                // 这个字段是activity进入的时候就获取的年月日时分秒
                jsonObject1.put("WOReceiveDate", receiveDate);
                jsonObject1.put("WOReceiveUser", equipInsBean.getWOReceiveUser());
                jsonObject1.put("WOItemsNum", totalNumber);  // 巡检工单的总数量
                jsonObject1.put("WOPerformNum", changeNumber + 1);  // 巡检工单的子数量
                jsonObject1.put("WOBeginDate", receiveDate);
                // 这个字段是点击发送按钮获取的当前的年月日时分秒
                jsonObject1.put("WOEndDate", endDate);
                jsonObject1.put("WOCreateDate", endDate);
                // 这是转发的WOType要填2代表巡检任务
                jsonObject1.put("WOType", "2");
                jsonObject1.put("WOExpectedTime", equipInsBean.getWOExpectedTime());
                jsonObject1.put("FBID", "1");
                jsonObject1.put("WOFeedback", "");
                // detail  字段      这个用jsonArray
                jsonObject2.put("WOID", equipInsBean.getWOID());
                jsonObject2.put("Num", "1");
                jsonObject2.put("OrderContent", equipInsBean.getWOContent());
                jsonObject2.put("OrderState", 0);
                jsonObject2.put("UserID", userId);
                jsonObject2.put("UserName", username);
                jsonObject2.put("DateTime", currentDate);
                jsonArray.put(jsonObject2);
            } else if ((changeNumber + 1) == totalNumber){
                // workorder 字段    这个不用jsonArray
                jsonObject1.put("WOID", equipInsBean.getWOID());
                jsonObject1.put("WOTitle", equipInsBean.getWOTitle());
                jsonObject1.put("WOContent", equipInsBean.getWOContent());
                jsonObject1.put("Voice", "无语音");
                jsonObject1.put("WOState", 1);
                jsonObject1.put("WOIssuedDate", currentDate);  // 派发日期
                jsonObject1.put("WOIssuedUser", equipInsBean.getWOIssuedUser());  // 派发人
                // 这个字段是activity进入的时候就获取的年月日时分秒
                jsonObject1.put("WOReceiveDate", receiveDate);
                jsonObject1.put("WOReceiveUser", equipInsBean.getWOReceiveUser());
                jsonObject1.put("WOItemsNum", totalNumber);  // 巡检工单的总数量
                jsonObject1.put("WOPerformNum", changeNumber + 1);  // 巡检工单的子数量
                jsonObject1.put("WOBeginDate", receiveDate);
                // 这个字段是点击发送按钮获取的当前的年月日时分秒
                jsonObject1.put("WOEndDate", endDate);
                jsonObject1.put("WOCreateDate", endDate);
                // 这是转发的WOType要填2代表巡检任务
                jsonObject1.put("WOType", "2");
                jsonObject1.put("WOExpectedTime", equipInsBean.getWOExpectedTime());
                jsonObject1.put("FBID", "1");
                jsonObject1.put("WOFeedback", "");
                // detail  字段      这个用jsonArray
                jsonObject2.put("WOID", equipInsBean.getWOID());
                jsonObject2.put("Num", "1");
                jsonObject2.put("OrderContent", equipInsBean.getWOContent());
                jsonObject2.put("OrderState", 1);
                jsonObject2.put("UserID", userId);
                jsonObject2.put("UserName", username);
                jsonObject2.put("DateTime", currentDate);
                jsonArray.put(jsonObject2);
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    // 发送工作任务的3个接口之一  UpdateWorkOrder接口  更新工单状态
    private void getUpdateWorkOrder() {
        submitData2();
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/UpdateWorkOrder");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/UpdateWorkOrder");
        }
        params.addBodyParameter("workorder", jsonObject1.toString());
        params.addBodyParameter("detail", jsonArray.toString());
        params.addBodyParameter("guid", guid);
        x.http().post(params, new Callback.CommonCallback<String>() {

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
                        toast(commonResponseBean.getMessage());
                        intent.setClass(DeviceInspectionActivity.this, EquipmentInspectionActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (commonResponseBean.getCode() == 0) {
                        intent.setClass(DeviceInspectionActivity.this, LoginActivity.class);
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

    // 通过设备编号得到设备信息
    private void getData3() {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/GetDeviceInfoByEquipNo");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/GetDeviceInfoByEquipNo");
        }
        params.addBodyParameter("guid", guid);
        params.addBodyParameter("EquipmentNo", SBBH.getText().toString().trim());
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {}

            @Override
            public void onError(Throwable ex, boolean arg1) { }

            @Override
            public void onFinished() {
                if (loadding.isShow()) {
                    loadding.close();
                }
            }

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject jsonObject1 = new JSONObject(arg0);
                    if (jsonObject1.getString("Code").equals("0")) {
                        intent.setClass(DeviceInspectionActivity.this,
                                LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(jsonObject1.getString("Message"));
                    } else if (jsonObject1.getString("Code").equals("1")) {
                        // 如果有值就将用户名和设备类型赋值为接口里面的下列值
                        JSONObject jsonObject2 = new JSONObject(jsonObject1.getString("Data"));
                        GKMC.setText(jsonObject2.getString("DeviceName")); // 顾客名称
                        SBXH.setText(jsonObject2.getString("DeviceType")); // 设备型号
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    // 数据的非空判断
    private boolean isEmpty() {
        // 总共56条数据
        if (GKMC.getText().length() < 1) {
            toast("请填写顾客名称");
            return false;
        }
        if (SBBH.getText().length() < 1){
            toast("请填写设备编号");
            return false;
        }
        if (SBXH.getText().length() < 1){
            toast("请填写设备型号");
            return false;
        }
        if (XJRY.getText().length() < 1){
            toast("请填写巡检人员");
            return false;
        }
        if (XJRQ.getText().equals("请输入巡检日期")) {
            toast("请选择巡检日期");
            return false;
        }
        if (SRDY1.getText().length() < 1){
            toast("请填写输入电源1");
            return false;
        }
        if (SRDY2.getText().length() < 1){
            toast("请填写输入电源2");
            return false;
        }
        if (radioButton1.getText().length() < 1) {
            toast("请填写前级开关压线情况");
            return false;
        }
        if (radioButton2.getText().length() < 1) {
            toast("请填写电机线压线情况");
            return false;
        }
        if (HJQK.getText().length() < 1) {
            toast("请填写环境情况");
            return false;
        }
        if (JKFS.getText().length() < 1){
            toast("请填写监控方式");
            return false;
        }
        if (radioButton3.getText().length() < 1) {
            toast("请填写监控是否连接正常");
            return false;
        }
        if (radioButton7.getText().length() < 1) {
            toast("请填写接触器是否灵活复位");
            return false;
        }
        if (radioButton8.getText().length() < 1) {
            toast("请填写控制柜内螺丝松动情况");
            return false;
        }
        if (radioButton9.getText().length() < 1) {
            toast("请填写微机控制系统接线是否良好");
            return false;
        }
        if (radioButton4.getText().length() < 1) {
            toast("请填写主线路触点电压情况");
            return false;
        }
        if (pumpSBXH.getText().length() < 1) {
            toast("请填写水泵型号");
            return false;
        }
        if (SBYX.getText().length() < 1) {
            toast("请填写水泵运行情况");
            return false;
        }
        if (DJDL1.getText().length() < 1) {
            toast("请填写电机额定电流1");
            return false;
        }
        if (DJDL2.getText().length() < 1) {
            toast("请填写电机额定电流2");
            return false;
        }
        if (RE11.getText().length() < 1 && RE21.getText().length() < 1 &&
                RE31.getText().length() < 1 && RE41.getText().length() < 1){
            toast("请填写热继电器动作值1");
            return false;
        }
        if (RE12.getText().length() < 1 && RE22.getText().length() < 1 &&
                RE32.getText().length() < 1 && RE42.getText().length() < 1) {
            toast("请填写热继电器动作值2");
            return false;
        }
        if (BPQDL.getText().length() < 1) {
            toast("请填写变频器动作电流值)");
            return false;
        }
        if (radioButton5.getText().length() < 1) {
            toast("请填写水泵填料密封是否漏水在规定范围内");
            return false;
        }
        if (radioButton6.getText().length() < 1) {
            toast("请填写水泵机械密封是否有漏水现象");
            return false;
        }
        if (SDYL.getText().length() < 1) {
            toast("请填写设定压力");
            return false;
        }
        if (SJYL.getText().length() < 1) {
            toast("请填写实际压力");
            return false;
        }
        if (CYBH.getText().length() < 1) {
            toast("请填写超压保护");
            return false;
        }
        if (pumpSBZX.getText().length() < 1) {
            toast("请填写水泵手动自动转向");
            return false;
        }
        if (radioButton11.getText().length() < 1) {
            toast("请填写自动能否正常启动");
            return false;
        }
        if (YSKJYL.getText().length() < 1){
            toast("请填写有水开机压力值");
            return false;
        }
        if (WSTJBH.getText().length() < 1) {
            toast("请填写无水停机保护值");
            return false;
        }
        if (XLLBY.getText().length() < 1) {
            toast("请填写小流量保压值");
            return false;
        }
        if (radioButton10.getText().length() < 1) {
            toast("请填写各水泵手动是否正常工作");
            return false;
        }
        if (radioButton12.getText().length() < 1) {
            toast("请填写开停机是否正常");
            return false;
        }
        if (radioButton14.getText().length() < 1) {
            toast("请填写自动交换是否正常");
            return false;
        }
        if (radioButton16.getText().length() < 1) {
            toast("请填写真空补偿器各指示灯是否正常");
            return false;
        }
        if (radioButton18.getText().length() < 1) {
            toast("请填写设备是否产生负压");
            return false;
        }
        if (radioButton13.getText().length() < 1) {
            toast("请填写止回阀是否正常");
            return false;
        }
        if (radioButton15.getText().length() < 1) {
            toast("请填写液位计工作是否正常");
            return false;
        }
        if (radioButton17.getText().length() < 1) {
            toast("请填写变频器工作是否正常（散热片是否清理）");
            return false;
        }
        if (radioButton19.getText().length() < 1) {
            toast("请填写过滤器是否进行清洗（半年/次）");
            return false;
        }
        if (radioButton20.getText().length() < 1) {
            toast("请填写消防水池是否有水");
            return false;
        }
        if (radioButton21.getText().length() < 1) {
            toast("请填写消防控制中心所处状态");
            return false;
        }
        if (radioButton22.getText().length() < 1) {
            toast("请填写无压巡检是否正常");
            return false;
        }
        if (radioButton23.getText().length() < 1) {
            toast("请填写管网所有螺丝是否全面紧固一遍");
            return false;
        }
        if (radioButton24.getText().length() < 1) {
            toast("请填写浮球阀是否检查保养正常");
            return false;
        }
        if (BENG1.getText().length() < 1){
            toast("请填写恒压各泵频率");
            return false;
        }
        if (KZGBY.getText().length() < 1){
            toast("请填写控制柜保养记录");
            return false;
        }
        if (SBGDBY.getText().length() < 1) {
            toast("请填写水泵及管道等保养记录");
            return false;
        }
        if (ZJYXJL.getText().length() < 1) {
            toast("请填写整机运行记录");
            return false;
        }
        if (radioButton25.getText().length() < 1) {
            toast("请填写巡检人员是否按以上试验功能");
            return false;
        }
        if (radioButton26.getText().length() < 1) {
            toast("请填写用户能否正常操作");
            return false;
        }
        if (YHDZ.getText().length() < 1) {
            toast("请填写用户地址");
            return false;
        }
        if (YHDH.getText().length() < 1) {
            toast("请填写用户电话");
            return false;
        }
        if (YHQZ.getText().length() < 1) {
            toast("请填写用户姓名");
            return false;
        }
        return true;
    }

    private void toast(String text){
        Toast.makeText(DeviceInspectionActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    // 手机返回按钮返回功能
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 设置处理优于所有其他NFC的处理
        if (nfcAdapter != null && nfcAdapter.isEnabled())
            nfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 恢复默认状态
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop(); // 停止定位
    }

    // nfc识别相关
    private void initNFC() {
        // 获取默认的NFC适配器
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            toast("设备不支持NFC！");
            return;
        }
        if (!nfcAdapter.isEnabled()) {
            toast("请在系统设置中先启用NFC功能！");
            return;
        }
        // 一旦截获NFC消息，就会通过PendingIntent调用窗口
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        // Setup an intent filter for all MIME based dispatches
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        mFilters = new IntentFilter[]{ndef,};
        // 读标签之前先确定标签类型。这里以大多数的NfcA为例
        mTechLists = new String[][]{new String[]{NfcA.class.getName()}};
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO 自动生成的方法存根
        super.onNewIntent(intent);
        String intentActionStr = intent.getAction();// 获取到本次启动的action
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intentActionStr)// NDEF类型
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intentActionStr)// 其他类型
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intentActionStr)) {// 未知类型
            // 在intent中读取Tag id
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG); // parcelable打包的
            byte[] bytesId = tag.getId();// 获取id数组
            info = ByteArrayChange.ByteArrayToHexString(bytesId) + "\n"; // hex十六进制
            String equNo = "";
            equNo = change(tag);
            // 转换为ASCll
            //toast("识别成功设备编号:" + equNo);
            // 如果设备编号为null的话就直接return，如果接着往下走就要闪退了
            if(equNo == null) return;
            if (equNo.length() > 0) {

                //TODO  拿到IC卡里携带的设备编号之后要执行的操作
                //toast("识别结果:" + equNo);
                SBBH.setText(equNo);
            } else {
                toast("识别失败请重新扫描卡片！");
            }
        }
    }

    // 将ncf识别出的Hex转换为字符串,一般ic卡里存储的是设备编号
    public String change(Tag tag) {
        MifareClassic mfc = MifareClassic.get(tag);
        Log.e("进入了change方法", "是的，进入了");
        boolean auth = false;
        // 读取TAG
        String ChangeInfo = "";
        String Ascll = "";
        // Enable I/O operations to the tag from this TagTechnology object.
        try {
            mfc.connect();
            // authenticate认证 sector扇形
            auth = mfc.authenticateSectorWithKeyA(1, MifareClassic.KEY_DEFAULT); // 非常重要---------------------------
            if (auth) {
                Log.e("change的auth验证成功", "开始读取模块信息");
                byte[] data = mfc.readBlock(4 * 1 + 1);//--------------
                ChangeInfo = ByteArrayChange.ByteArrayToHexString(data);
                //String temp = ToStringHex.decode(ChangeInfo);
                Ascll = ToStringHex.decode(ChangeInfo);
                return Ascll;
            }
        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        } finally {
            try {
                if(mfc == null) return "彬彬";
                mfc.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }
}
