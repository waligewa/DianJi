package com.example.motor.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
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
import com.example.motor.db.DeleteDeviceReportItem;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DoubleClickUtils;
import com.example.motor.util.FSScreen2;
import com.example.motor.util.Loadding;
import com.example.motor.util.SPUtils;
import com.example.motor.widget.SpinerPopWindow;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 这个activity是从设备报修列表点击进入的activity
 *
 */
public class EquipmentReportRepairActivity2 extends BaseActivity {

    @ViewInject(R.id.arrival_date)
    TextView arrivalDate;
    @ViewInject(R.id.run_date)
    TextView runDate;
    @ViewInject(R.id.belong_region)
    TextView belongRegin;  // 所属区域
    @ViewInject(R.id.username)
    EditText username;  // 设备名称
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
    @ViewInject(R.id.radiobutton2)
    RadioButton radioButton2;
    @ViewInject(R.id.satisfaction)
    TextView satisfaction;
    @ViewInject(R.id.tv_submit)
    TextView submit;
    /*@ViewInject(R.id.video_recording)
    TextView videoRecording;*/
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
    private List<CustemInfo> mCustomInfors = new ArrayList<>();
    private int userId;
    private DeleteDeviceReportItem deleteItem;

    // layout文件
    @Override
    public int setLayout() {
        return R.layout.activity_equipment_report_repair2;
    }

    // 在这里面设置TitleBar相关的
    @Override
    public void setTitle() {
        mBaseTitleBarView.setTitleText("设备报修信息");
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
        // 这个taskItemBean是从前面HomeFragment传过来的实体类
        deleteItem = (DeleteDeviceReportItem) getIntent().getSerializableExtra("delete_device_report_item");
        /*editor.putString("videopath", "");
        editor.putString("filename", "");
        editor.apply();*/
        address = prefs1.getString("add", "");
        guid = prefs1.getString("guid", "");
        userId = prefs1.getInt("UserID", 0);
        userName = prefs1.getString("username", "");
        videoPlaying.setVisibility(View.GONE);
        //videoRecording.setVisibility(View.VISIBLE);
        belongRegin.setText(deleteItem.getDevicePosition()); // 所属区域
        username.setText(deleteItem.getCompany()); // 设备名称
        equipmentNumber.setText(deleteItem.getEquipmentNo()); // 设备编号
        faultPhenomenon.setText(deleteItem.getSymptom()); // 故障现象
        analysisReason.setText(deleteItem.getReasons()); // 分析原因
        deviceName.setText(deleteItem.getDeviceName()); // 器件名称
        deviceModel.setText(deleteItem.getDeviceType()); // 器件型号
        deviceNumber.setText(deleteItem.getDeviceNo()); // 器件编号
        deviceManufacturer.setText(deleteItem.getManufactor()); // 器件生产商
        feedbackTelephone.setText(deleteItem.getCTelePhone()); // 反馈人电话
        userContact.setText(deleteItem.getContactUser()); // 用户联系人
        arrivalDate.setText(deleteItem.getArrivalDate().replace("T", " ")); // 到货日期
        runDate.setText(deleteItem.getFunctionDate().replace("T", " ")); // 运行日期
        if(deleteItem.getCharge().equals("false")){
            radioButton1.setChecked(true);
        } else if(deleteItem.getCharge().equals("true")){
            radioButton1.setChecked(false);
        }
        equipmentNumber.setEnabled(false);
        if(deleteItem.getIsrevice() != null && deleteItem.getIsrevice().equals("已派工")){
            username.setEnabled(false);
            arrivalDate.setEnabled(false);
            runDate.setEnabled(false);
            faultPhenomenon.setEnabled(false);
            analysisReason.setEnabled(false);
            deviceName.setEnabled(false);
            deviceModel.setEnabled(false);
            deviceNumber.setEnabled(false);
            deviceManufacturer.setEnabled(false);
            feedbackTelephone.setEnabled(false);
            userContact.setEnabled(false);
            radioButton1.setEnabled(false);
            radioButton2.setEnabled(false);
            submit.setVisibility(View.GONE);
        }
        // EditText的获取焦点状态从而实现一些操作
        equipmentNumber.setOnFocusChangeListener(new View.
                OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // 设备编号不为空字符串的话才进行操作
                    if(!TextUtils.isEmpty(equipmentNumber.getText())){
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

                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setMessage("您确定提交吗？");
                    dialog.setCancelable(true);
                    dialog.setPositiveButton("是的", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            loadding.show("上传数据中...");
                            if (isEmpty() && !DoubleClickUtils.isFastDoubleClick()) {
                                // 下面2条是对于数据的加载和视频的上传操作
                                modifyData();
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
            /*case R.id.video_recording:
                intent.setClass(EquipmentReportRepairActivity2.this,
                        CameraCaptureActivity.class);
                startActivity(intent);
                break;*/
            case R.id.satisfaction:
                showSpinWindowArea();
                break;
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
        mSpinerPopWindow.setWidth(satisfaction.getWidth()); // 设置下拉框的宽度
        mSpinerPopWindow.showAsDropDown(satisfaction); // 设置下拉框在哪一个控件的下面
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

    // 设备报修子项的修改接口
    private void modifyData() {
        loadding.show("上传数据中...");
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/UpdateFaultReport");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/UpdateFaultReport");
        }
        params.addBodyParameter("id", deleteItem.getID());
        params.addBodyParameter("EquipmentNo", deleteItem.getEquipmentNo()); // 设备编号
        params.addBodyParameter("EquipmentType", ""); // 设备类型
        params.addBodyParameter("CName", deleteItem.getCName()); // 设备名称
        params.addBodyParameter("CTelePhone", feedbackTelephone.getText().toString().trim()); // 反馈人姓名
        params.addBodyParameter("Company", username.getText().toString().trim()); // 设备名称
        params.addBodyParameter("Symptom", faultPhenomenon.getText().toString().trim()); // 故障现象
        params.addBodyParameter("DevicePosition", deleteItem.getDevicePosition()); // 设备地址
        params.addBodyParameter("video", deleteItem.getVideo()); // 视频名称
        params.addBodyParameter("UserName", deleteItem.getUserName());
        params.addBodyParameter("ArrivalDate", arrivalDate.getText().toString());
        params.addBodyParameter("FunctionDate", runDate.getText().toString());
        params.addBodyParameter("Reasons", analysisReason.getText().toString().trim()); // 分析原因
        params.addBodyParameter("DeviceName", deviceName.getText().toString().trim());// 器件名称
        params.addBodyParameter("DeviceType", deviceModel.getText().toString().trim());// 器件类型
        params.addBodyParameter("DeviceNo", deviceNumber.getText().toString().trim());// 器件编号
        params.addBodyParameter("Manufactor", deviceManufacturer.getText().toString().trim());// 器件生产厂商
        params.addBodyParameter("FeedbackPerson", deleteItem.getFeedbackPerson());// 反馈人
        params.addBodyParameter("FBDate", simpleDateFormat.format(date));// 反馈日期
        params.addBodyParameter("ContactUser", userContact.getText().toString().trim());// 反馈人电话
        params.addBodyParameter("Charge", String.valueOf(boolean1));// 是否收费
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
                    Intent intent = new Intent(EquipmentReportRepairActivity2.this,
                            LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                toast(commonResponseBean.getMessage());
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
                        intent.setClass(EquipmentReportRepairActivity2.this,
                                LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(jsonObject1.getString("Message"));
                    } else if (jsonObject1.getString("Code").equals("1")) {
                        // 如果有值就将用户名和设备类型赋值为接口里面的下列值
                        JSONObject jsonObject2 = new JSONObject(jsonObject1.getString("Data"));
                        username.setText(jsonObject2.getString("DeviceName"));
                        equipmentId = jsonObject2.getString("EquipmentID");
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    // 发送设备报修的3个接口之一  UploadVoice接口      上传文件
    /*private void upload(){
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
                toast(commonResponseBean.getMessage());
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
    }*/
}
