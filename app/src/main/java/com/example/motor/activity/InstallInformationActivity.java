package com.example.motor.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.base.BaseActivity;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.CommonResponseBean;
import com.example.motor.db.InstallDebug;
import com.example.motor.db.RtuInfo;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DoubleClickUtils;
import com.example.motor.util.Loadding;
import com.example.motor.util.SPUtils;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 安装信息的activity
 *
  */
public class InstallInformationActivity extends BaseActivity {

    @ViewInject(R.id.device_number)
    EditText deviceNumber;
    @ViewInject(R.id.located_area)
    EditText locatedArea;
    @ViewInject(R.id.engineer_place)
    EditText engineerPlace;
    @ViewInject(R.id.user_address)
    EditText userAddress;
    @ViewInject(R.id.username)
    EditText username;
    @ViewInject(R.id.site_name)
    EditText siteName;
    @ViewInject(R.id.device_type)
    EditText deviceType;
    @ViewInject(R.id.machine_installer)
    EditText machineInstaller;
    @ViewInject(R.id.electrical_installer)
    EditText electricalInstaller;
    @ViewInject(R.id.equipment_debugger)
    EditText equipmentDebugger;
    @ViewInject(R.id.installation_debug_conclusion)
    EditText installationDebugConclusion;
    @ViewInject(R.id.installation_debug_feedback)
    EditText installationDebugFeedback;
    @ViewInject(R.id.feedback_measure)
    EditText feedbackMeasure;
    @ViewInject(R.id.arrival_date)
    TextView arrivalDate;
    @ViewInject(R.id.machine_install_date)
    TextView machineInstallDate;
    @ViewInject(R.id.electrical_install_date)
    TextView electricalInstallDate;
    @ViewInject(R.id.equipment_debug_date)
    TextView equipmentDebugDate;
    @ViewInject(R.id.spinner)
    Spinner spinner;
    private Intent intent;
    private Calendar calendar;  // 用来装日期的
    private String guid, address, equipmentNo, deviceid = "";
    private SharedPreferences prefs1, prefs2;
    private Loadding loadding;
    private InstallDebug installDebug = null;
    private int userId;
    private Activity mActivity;
    // 下拉框
    private Spinner_Adapter adapter;
    private List<RtuInfo> list = new ArrayList<>(); // spinner的集合

    // layout文件
    @Override
    public int setLayout() {
        return R.layout.activity_install_information;
    }

    // 在这里面设置TitleBar相关的
    @Override
    public void setTitle() {
        mBaseTitleBarView.setTitleText("安装信息");
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
        mActivity = this;
        prefs1 = getSharedPreferences("UserInfo", 0);
        prefs2 = getSharedPreferences("device", Context.MODE_PRIVATE);
        guid = prefs1.getString("guid", "");
        address = prefs1.getString("add", "");
        userId = prefs1.getInt("UserID", 0);
        equipmentNo = prefs2.getString("EquipmentNo", "");
        deviceNumber.setText("");
        locatedArea.setText("");
        engineerPlace.setText("");
        userAddress.setText("");
        username.setText("");
        siteName.setText("");
        deviceType.setText("");
        machineInstaller.setText("");
        electricalInstaller.setText("");
        equipmentDebugger.setText("");
        installationDebugConclusion.setText("");
        installationDebugFeedback.setText("");
        feedbackMeasure.setText("");
        if(!TextUtils.isEmpty(equipmentNo)){
            // 根据设备编号获取安装调试信息
            getData2();
        }
        // 根据guid获取设备厂商信息
        getData4();
        // EditText的获取焦点状态从而实现一些操作
        deviceNumber.setOnFocusChangeListener(new android.view.View.
                OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // 设备编号不为空字符串的话才进行操作
                    if(!TextUtils.isEmpty(deviceNumber.getText())){
                        // 通过设备编号得到设备信息
                        //getData3();
                    }
                }
            }
        });

        // spinner的适配器和附加上适配器
        adapter = new Spinner_Adapter(getApplicationContext(), list);
        spinner.setAdapter(adapter);
        // spinner的点击事件
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                RtuInfo info = list.get(position); // 获取实体类RtuInfo
                deviceid = info.getDeviceID();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    @Event(value = { R.id.arrival_date, R.id.machine_install_date, R.id.electrical_install_date,
            R.id.equipment_debug_date, R.id.tv_submit }, type = View.OnClickListener.class)
    private void btnClick(View v){
        switch(v.getId()){
            case R.id.arrival_date:
                // 日期选择方法
                showDate1();
                break;
            case R.id.machine_install_date:
                showDate2();
                break;
            case R.id.electrical_install_date:
                // 日期选择方法
                showDate3();
                break;
            case R.id.equipment_debug_date:
                // 日期选择方法
                showDate4();
                break;
            case R.id.tv_submit:
                if (isEmpty() && !DoubleClickUtils.isFastDoubleClick()) {
                    installDebug = new InstallDebug(
                            deviceNumber.getText().toString(),
                            locatedArea.getText().toString(),
                            engineerPlace.getText().toString(),
                            deviceNumber.getText().toString(),
                            siteName.getText().toString(),
                            username.getText().toString(),
                            userAddress.getText().toString(),
                            deviceType.getText().toString(),
                            arrivalDate.getText().toString(),
                            machineInstaller.getText().toString(),
                            machineInstallDate.getText().toString(),
                            electricalInstaller.getText().toString(),
                            electricalInstallDate.getText().toString(),
                            equipmentDebugDate.getText().toString(),
                            equipmentDebugger.getText().toString(),
                            "true",
                            deviceid,
                            installationDebugConclusion.getText().toString(),
                            installationDebugFeedback.getText().toString(),
                            feedbackMeasure.getText().toString());
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setMessage("您确定提交吗？");
                    dialog.setCancelable(true);
                    dialog.setPositiveButton("是的", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try{
                                if(!DoubleClickUtils.isFastDoubleClick()){
                                    // 提交安装信息
                                    getData();
                                }
                            }catch (Exception e){
                                e.printStackTrace();
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
        }
    }

    // 日期选择方法
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
    }

    // 日期选择方法
    private void showDate2() {
        calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, AlertDialog.BUTTON_POSITIVE,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        monthOfYear++;
                        machineInstallDate.setText(year + "年" + monthOfYear + "月" + dayOfMonth + "日");
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    // 日期选择方法
    private void showDate3() {
        calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, AlertDialog.BUTTON_POSITIVE,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        monthOfYear++;
                        electricalInstallDate.setText(year + "年" + monthOfYear + "月" + dayOfMonth + "日");
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    // 日期选择方法
    private void showDate4() {
        calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, AlertDialog.BUTTON_POSITIVE,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        monthOfYear++;
                        equipmentDebugDate.setText(year + "年" + monthOfYear + "月" + dayOfMonth + "日");
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    // 提交安装信息
    private void getData() {
        Gson gson = new Gson();
        String data = gson.toJson(installDebug, InstallDebug.class);
        loadding.show("正在上传中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/DeviceInstallDebug");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/DeviceInstallDebug");
        }
        params.addBodyParameter("guid", "");
        params.addBodyParameter("installDebug", data);

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
                        toast(commonResponseBean.getMessage());
                        deviceNumber.setText("");
                        locatedArea.setText("");
                        engineerPlace.setText("");
                        userAddress.setText("");
                        username.setText("");
                        siteName.setText("");
                        deviceType.setText("");
                        machineInstaller.setText("");
                        electricalInstaller.setText("");
                        equipmentDebugger.setText("");
                        installationDebugConclusion.setText("");
                        installationDebugFeedback.setText("");
                        feedbackMeasure.setText("");
                        arrivalDate.setText("请选择日期");
                        machineInstallDate.setText("请选择日期");
                        electricalInstallDate.setText("请选择日期");
                        equipmentDebugDate.setText("请选择日期");
                        //finish();
                    } else if (commonResponseBean.getCode() == 0) {
                        intent.setClass(InstallInformationActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(commonResponseBean.getMessage());
                    } else {
                        toast(commonResponseBean.getMessage());
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

    // 根据设备编号获取安装调试信息
    private void getData2() {
        if(equipmentNo.equals("")) return;
        loadding.show("加载数据中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadInstallDebug");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/LoadInstallDebug");
        }
        params.addBodyParameter("guid", guid);
        params.addBodyParameter("equNo", equipmentNo);//equipmentNo    "0712024"
        params.addBodyParameter("userId", String.valueOf(userId));

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
                        deviceNumber.setText(jsonObject.getString("EquipmentNum"));
                        locatedArea.setText(jsonObject.getString("Area"));
                        engineerPlace.setText(jsonObject.getString("EngineeringPlace"));
                        siteName.setText(jsonObject.getString("DeviceName"));
                        username.setText(jsonObject.getString("VillageName"));
                        userAddress.setText(jsonObject.getString("CustomerAddress"));
                        deviceType.setText(jsonObject.getString("EquipmentType"));
                        arrivalDate.setText(jsonObject.getString("ArrivalDate").split("T")[0]);
                        machineInstaller.setText(jsonObject.getString("MachineUser"));
                        machineInstallDate.setText(jsonObject.getString("MachineDate").split("T")[0]);
                        electricalInstaller.setText(jsonObject.getString("ElectricalUser"));
                        electricalInstallDate.setText(jsonObject.getString("ElectricalDate").split("T")[0]);
                        equipmentDebugDate.setText(jsonObject.getString("DebuggDate").split("T")[0]);
                        equipmentDebugger.setText(jsonObject.getString("DebuggUser"));
                        installationDebugConclusion.setText(jsonObject.getString("Conclusion").equals("null") ? "" : jsonObject.getString("Conclusion"));
                        installationDebugFeedback.setText(jsonObject.getString("Feedback").equals("null") ? "" : jsonObject.getString("Feedback"));
                        feedbackMeasure.setText(jsonObject.getString("Mesures").equals("null") ? "" : jsonObject.getString("Mesures"));
                        // spinner的赋值操作
                        if(Integer.valueOf(jsonObject.getString("Manufacturer")) <= 26){
                            spinner.setSelection(Integer.valueOf(jsonObject.getString("Manufacturer")) - 15);
                        }
                    } else if (commonResponseBean.getCode() == 0) {
                        intent.setClass(InstallInformationActivity.this, LoginActivity.class);
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

    // 通过设备编号得到设备信息，此activity里面没有作用
    private void getData3() {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadRtuData");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/LoadRtuData");
        }
        params.addBodyParameter("userId", String.valueOf(userId));
        params.addBodyParameter("equNo", deviceNumber.getText().toString().trim());
        params.addBodyParameter("pageSize", "20");
        params.addBodyParameter("pageIndex", "1");
        params.addBodyParameter("guid", guid);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {}

            @Override
            public void onError(Throwable ex, boolean arg1) { }

            @Override
            public void onFinished() { }

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject jsonObject1 = new JSONObject(arg0);
                    if (jsonObject1.getString("Code").equals("0")) {
                        intent.setClass(mActivity, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(jsonObject1.getString("Message"));
                    } else if (jsonObject1.getString("Code").equals("1")) {
                        JSONObject jsonObject2 = new JSONObject(jsonObject1.getString("Data"));
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    // 根据guid获取设备厂商信息
    private void getData4() {
        list.clear();
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/GetManufacturer");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/GetManufacturer");
        }
        params.addBodyParameter("guid", guid);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast(ex.getMessage());
            }

            @Override
            public void onFinished() { }

            @Override
            public void onSuccess(String arg0) {
                try {
                    CommonResponseBean commonResponseBean = new CommonResponseBean().getcommenHit(arg0);
                    if (commonResponseBean.getCode() == 1) {
                        JSONArray jsonArray = new JSONArray(commonResponseBean.getData());
                        for(int i = 0; i < jsonArray.length(); i++){
                            JSONObject object = jsonArray.getJSONObject(i);
                            RtuInfo info = new RtuInfo();
                            info.setDeviceID(object.getString("ID"));
                            info.setDeviceName(object.getString("ManufacturerName"));
                            list.add(info);
                        }
                        adapter.notifyDataSetChanged();
                    } else if (commonResponseBean.getCode() == 0) {
                        intent.setClass(InstallInformationActivity.this, LoginActivity.class);
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

    // 数据的非空判断
    private boolean isEmpty() {
        if (deviceNumber.getText().length() < 1) {
            toast("请填写设备编号");
            return false;
        }
        if(locatedArea.getText().length() < 1){
            toast("请填写所在地区");
            return false;
        }
        if (engineerPlace.getText().length() < 1) {
            toast("请填写工程地点");
            return false;
        }
        if (userAddress.getText().length() < 1) {
            toast("请填写用户地址");
            return false;
        }
        if (username.getText().length() < 1) {
            toast("请填写用户名称");
            return false;
        }
        if(siteName.getText().length() < 1){
            toast("请填写站点名称");
            return false;
        }
        if (deviceType.getText().length() < 1) {
            toast("请填写设备类型");
            return false;
        }
        if (arrivalDate.getText().toString().equals("请选择日期")) {
            toast("请选择到达日期");
            return false;
        }
        if (machineInstaller.getText().length() < 1) {
            toast("请填写机器安装人员");
            return false;
        }
        if (machineInstallDate.getText().toString().equals("请选择日期")) {
            toast("请选择机器安装日期");
            return false;
        }
        if (electricalInstaller.getText().length() < 1) {
            toast("请填写电气安装人员");
            return false;
        }
        if (electricalInstallDate.getText().toString().equals("请选择日期")) {
            toast("请选择电气安装日期");
            return false;
        }
        if (electricalInstaller.getText().length() < 1) {
            toast("请填写设备调试人员");
            return false;
        }
        if (electricalInstallDate.getText().toString().equals("请选择日期")) {
            toast("请选择设备调试日期");
            return false;
        }
        if (installationDebugConclusion.getText().length() < 1) {
            toast("请填写安装调试结论");
            return false;
        }
        if (installationDebugFeedback.getText().length() < 1) {
            toast("请填写安装调试反馈");
            return false;
        }
        if (feedbackMeasure.getText().length() < 1) {
            toast("请填写反馈措施");
            return false;
        }
        return true;
    }

    private void toast(String text){
        Toast.makeText(InstallInformationActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    public class Spinner_Adapter extends BaseAdapter {
        private LayoutInflater inflater;
        private List<RtuInfo> mlist;
        private TextView deviceName;

        public Spinner_Adapter(Context context, List<RtuInfo> list) {
            this.inflater = LayoutInflater.from(context);
            this.mlist = list;
        }

        @Override
        public int getCount() {
            return mlist.size();
        }

        @Override
        public Object getItem(int position) {
            return mlist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.rtu_item, null);
                // 实例化控件
                deviceName = (TextView) convertView.findViewById(R.id.tv_devicename);
                convertView.setTag(new DataWrapper(deviceName));
            } else {
                DataWrapper dataWrapper = (DataWrapper) convertView.getTag();
                deviceName = dataWrapper.DeviceName;
            }
            // 为控件赋值
            RtuInfo m = list.get(position); // 这里这个list是成员变量list集合
            deviceName.setText(m.getDeviceName());
            return convertView;
        }
    }

    private final class DataWrapper {
        public TextView DeviceName;
        public DataWrapper( TextView deviceName ) {
            this.DeviceName = deviceName;
        }
    }
}
