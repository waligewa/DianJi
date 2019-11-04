package com.example.motor.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.example.motor.adapter.AbstractSpinerAdapter;
import com.example.motor.adapter.CustemSpinerAdapter;
import com.example.motor.base.BaseActivity;
import com.example.motor.base.CustemInfo;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.CommonResponseBean;
import com.example.motor.service.LocationServer;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DoubleClickUtils;
import com.example.motor.util.FSScreen2;
import com.example.motor.util.Loadding;
import com.example.motor.util.MyAlertDialog;
import com.example.motor.util.SPUtils;
import com.example.motor.widget.SpinerPopWindow;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 设备报修功能块（服务界面过来的）。
 * 设备报修上传给后台，后台操作一下就可以将它生成一个工单，然后在工单列表里面就能收到这个工单子项，然后我
 * 可以看到我刚才上传的视频
 *
 */
public class EquipmentReportRepairActivity extends BaseActivity {

    @ViewInject(R.id.arrival_date)
    TextView arrivalDate;
    @ViewInject(R.id.run_date)
    TextView runDate;
    @ViewInject(R.id.belong_region)
    TextView belongRegin;  // 所属区域
    @ViewInject(R.id.username)
    EditText username;  // 用户名称
    @ViewInject(R.id.equipment_number)
    EditText equipmentNumber;  // 设备编号
    @ViewInject(R.id.equipment_model)
    EditText equipmentModel;  // 设备型号
    @ViewInject(R.id.fault_phenomenon)
    EditText faultPhenomenon;  // 故障现象
    @ViewInject(R.id.resolve_method)
    EditText resolveMethod;  // 解决方法
    @ViewInject(R.id.repair_result)
    EditText repairResult;  // 维修结果
    @ViewInject(R.id.analysis_reason)
    EditText analysisReason;  // 分析原因
    @ViewInject(R.id.device_name)
    EditText deviceName;  // 器件名称
    @ViewInject(R.id.device_model)
    EditText deviceModel;  // 器件型号
    @ViewInject(R.id.device_number)
    EditText deviceNumber;  // 器件编号
    @ViewInject(R.id.device_manufacturer)
    EditText deviceManufacturer;  // 器件生产商
    @ViewInject(R.id.feedback_telephone)
    EditText feedbackTelephone;  // 反馈人电话
    @ViewInject(R.id.user_contact)
    EditText userContact;  // 用户联系人
    @ViewInject(R.id.belong_region_manager)
    EditText belongReginManager;  // 所属区域经理
    @ViewInject(R.id.radiobutton1)
    RadioButton radioButton1;
    @ViewInject(R.id.satisfaction)
    TextView satisfaction;
    @ViewInject(R.id.radiobutton3)
    RadioButton radioButton3;
    @ViewInject(R.id.video_recording)
    TextView videoRecording;
    @ViewInject(R.id.video_playing)
    TextView videoPlaying;
    private Intent intent;
    private Calendar calendar;  // 用来装日期的
    private boolean boolean1, boolean3;
    private String guid, address, videoPath = "", fileName = "", equipmentId = "", userName;
    private SharedPreferences prefs1;
    private SharedPreferences.Editor editor;
    private Loadding loadding;
    private SpinerPopWindow mSpinerPopWindow;
    private CustemSpinerAdapter mAdapter;
    private List<CustemInfo> mCustomInfors = new ArrayList<CustemInfo>();
    private int userId;
    private LocationClient mLocationClient;
    private AlertDialog alertDialog;

    // layout文件
    @Override
    public int setLayout() {
        // LocationClient构建方法接收Context参数，getApplicationContext获取一个全局Context参数
        mLocationClient = new LocationClient(getApplicationContext());
        // 注册定位监听器，获取到位置信息时会回调
        mLocationClient.registerLocationListener(new MyLocationListener());
        return R.layout.activity_equipment_report_repair;
    }

    // 在这里面设置TitleBar相关的
    @Override
    public void setTitle() {
        mBaseTitleBarView.setTitleText("添加设备报修");
        mBaseTitleBarView.setLeftDrawable(-1);
    }

    // 初始化控件，设置监听
    @Override
    public void initWidget() {
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);
        requestLocation();
        init();
    }

    // View的click事件
    @Override
    public void widgetClick(View v) {}

    // 返回键事件
    @Override
    public void onTitleLeftPressed() {
        setResult(RESULT_OK, intent);// 返回的时候给一个RESULT_OK
        onBackPressed();
    }

    private void init() {
        FSScreen2.init(this);
        loadding = new Loadding(this);
        intent = new Intent();
        prefs1 = getSharedPreferences("UserInfo", 0);
        // 下列四行代码的意思是将这两个进行清空，防止存储了之后，下次人忘了录制了，然后还是能提报上去上次
        // 的视频
        editor = prefs1.edit();
        editor.putString("videopath", "");
        editor.putString("filename", "");
        editor.apply();
        address = prefs1.getString("add", "");
        guid = prefs1.getString("guid", "");
        userId = prefs1.getInt("UserID", 0);
        userName = prefs1.getString("userfullname", "");
        videoPlaying.setVisibility(View.GONE);
        videoRecording.setVisibility(View.VISIBLE);
        belongRegin.setText("");
        username.setText("");
        equipmentNumber.setText("");
        equipmentModel.setText("");
        faultPhenomenon.setText("");
        resolveMethod.setText("");
        repairResult.setText("");
        analysisReason.setText("");
        deviceName.setText("");
        deviceModel.setText("");
        deviceNumber.setText("");
        deviceManufacturer.setText("");
        feedbackTelephone.setText("");
        userContact.setText("");
        belongReginManager.setText("");
        radioButton1.setChecked(true);
        radioButton3.setChecked(true);
        // EditText的获取焦点状态从而实现一些操作
        equipmentNumber.setOnFocusChangeListener(new android.view.View.
                OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                } else {
                    // 设备编号不为空字符串的话才进行操作
                    if(!TextUtils.isEmpty(equipmentNumber.getText())){
                        //toast("成功了");
                        // 通过设备编号得到设备信息
                        getData3();
                    }
                }
            }
        });
    }

    @Event(value = { R.id.arrival_date, R.id.run_date, R.id.tv_submit, R.id.video_recording,
            R.id.satisfaction }, type = View.OnClickListener.class)
    private void btnClick(View v){
        switch(v.getId()){
            case R.id.arrival_date:
                showDate1();
                break;
            case R.id.run_date:
                showDate2();
                break;
            case R.id.tv_submit:
                fileName = prefs1.getString("filename", "");
                videoPath = prefs1.getString("videopath", "");
                if(!DoubleClickUtils.isFastDoubleClick() && isEmpty()){
                    // 是否收费
                    if(radioButton1.isChecked()){
                        boolean1 = false;
                    } else {
                        boolean1 = true;
                    }
                    //Log.e("EquipmentReportRepairActivity", boolean1 + "");
                    // 区域经理是否知道此消息
                    if(radioButton3.isChecked()){
                        boolean3 = false;
                    } else {
                        boolean3 = true;
                    }

                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    //dialog.setTitle("请选择");
                    dialog.setMessage("您确定提交吗？");
                    dialog.setCancelable(true);
                    dialog.setPositiveButton("是的", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            loadding.show("上传数据中...");
                            if (isEmpty() && !DoubleClickUtils.isFastDoubleClick()) {
                                // 下面2条是对于数据的加载和视频的上传操作
                                getData1();// 成功的话提示“上报成功”
                                if(!"".equals(videoPath)){
                                    upload();// 成功的话提示“添加成功”
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
                break;
            case R.id.video_recording:
                intent.setClass(EquipmentReportRepairActivity.this, CameraCaptureActivity.class);
                startActivity(intent);
                break;
            case R.id.satisfaction:
                showSpinWindowArea();
                break;
        }
    }

    // 在initWidget()方法里面有这个方法的调用
    private void requestLocation(){
        initLocation();
        mLocationClient.start();// 开始定位，定位的结果会回调到监听器中
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);//设置更新间隔，5s更新一下当前位置  scan扫描span跨度
        // 表示要获取当前位置的详细信息，获取地址信息一定要用网络，所以即使时Device_Sensors模式也会自动开启网络定位功能
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    // 这个经纬度位置5秒刷新一次
    private class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append(bdLocation.getProvince()).append("");
            currentPosition.append(bdLocation.getCity()).append("");
            currentPosition.append(bdLocation.getDistrict()).append("");
            currentPosition.append(bdLocation.getStreet()).append("");
            belongRegin.setText(currentPosition);
            //toast("网络定位" + currentPosition.toString());
        }
    }

    private void showSpinWindowArea() {
        mCustomInfors.clear();
        getWorkState();
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
        DisplayMetrics dm = new DisplayMetrics();   // metrics  韵律学
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        //mSpinerPopWindow.setWidth(width / 2);
        mSpinerPopWindow.setWidth(satisfaction.getWidth());// 设置下拉框的宽度
        mSpinerPopWindow.showAsDropDown(satisfaction);// 设置下拉框在哪一个控件的下面
        mSpinerPopWindow.removeFooterView();
    }

    // 加载的区域
    private void getWorkState() {
        try {
            CustemInfo info1 = new CustemInfo();
            info1.setId("1");
            info1.setDeviceName("满意");
            CustemInfo info2 = new CustemInfo();
            info2.setId("2");
            info2.setDeviceName("一般");
            CustemInfo info3 = new CustemInfo();
            info3.setId("3");
            info3.setDeviceName("不满意");
            mCustomInfors.add(info1);
            mCustomInfors.add(info2);
            mCustomInfors.add(info3);
            mAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 设置下拉框内容
    private void setHero(int pos) {
        CustemInfo value = mCustomInfors.get(pos);
        satisfaction.setText(value.getDeviceName());
        mSpinerPopWindow.dismiss();
    }

    private void showDate1() {
        calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, AlertDialog.BUTTON_POSITIVE,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        monthOfYear++;
                        arrivalDate.setText(year + "年" + monthOfYear + "月" + dayOfMonth + "日");
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();

        DatePickerDialog dialog2 = new DatePickerDialog(this, AlertDialog.BUTTON_POSITIVE,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        monthOfYear++;
                        arrivalDate.setText(year + "年" + monthOfYear + "月" + dayOfMonth + "日");
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    private void showDate2() {
        calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, AlertDialog.BUTTON_POSITIVE,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        monthOfYear++;
                        runDate.setText(year + "年" + monthOfYear + "月" + dayOfMonth + "日");
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    // 设备报修的上报接口
    private void getData1() {
        loadding.show("上传数据中...");
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/FaultReport");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/FaultReport");
        }
        params.addBodyParameter("guid", guid);
        params.addBodyParameter("UserID", String.valueOf(userId));
        // 这个字段一直出错，如果EquipmentID为“”那这个接口就上传不了，如何导致EquipmentID为空，设备id通过
        // getData3()这个接口得到，如果通过这个接口得不到equpimentId，那这个接口会走onError方法
        params.addBodyParameter("EquipmentID", equipmentId);
        params.addBodyParameter("EquipmentNo", equipmentNumber.getText().toString().trim());
        // 登录的用户名
        params.addBodyParameter("CName", userName);
        params.addBodyParameter("CTelePhone", feedbackTelephone.getText().toString().trim());
        params.addBodyParameter("Company", username.getText().toString().trim());
        params.addBodyParameter("Symptom", faultPhenomenon.getText().toString().trim());
        params.addBodyParameter("DevicePosition", belongRegin.getText().toString().trim());
        params.addBodyParameter("Voice", "");
        params.addBodyParameter("OpenID", "12345");
        // 如果文件名称有值的话就附加值，如果没有值的话就赋值为空字符串
        if("".equals(fileName)){
            params.addBodyParameter("video","");
        } else {
            params.addBodyParameter("video", fileName + ".mp4");
        }
        // 登录的用户名
        params.addBodyParameter("UserName", userName);
        params.addBodyParameter("EquipmentType", "");
        //params.addBodyParameter("ArrivalDate", arrivalDate.getText().toString());// 到货日期
        //params.addBodyParameter("FunctionDate", runDate.getText().toString());// 运行日期
        params.addBodyParameter("ArrivalDate", simpleDateFormat.format(date));// 到货日期
        params.addBodyParameter("FunctionDate", simpleDateFormat.format(date));// 运行日期
        params.addBodyParameter("Resolvent", resolveMethod.getText().toString().trim());// 解决方法
        params.addBodyParameter("Result", repairResult.getText().toString().trim());// 维修结果
        params.addBodyParameter("Reasons", analysisReason.getText().toString().trim());// 分析原因
        params.addBodyParameter("DeviceName", deviceName.getText().toString().trim());// 器件名称
        params.addBodyParameter("DeviceType", deviceModel.getText().toString().trim());// 器件型号
        params.addBodyParameter("DeviceNo", deviceNumber.getText().toString().trim());// 器件编号
        params.addBodyParameter("Manufactor", deviceManufacturer.getText().toString().trim());// 器件生产厂商
        // 登录的用户名
        params.addBodyParameter("FeedbackPerson", userName);//  反馈人
        params.addBodyParameter("FBDate", simpleDateFormat.format(date));// 反馈日期
        params.addBodyParameter("ContactUser", userContact.getText().toString().trim());// 用户了联系人
        params.addBodyParameter("Charge", String.valueOf(boolean1));// 是否收费
        params.addBodyParameter("Satisfaction", satisfaction.getText().toString());// 用户或者业务员满意度
        params.addBodyParameter("Manager", belongReginManager.getText().toString().trim());// 所属区域经理
        params.addBodyParameter("IsKnow", String.valueOf(boolean3));// 区域经理是否知道此消息
        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {}

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
                CommonResponseBean commonResponseBean = new CommonResponseBean().getcommenHit(arg0);
                if (commonResponseBean.getCode() == 0) {
                    Intent intent = new Intent(EquipmentReportRepairActivity.this,
                            LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                toast(commonResponseBean.getMessage());
                arrivalDate.setText("请选择到货日期");
                runDate.setText("请选择运行日期");
                if (loadding.isShow()) {
                    loadding.close();
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
        params.addBodyParameter("EquipmentNo", equipmentNumber.getText().toString().trim());
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
                        intent.setClass(EquipmentReportRepairActivity.this,
                                LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(jsonObject1.getString("Message"));
                    } else if (jsonObject1.getString("Code").equals("1")) {
                        // 如果有值就将用户名和设备类型赋值为接口里面的下列值
                        JSONObject jsonObject2 = new JSONObject(jsonObject1.getString("Data"));
                        username.setText(jsonObject2.getString("DeviceName"));
                        equipmentModel.setText(jsonObject2.getString("DeviceType"));
                        equipmentId = jsonObject2.getString("EquipmentID");
                    } else {
                        username.setText("");
                        equipmentModel.setText("");
                        new MyAlertDialog(EquipmentReportRepairActivity.this)
                                .builder()
                                .setMsg("请点击“确定”按钮后完善设备信息")
                                .setCancelable(false)
                                .setPositiveButton("确定", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //点击确定之后再调出弹出框
                                        View submitView = LayoutInflater.from(getBaseContext())
                                                .inflate(R.layout.check_admin_layout3, null);
                                        final EditText equipmentName =
                                                (EditText) submitView.findViewById(R.id.equipment_name);
                                        final EditText equipmentType =
                                                (EditText) submitView.findViewById(R.id.equipment_type);
                                        final EditText equipmentPosition =
                                                (EditText) submitView.findViewById(R.id.equipment_position);
                                        Button btSubmit = (Button) submitView.findViewById(R.id.bt_submit);
                                        Button btCancel = (Button) submitView.findViewById(R.id.bt_cancel);
                                        btSubmit.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (TextUtils.isEmpty(equipmentName.getText())) {
                                                    toast("设备名称不可为空，请重新填写！");
                                                } else if (TextUtils.isEmpty(equipmentType.getText())) {
                                                    toast("设备类型不可为空，请重新填写！");
                                                } else if (TextUtils.isEmpty(equipmentPosition.getText())){
                                                    toast("设备位置不可为空，请重新填写！");
                                                } else {
                                                    // 完善设备总表信息
                                                    getData4(equipmentName.getText().toString().trim(),
                                                            equipmentType.getText().toString().trim(),
                                                            equipmentPosition.getText().toString().trim());
                                                }
                                            }
                                        });
                                        btCancel.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (alertDialog.isShowing()) {
                                                    alertDialog.dismiss();
                                                }
                                            }
                                        });
                                        alertDialog = new AlertDialog
                                                .Builder(EquipmentReportRepairActivity.this)
                                                .create();
                                        alertDialog.setCancelable(false);
                                        // 不加这句代码EditText无法填入东西
                                        alertDialog.setView(new
                                                EditText(EquipmentReportRepairActivity.this));
                                        alertDialog.show();
                                        // 这句代码的意思是将布局填入AlertDialog
                                        alertDialog.getWindow().setContentView(submitView);
                                    }
                                })
                                .setNegativeButton("取消", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {}
                                })
                                .show();
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    // 通过设备编号得到设备信息
    private void getInformationFromDeviceNumber() {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/GetDeviceInfoByEquipNo");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/GetDeviceInfoByEquipNo");
        }
        params.addBodyParameter("guid", "");
        params.addBodyParameter("EquipmentNo", equipmentNumber.getText().toString().trim());
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
                        intent.setClass(EquipmentReportRepairActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(jsonObject1.getString("Message"));
                    } else if (jsonObject1.getString("Code").equals("1")) {
                        // 如果有值就将用户名和设备类型赋值为接口里面的下列值
                        JSONObject jsonObject2 = new JSONObject(jsonObject1.getString("Data"));
                        equipmentModel.setText(jsonObject2.getString("DeviceType"));
                        equipmentId = jsonObject2.getString("EquipmentID");
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    // 完善设备总表信息
    private void getData4(final String string1, final String string2, String string3) {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/AddDeviceInfo");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/AddDeviceInfo");
        }
        params.addBodyParameter("guid", guid);
        params.addBodyParameter("EquipmentNo", equipmentNumber.getText().toString().trim());
        params.addBodyParameter("DeviceName", string1);
        params.addBodyParameter("DeviceType", string2);
        params.addBodyParameter("DevicePosition", string3);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {}

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast("内部服务器异常");
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
                    JSONObject jsonObject1 = new JSONObject(arg0);
                    if (jsonObject1.getString("Code").equals("0")) {
                        intent.setClass(EquipmentReportRepairActivity.this,
                                LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(jsonObject1.getString("Message"));
                    } else if (jsonObject1.getString("Code").equals("1")) {
                        toast(jsonObject1.getString("Message"));
                        username.setText(string1);
                        alertDialog.dismiss();
                        // 通过设备编号得到设备信息。如果完善信息成功之后，对equipmentId进行赋值。如果不进行这步，完善信息之后
                        // equipmentId就成了空字符串，就会造成设备报修提交时的error
                        getInformationFromDeviceNumber();
                    } else {
                        toast(jsonObject1.getString("Message"));
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    // 发送设备报修的3个接口之一  UploadVoice接口      上传文件
    private void upload(){
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/UploadVoice");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/UploadVoice");
        }
        try {
            params.addHeader("Content-Type","video/mp4");
            params.setMultipart(true);
            params.addBodyParameter("file", new File(videoPath)); //  设置上传的文件路径
        }catch (Exception e){
            e.printStackTrace();
        }
        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                CommonResponseBean commonResponseBean = new CommonResponseBean().getcommenHit(result);
                //toast(commonResponseBean.getMessage());
                toast("视频添加成功");
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                toast("服务器异常");
            }

            @Override
            public void onCancelled(CancelledException cex) {}

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
        if (belongRegin.getText().length() < 1) {
            toast("请填写所属区域");
            return false;
        }
        if (username.getText().length() < 1){
            toast("请填写用户名称");
            return false;
        }
        if (equipmentNumber.getText().length() < 1){
            toast("请填写设备编号");
            return false;
        }
        if (equipmentModel.getText().length() < 1){
            toast("请填写设备型号");
            return false;
        }
        if (arrivalDate.getText().equals("请选择到货日期")) {
            toast("请选择到货日期");
            return false;
        }
        if (runDate.getText().equals("请选择运行日期")) {
            toast("请选择运行日期");
            return false;
        }
        if (faultPhenomenon.getText().length() < 1) {
            toast("请填写故障现象");
            return false;
        }
        if (analysisReason.getText().length() < 1) {
            toast("请填写分析原因");
            return false;
        }
        if (deviceName.getText().length() < 1) {
            toast("请填写器件名称");
            return false;
        }
        if (deviceModel.getText().length() < 1) {
            toast("请填写器件型号");
            return false;
        }
        if (deviceNumber.getText().length() < 1) {
            toast("请填写器件编号");
            return false;
        }
        if (deviceManufacturer.getText().length() < 1) {
            toast("请填写器件生产商");
            return false;
        }
        if (feedbackTelephone.getText().length() < 1){
            toast("请填写反馈人电话");
            return false;
        }
        if (userContact.getText().length() < 1) {
            toast("请填写用户联系人");
            return false;
        }
        return true;
    }

    private void toast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 停止百度鹰眼的服务（里面还有给王珂上传坐标的一个网络请求方法）
        stopService(new Intent(this, LocationServer.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();// 停止定位
        // 开启百度鹰眼的服务（里面还有给王珂上传坐标的一个网络请求方法）
        startService(new Intent(this, LocationServer.class));
    }

    // 手机返回按钮返回功能
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                setResult(RESULT_OK, intent);// 返回的时候给一个RESULT_OK
                finish();
                break;
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }
}
