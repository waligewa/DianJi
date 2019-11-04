package com.example.motor.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.adapter.DeleteDeviceReportAdapter;
import com.example.motor.base.BaseActivity;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.DeleteDeviceReportItem;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DoubleClickUtils;
import com.example.motor.util.Loadding;
import com.example.motor.util.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

/**
 * 设备报修activity
 *
 */
public class DeleteDeviceReportActivity extends BaseActivity {

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Loadding loadding;
    private Intent intent;
    private Activity mActivity;
    private String userId, gatewayAddress = "";
    private DeleteDeviceReportAdapter adapter;
    private List<DeleteDeviceReportItem> list = new LinkedList<>();
    private Loadding loading;
    private AlertDialog alertDialog;
    @ViewInject(R.id.listview)
    ListView listView;
    @ViewInject(R.id.radiogroup)
    RadioGroup radioGroup;
    @ViewInject(R.id.unhandle)
    RadioButton unHandle;
    @ViewInject(R.id.handle)
    RadioButton handle;

    @Override
    public int setLayout() {
        return R.layout.activity_delete_device_report;
    }

    @Override
    public void setTitle() {
        mBaseTitleBarView.setTitleText("报修列表");
        mBaseTitleBarView.setLeftDrawable(-1);
        mBaseTitleBarView.setRightDrawable(R.mipmap.add);
    }

    @Override
    public void initWidget() {
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);

        init();
    }

    @Override
    public void widgetClick(View v) {}

    // 返回键事件
    @Override
    public void onTitleLeftPressed() {
        onBackPressed();
    }

    @Override
    public void onTitleRightImagePressed(){
        if(!DoubleClickUtils.isFastDoubleClick()){
            intent.setClass(this, EquipmentReportRepairActivity.class);
            startActivityForResult(intent, 1);// 先加一个requestCode 1
        }
    }

    private void init(){
        loading = new Loadding(this);
        intent = new Intent();
        mActivity = this;
        prefs = getSharedPreferences("UserInfo", 0); //  创建一个偏好文件
        editor = prefs.edit();
        intent = new Intent();
        loadding = new Loadding(this);
        userId = String.valueOf(prefs.getInt("UserID", 0));
        gatewayAddress = prefs.getString("add", "");

        adapter = new DeleteDeviceReportAdapter(mActivity, list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    if(list.get(position).getIsrevice() != null && list.get(position).getIsrevice().equals("已派工")){
                        // 把实体类子项传递过去DeviceInspectionActivity
                        intent.setClass(mActivity, EquipmentReportRepairActivity2.class);
                        intent.putExtra("delete_device_report_item", list.get(position));
                        startActivityForResult(intent, 1);// 先加一个requestCode 1
                    } else if (list.get(position).getIsrevice() == null || !list.get(position).getIsrevice().equals("已派工")){
                        // 显示选择对话框
                        showDialog(list.get(position));
                    }
                }
            }
        });
        // RadioGroup的两个RadioButton的两个点击事件
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    if (checkedId == R.id.unhandle) {
                        // 未处理
                        getNoHandleData();
                    } else if (checkedId == R.id.handle){
                        // 已处理
                        getRepairReport();
                    }
                }
            }
        });
        // 获取维修提报未处理数据
        //getNoHandleData();
    }

    // 显示选择对话框
    private void showDialog(final DeleteDeviceReportItem item) {
        View v = LayoutInflater.from(getBaseContext()).inflate(R.layout.select_mode_layout, null);
        TextView checkInformation = (TextView) v.findViewById(R.id.check_information);
        TextView deleteInformation = (TextView) v.findViewById(R.id.delete_information);
        checkInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                // 把实体类子项传递过去DeviceInspectionActivity
                intent.setClass(mActivity, EquipmentReportRepairActivity2.class);
                intent.putExtra("delete_device_report_item", item);
                startActivityForResult(intent, 1);// 先加一个requestCode 1
            }
        });
        deleteInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 删除维修提报数据
                deleteData(item);
            }
        });
        alertDialog = new AlertDialog.Builder(mActivity).create();
        alertDialog.setCancelable(true);
        alertDialog.show();
        alertDialog.getWindow().setContentView(v); // 这句代码的意思是将布局填入AlertDialog
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        super.onActivityResult(requestCode, resultCode, data);  // 这个super可不能落下，否则可能回调不了
        switch(requestCode){
            case 1:
                // 如果既是1又是RESULT_OK的话就可以执行下面的
                if(resultCode == mActivity.RESULT_OK){
                    if(unHandle.isChecked()){
                        // 获取维修提报未处理数据
                        getNoHandleData();
                    } else if(handle.isChecked()){
                        // 获取维修提报数据
                        getRepairReport();
                    }
                }
                break;
        }
    }

    // 获取维修提报数据
    private void getRepairReport() {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadFaultReport");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadFaultReport");
        }
        params.addBodyParameter("UserID", userId);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast(ex.getMessage());
                if (loading.isShow()) {
                    loading.close();
                }
            }

            @Override
            public void onFinished() { }

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        intent.setClass(mActivity, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")){
                        list.clear();
                        Type listType = new TypeToken<List<DeleteDeviceReportItem>>() {}.getType();
                        Gson gson = new Gson();
                        List<DeleteDeviceReportItem> gsonDatas = gson.fromJson(object1.getString("Data"), listType);
                        for(int i = 0; i < gsonDatas.size(); i++){
                            DeleteDeviceReportItem item = gsonDatas.get(i);
                            if(item.getIsrevice() != null && item.getIsrevice().equals("已派工")){
                                list.add(item);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        toast(object1.getString("Message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (loading.isShow()) {
                    loading.close();
                }
            }
        });
    }

    // 获取维修提报未处理数据
    private void getNoHandleData() {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadFaultReport");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadFaultReport");
        }
        params.addBodyParameter("UserID", userId);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast(ex.getMessage());
                if (loading.isShow()) {
                    loading.close();
                }
            }

            @Override
            public void onFinished() { }

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        intent.setClass(mActivity, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")){
                        list.clear();
                        Type listType = new TypeToken<List<DeleteDeviceReportItem>>() {}.getType();
                        Gson gson = new Gson();
                        List<DeleteDeviceReportItem> gsonDatas = gson.fromJson(object1.getString("Data"), listType);
                        for(int i = 0; i < gsonDatas.size(); i++){
                            DeleteDeviceReportItem item = gsonDatas.get(i);
                            if(gsonDatas.get(i).getIsrevice() == null || !gsonDatas.get(i).getIsrevice().equals("已派工")){
                                list.add(item);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        toast(object1.getString("Message"));
                        /*list.clear();
                        adapter.notifyDataSetChanged();*/
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (loading.isShow()) {
                    loading.close();
                }
            }
        });
    }

    // 删除维修提报数据
    private void deleteData(final DeleteDeviceReportItem item) {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/DelFaultReport");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/DelFaultReport");
        }
        params.addBodyParameter("id", item.getID());
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast(ex.getMessage());
                if (loading.isShow()) {
                    loading.close();
                }
            }

            @Override
            public void onFinished() { }

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        intent.setClass(mActivity, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")){
                        toast(object1.getString("Message"));
                        alertDialog.dismiss();
                        // 10月31日在pda手机上面出现一个错误，设备报修第一个数据，然后删除之后，对话框可以消失，但是子项不消失，
                        // 因为我删除完毕之后还要调用一个接口，在这个接口Code为1里面才进行界面的更新操作，现在将界面的更新操作放在这里，
                        // 通过集合list的remove一个子项，再通知适配器变化来实现
                        list.remove(item);
                        adapter.notifyDataSetChanged();
                        // 获取维修提报未处理数据
                        getNoHandleData();
                    } else {
                        toast(object1.getString("Message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (loading.isShow()) {
                    loading.close();
                }
            }
        });
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (handle.isChecked()) {
            // 已处理
            getRepairReport();
        } else if (unHandle.isChecked()){
            // 未处理
            getNoHandleData();
        }
    }
}
