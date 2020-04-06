package com.example.motor.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.adapter.AbstractSpinerAdapter;
import com.example.motor.adapter.CustemSpinerAdapter;
import com.example.motor.adapter.NumericWheelAdapter;
import com.example.motor.base.BaseActivity;
import com.example.motor.base.CustemInfo;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.CommonResponseBean;
import com.example.motor.listener.OnWheelChangedListener;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DoubleClickUtils;
import com.example.motor.util.Loadding;
import com.example.motor.util.SPUtils;
import com.example.motor.util.WheelView;
import com.example.motor.widget.SpinerPopWindow;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * 这是从服务界面点击进去的完善信息
 *
 */
public class PerfectInformationActivity extends BaseActivity {

    @ViewInject(R.id.equipment_number)
    EditText equipmentNumber;
    @ViewInject(R.id.equipment_id)
    EditText equipmentId;
    @ViewInject(R.id.equipment_type)
    EditText equipmentType;
    @ViewInject(R.id.device_name)
    EditText deviceName;
    @ViewInject(R.id.device_type)
    EditText deviceType;
    @ViewInject(R.id.pump_power)
    EditText pumpPower;
    @ViewInject(R.id.system_flow)
    EditText systemFlow;
    @ViewInject(R.id.system_lift)
    EditText systemLift;
    @ViewInject(R.id.import_dn)
    EditText importDn;
    @ViewInject(R.id.export_dn)
    EditText exportDn;
    @ViewInject(R.id.manufacture_date)
    TextView manufactureDate;
    @ViewInject(R.id.customer_name)
    EditText customerName;
    @ViewInject(R.id.creater)
    EditText creater;
    @ViewInject(R.id.customer_phone)
    EditText customerPhone;
    @ViewInject(R.id.tv_province)
    TextView province;
    @ViewInject(R.id.details_address)
    EditText detailPosition;
    private Intent intent;
    private Calendar calendar;  // 用来装日期的
    private String guid, address, lng, lat, equipmentNo;
    private SharedPreferences prefs, prefs2;
    private Dialog dialog;
    private int click = 0, userId;
    private List<CustemInfo> mCustomInfors = new ArrayList<>();
    private CustemSpinerAdapter mAdapter;
    private SpinerPopWindow mSpinerPopWindow;
    private String provinceId = "", cityId = "", districtId = "", provinceString = "",
            cityString = "", districtString = "", guidString;
    private Loadding loadding;
    private LocationClient mLocationClient;
    private List<String> permissionList = new ArrayList<>();

    // layout文件
    @Override
    public int setLayout() {
        mLocationClient = new LocationClient(this);
        mLocationClient.registerLocationListener(new MyLocationListener());
        return R.layout.activity_perfect_information_activity;
    }

    // 在这里面设置TitleBar相关的
    @Override
    public void setTitle() {
        mBaseTitleBarView.setTitleText("完善信息");
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
        loadding = new Loadding(this);
        intent = new Intent();
        prefs = getSharedPreferences("UserInfo", 0);  //  创建一个sp文件
        prefs2 = getSharedPreferences("device", Context.MODE_PRIVATE);
        guid = prefs.getString("guid", "");
        address = prefs.getString("add", "");
        guidString = prefs.getString("guid", "");
        userId = prefs.getInt("UserID", 0);
        equipmentNo = prefs2.getString("EquipmentNo", "");
        Log.e("PerfectInformation", equipmentNo);
        manufactureDate.setOnClickListener(new timePickThree());
        if(ContextCompat.checkSelfPermission(PerfectInformationActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(PerfectInformationActivity.this,
                android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(android.Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(PerfectInformationActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(PerfectInformationActivity.this, permissions, 1);
        } else {
            requestLocation();
        }
        // 根据设备编号获取设备信息
        getData2();
    }

    private void requestLocation(){
        mLocationClient.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length > 0){
                    for(int result : grantResults){
                        if(result != PackageManager.PERMISSION_GRANTED){
                            toast("必须同意所有权限才能使用本程序");
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }
            default:
        }
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation){
            lat = String.valueOf(bdLocation.getLatitude());
            lng = String.valueOf(bdLocation.getLongitude());
        }
    }

    @Event(value = { R.id.tv_submit, R.id.tv_province }, type = View.OnClickListener.class)
    private void btnClick(View v){
        switch(v.getId()){
            case R.id.tv_submit:
                if((!DoubleClickUtils.isFastDoubleClick()) && isEmpty()){
                    // 跳转一个对话框
                    jumpDialog();
                }
                break;
            case R.id.tv_province:
                if (!DoubleClickUtils.isFastDoubleClick()) {
                    click = 1;
                    // 每次点击province按钮就将这三个字符串清空一下，有利于后面对这三个字符串进行传值操作，
                    // 并且防止点击popWindow外面发生不想要的TextView赋值错误问题。
                    provinceString = "";
                    cityString = "";
                    districtString = "";
                    province.setText("请选择省");
                    showSpinWindowArea();
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
                    // 完善信息的提交网络接口
                    getData();
                }
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });
        dialog.show();
    }

    // 设置下拉框内容
    private void showSpinWindowArea() {
        mCustomInfors.clear();
        switch (click) {
            case 1: // 点击省
                getCtiy("0");
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
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        mSpinerPopWindow.setWidth(width / 2);
        mSpinerPopWindow.showAsDropDown(manufactureDate);
        //mSpinerPopWindow.showAtLocation(equipmentNumber, Gravity.NO_GRAVITY, dm.widthPixels/3, dm.heightPixels/3);
        mSpinerPopWindow.removeFooterView();
    }

    // 点击下拉框内容
    private void setHero(int pos) {

        CustemInfo value = mCustomInfors.get(pos);
        String id = value.getId();
        String nameString = value.getDeviceName();
        switch (click) {
            case 1:// 点击省
                // 将得到的nameString传值给regionString，将2赋值给click，然后调用getCity方法。
                provinceId = id;
                province.setText(nameString);
                provinceString = nameString;
                click = 2;
                // 点击区域人员之后就进入选择班组的方法里面
                getCtiy(provinceId);
                break;
            case 2:// 点击市
                cityId = id;
                province.setText(provinceString + " " + nameString);
                cityString = nameString;
                click = 3;
                getCtiy(cityId);
                break;
            case 3:  // 点击区
                districtId = id;
                province.setText(provinceString + " " + cityString + " " + nameString);
                districtString = nameString;
                mSpinerPopWindow.dismiss();
                break;
        }
    }

    // 0 选择省 其他传省id或者市id选择区域
    private void getCtiy(String i) {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadAreas");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/LoadAreas");
        }
        params.addBodyParameter("guid", guidString);
        params.addBodyParameter("AreaID", i);  // 区域id

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
                    Log.e("json ", arg0);
                    CommonResponseBean commonResponseBean = new CommonResponseBean().getcommenHit(arg0);
                    if (commonResponseBean.getCode() == 1) {
                        mCustomInfors.clear();
                        JSONArray array = new JSONArray(commonResponseBean.getData());
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            CustemInfo info = new CustemInfo();
                            info.setId(object.getString("ID"));
                            info.setDeviceName(object.getString("AreaName"));
                            mCustomInfors.add(info);
                        }
                        mAdapter.notifyDataSetChanged();
                    } else if (commonResponseBean.getCode() == 0) {
                        Intent intent = new Intent(PerfectInformationActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        mSpinerPopWindow.dismiss();
                        toast(commonResponseBean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 完善信息的提交网络接口
    private void getData() {
        loadding.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/AddRTUInfo");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/AddRTUInfo");
        }
        // 这样做是因为完善信息存在bug，如果存在完善信息，修改除用户地址之外的数据之后点击提交按钮时提交不上的。因为
        // 省市区id为空字符串的问题，所有在每次提交之前都进行一下判断。
        if(provinceId.equals("") || cityId.equals("") || districtId.equals("")){
            toast("请选择省市区");
            return;
        }
        params.addBodyParameter("guid", guidString);
        params.addBodyParameter("EquipmentID", equipmentId.getText().toString().trim());
        params.addBodyParameter("EquipmentNo", equipmentNumber.getText().toString().trim());
        params.addBodyParameter("DeviceName", deviceName.getText().toString().trim());
        params.addBodyParameter("Province", provinceId);
        params.addBodyParameter("City", cityId);
        params.addBodyParameter("District", districtId);
        params.addBodyParameter("DevicePosition", detailPosition.getText().toString().trim());
        params.addBodyParameter("DeviceType", deviceType.getText().toString().trim());
        params.addBodyParameter("CustomerName", customerName.getText().toString().trim());
        params.addBodyParameter("CustomerPhone", customerPhone.getText().toString().trim());
        params.addBodyParameter("Lng", lng);
        params.addBodyParameter("Lat", lat);
        params.addBodyParameter("Creater", creater.getText().toString().trim());
        params.addBodyParameter("power", pumpPower.getText().toString().trim());
        params.addBodyParameter("SystemFlow", systemFlow.getText().toString().trim());
        params.addBodyParameter("SystemLift", systemLift.getText().toString().trim());
        params.addBodyParameter("ImportDN", importDn.getText().toString().trim());
        params.addBodyParameter("ExportDN", exportDn.getText().toString().trim());
        params.addBodyParameter("ManufactureDate", manufactureDate.getText().toString().trim());
        params.addBodyParameter("EquipmentType", equipmentType.getText().toString());

        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                if (loadding.isShow()) {
                    loadding.close();
                }
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
                try {
                    Log.e("json ", arg0);
                    CommonResponseBean commonResponseBean = new CommonResponseBean().getcommenHit(arg0);
                    if (commonResponseBean.getCode() == 1) {
                        toast("信息完善成功");
                        finish();
                    } else if (commonResponseBean.getCode() == 0) {
                        Intent intent = new Intent(PerfectInformationActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        return;
                    } else {
                        toast(commonResponseBean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 根据设备编号获取设备信息
    private void getData2() {
        if(equipmentNo.equals("")) return;
        // 请求参数
        loadding.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadRtuData");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/LoadRtuData");
        }
        params.addBodyParameter("userId", String.valueOf(userId));
        params.addBodyParameter("equNo", equipmentNo);// equipmentNo    "0712024"
        params.addBodyParameter("pageSize", "100");
        params.addBodyParameter("pageIndex", "1");
        params.addBodyParameter("guid", guid);

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
                    Log.e("json ", arg0);
                    CommonResponseBean commonResponseBean = new CommonResponseBean().getcommenHit(arg0);
                    if (commonResponseBean.getCode() == 1) {
                        JSONArray jsonArray = new JSONArray(commonResponseBean.getData());
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        equipmentNumber.setText(jsonObject.getString("EquipmentNo"));
                        equipmentId.setText(jsonObject.getString("EquipmentID"));
                        equipmentType.setText(jsonObject.getString("EquipmentType"));
                        deviceName.setText(jsonObject.getString("DeviceName"));
                        deviceType.setText(jsonObject.getString("DeviceType"));
                        pumpPower.setText(jsonObject.getString("power").equals("null") ?
                                "" : jsonObject.getString("power"));
                        systemFlow.setText(jsonObject.getString("SystemFlow").equals("null") ?
                                "" : jsonObject.getString("SystemFlow"));
                        systemLift.setText(jsonObject.getString("SystemLift").equals("null") ?
                                "" : jsonObject.getString("SystemLift"));
                        importDn.setText(jsonObject.getString("ImportDN").equals("null") ?
                                "" : jsonObject.getString("ImportDN"));
                        exportDn.setText(jsonObject.getString("ExportDN").equals("null") ?
                                "" : jsonObject.getString("ExportDN"));
                        manufactureDate.setText(jsonObject.getString("ManufactureDate")
                                .replace("T", " ").equals("null") ? "请选择出厂日期" :
                                jsonObject.getString("ManufactureDate").replace("T", " "));
                        customerName.setText(jsonObject.getString("CustomerName").equals("null") ?
                                "" : jsonObject.getString("CustomerName"));
                        creater.setText(jsonObject.getString("Creater").equals("null") ?
                                "" : jsonObject.getString("Creater"));
                        customerPhone.setText(jsonObject.getString("CustomerPhone").equals("null") ?
                                "" : jsonObject.getString("CustomerPhone"));
                        if(!jsonObject.getString("DevicePosition").equals("null")){
                            detailPosition.setText(jsonObject.getString("DevicePosition"));
                        } else {
                            detailPosition.setText("");
                        }
                        province.setText(jsonObject.getString("Province") +
                                jsonObject.getString("City") +
                                jsonObject.getString("District"));
                        provinceId = TextUtils.isEmpty(jsonObject.getString("ProvinceID")) ?
                                "" : jsonObject.getString("ProvinceID");
                        cityId = TextUtils.isEmpty(jsonObject.getString("CityID")) ?
                                "" : jsonObject.getString("CityID");
                        districtId = TextUtils.isEmpty(jsonObject.getString("DistrictID")) ?
                                "" : jsonObject.getString("DistrictID");
                    } else if (commonResponseBean.getCode() == 0) {
                        Intent intent = new Intent(PerfectInformationActivity.this, LoginActivity.class);
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

    // 汇报时间设置
    private final class timePickThree implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            showDateTimePickerThree();
        }
    }

    // 第一个滚轮的选择
    private void showDateTimePickerThree() {

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);

        // 添加大小月月份并将其转换为list,方便之后的判断
        String[] months_big = { "1", "3", "5", "7", "8", "10", "12" };
        String[] months_little = { "4", "6", "9", "11" };

        final List<String> list_big = Arrays.asList(months_big);        //这个不能用add()方法
        final List<String> list_little = Arrays.asList(months_little);  //这个不能用add()方法

        dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 找到dialog的布局文件
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.time_layout, null);

        // 年
        final WheelView wv_year = (WheelView) view.findViewById(R.id.year);
        wv_year.setAdapter(new NumericWheelAdapter(1990, 2100)); //  设置"年"的显示数据
        wv_year.setCyclic(true); //  可循环滚动
        wv_year.setLabel("年");  //  添加文字
        wv_year.setCurrentItem(year - 1990); // 初始化时显示的数据

        // 月
        final WheelView wv_month = (WheelView) view.findViewById(R.id.month);
        wv_month.setAdapter(new NumericWheelAdapter(1, 12));
        wv_month.setCyclic(true); //  可循环滚动
        wv_month.setLabel("月");  //  添加文字
        wv_month.setCurrentItem(month); //  初始化时显示的数据

        // 日
        final WheelView wv_day = (WheelView) view.findViewById(R.id.day);
        wv_day.setCyclic(true); //  可循环滚动
        // 判断大小月及是否闰年,用来确定"日"的数据
        if (list_big.contains(String.valueOf(month + 1))) {
            wv_day.setAdapter(new NumericWheelAdapter(1, 31));
        } else if (list_little.contains(String.valueOf(month + 1))) {
            wv_day.setAdapter(new NumericWheelAdapter(1, 30));
        } else {
            // 闰年
            if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
                wv_day.setAdapter(new NumericWheelAdapter(1, 29));
            else
                wv_day.setAdapter(new NumericWheelAdapter(1, 28));
        }
        wv_day.setLabel("日");
        wv_day.setCurrentItem(day - 1);

        // 添加"年"监听
        OnWheelChangedListener wheelListener_year = new OnWheelChangedListener() {
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                int year_num = newValue + 1990;
                //  判断大小月及是否闰年,用来确定"日"的数据
                if (list_big.contains(String.valueOf(wv_month.getCurrentItem() + 1))) {
                    wv_day.setAdapter(new NumericWheelAdapter(1, 31));
                } else if (list_little.contains(String.valueOf(wv_month.getCurrentItem() + 1))) {
                    wv_day.setAdapter(new NumericWheelAdapter(1, 30));
                } else {
                    if ((year_num % 4 == 0 && year_num % 100 != 0) || year_num % 400 == 0)
                        wv_day.setAdapter(new NumericWheelAdapter(1, 29));
                    else
                        wv_day.setAdapter(new NumericWheelAdapter(1, 28));
                }
            }
        };
        // 添加"月"监听
        OnWheelChangedListener wheelListener_month = new OnWheelChangedListener() {
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                int month_num = newValue + 1;
                //  判断大小月及是否闰年,用来确定"日"的数据
                if (list_big.contains(String.valueOf(month_num))) {
                    wv_day.setAdapter(new NumericWheelAdapter(1, 31));
                } else if (list_little.contains(String.valueOf(month_num))) {
                    wv_day.setAdapter(new NumericWheelAdapter(1, 30));
                } else {
                    if (((wv_year.getCurrentItem() + 1990) % 4 == 0 && (wv_year
                            .getCurrentItem() + 1990) % 100 != 0)
                            || (wv_year.getCurrentItem() + 1990) % 400 == 0)
                        wv_day.setAdapter(new NumericWheelAdapter(1, 29));
                    else
                        wv_day.setAdapter(new NumericWheelAdapter(1, 28));
                }
            }
        };
        wv_year.addChangingListener(wheelListener_year);
        wv_month.addChangingListener(wheelListener_month);

        // 根据屏幕密度来指定选择器字体的大小
        int textSize = 0;
        textSize = pixelsToDip(this, 15);
        wv_day.TEXT_SIZE = textSize;
        wv_month.TEXT_SIZE = textSize;
        wv_year.TEXT_SIZE = textSize;

        Button btn_sure = (Button) view.findViewById(R.id.btn_datetime_sure);
        Button btn_cancel = (Button) view.findViewById(R.id.btn_datetime_cancel);
        // 确定
        btn_sure.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //  如果是个数,则显示为"02"的样式
                String parten = "00";
                DecimalFormat decimal = new DecimalFormat(parten);
                //  设置日期的显示
                String reportTime = (wv_year.getCurrentItem() + 1990) + "-"
                        + decimal.format((wv_month.getCurrentItem() + 1)) + "-"
                        + decimal.format((wv_day.getCurrentItem() + 1)) + " ";
                manufactureDate.setText(reportTime);
                dialog.dismiss();
            }
        });
        // 取消
        btn_cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });
        // 设置dialog的布局,并显示
        dialog.setContentView(view);
        dialog.show();
    }

    // 像素变为尺寸
    public static int pixelsToDip (Context context, int pixels) {
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        int screenw = mDisplayMetrics.widthPixels;
        float rate= (float) screenw / 320;
        float paintsize = 13 * rate;
        return (int)paintsize;
    }

    // 数据的非空判断
    private boolean isEmpty() {
        if (equipmentNumber.getText().length() < 1) {
            toast("请填写设备编号");
            return false;
        }
        if (equipmentId.getText().length() < 1){
            toast("请填写设备ID");
            return false;
        }
        if (equipmentType.getText().length() < 1){
            toast("请填写设备型号");
            return false;
        }
        if (deviceName.getText().length() < 1) {
            toast("请填写设备名称");
            return false;
        }
        if (deviceType.getText().length() < 1) {
            toast("请填写设备类型");
            return false;
        }
        if (pumpPower.getText().length() < 1) {
            toast("请填写水泵功率");
            return false;
        }
        if (systemFlow.getText().length() < 1) {
            toast("请填写系统流量");
            return false;
        }
        if (systemLift.getText().length() < 1) {
            toast("请填写系统扬程");
            return false;
        }
        if (importDn.getText().length() < 1){
            toast("请填写进口DN");
            return false;
        }
        if (exportDn.getText().length() < 1){
            toast("请填写出口DN");
            return false;
        }
        if (manufactureDate.getText().toString().equals("请选择出厂日期")) {
            toast("请选择出厂日期");
            return false;
        }
        if (customerName.getText().length() < 1) {
            toast("请填写用户名称");
            return false;
        }
        if (creater.getText().length() < 1) {
            toast("请填写联系人");
            return false;
        }
        if (customerPhone.getText().length() < 1) {
            toast("请填写联系方式");
            return false;
        }
        if (province.getText().length() < 1) {
            toast("请选择省份");
            return false;
        }
        if (detailPosition.getText().length() < 1){
            toast("请填写详细地址");
        }
        return true;
    }

    private void toast(String text){
        Toast.makeText(PerfectInformationActivity.this, text, Toast.LENGTH_SHORT).show();
    }
}
