package com.example.motor.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.IdRes;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.base.BaseActivity;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.CommonResponseBean;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DoubleClickUtils;
import com.example.motor.util.Loadding;
import com.example.motor.util.SPUtils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

public class SatisfactionSurveyActivity extends BaseActivity {

    private Loadding loadding;
    private Intent intent;
    @ViewInject(R.id.username)
    EditText userName;
    @ViewInject(R.id.contact_mode)
    EditText contactMode;
    @ViewInject(R.id.device_number)
    EditText deviceNumber;
    @ViewInject(R.id.customer_unit)
    EditText customerUnit;
    @ViewInject(R.id.device_phenomenon)
    EditText devicePhenomenon;
    @ViewInject(R.id.device_address)
    EditText deviceAddress;
    @ViewInject(R.id.analysis_reason)
    EditText analysisReason;
    @ViewInject(R.id.user_evaluate)
    RadioGroup workSituation;
    @ViewInject(R.id.satisfaction1)
    RadioButton satisfaction1;
    @ViewInject(R.id.dissatisfied1)
    RadioButton dissatisfied1;

    private SharedPreferences prefs1, prefs2;
    private String gatewayAddress, equipmentNo, guid, satisfied1 = "true";
    // layout文件
    @Override
    public int setLayout() {
        return R.layout.activity_satisfaction_survey;
    }

    // 在这里面设置TitleBar相关的
    @Override
    public void setTitle() {
        mBaseTitleBarView.setTitleText("满意度调查");
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
    public void widgetClick(View v) {
    }

    // 返回键事件
    @Override
    public void onTitleLeftPressed() {
        onBackPressed();
    }

    private void init() {
        intent = new Intent();
        loadding = new Loadding(this);
        prefs1 = getSharedPreferences("UserInfo", 0);
        guid = prefs1.getString("guid", "");
        //  Log.e("guid", guid);
        prefs2 = getSharedPreferences("device", Context.MODE_PRIVATE);
        equipmentNo = prefs2.getString("EquipmentNo", "");
        //  Log.e("设备编号", equipmentNo);
        deviceNumber.setText(equipmentNo);   //  从device的偏好文件里面取出设备编号复制给deviceNumber
        gatewayAddress = prefs1.getString("add", "");
        satisfaction1.setChecked(true);
        workSituation.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if (satisfaction1.getId() == i) {
                    Log.e("试一下1", "是");
                    satisfied1 = "true";
                } else if (dissatisfied1.getId() == i) {
                    Log.e("试一下1", "否");
                    satisfied1 = "false";
                }
            }
        });
        userName.setText("");
        contactMode.setText("");
        customerUnit.setText("");
        devicePhenomenon.setText("");
        deviceAddress.setText("");
        analysisReason.setText("");
    }

    @Event(value = {R.id.tv_submit}, type = View.OnClickListener.class)
    private void btnClick(View v){
        switch (v.getId()){
            case R.id.tv_submit:
                if (isEmpty()) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setMessage("您确定提交吗？");
                    dialog.setCancelable(true);
                    dialog.setPositiveButton("是的", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try{
                                if(!DoubleClickUtils.isFastDoubleClick()){
                                    getData();
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                    dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });
                    dialog.show();
                }
                break;
        }
    }

    private void getData() {
        loadding.show("正在上传中...");
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/AddProblemFeedback");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/AddProblemFeedback");
        }
        params.addBodyParameter("guid", guid);
        params.addBodyParameter("name", userName.getText().toString().trim());
        params.addBodyParameter("telePhone", contactMode.getText().toString().trim());
        params.addBodyParameter("equNo", deviceNumber.getText().toString().trim());
        params.addBodyParameter("equType", customerUnit.getText().toString().trim());
        params.addBodyParameter("symptom", devicePhenomenon.getText().toString().trim());
        params.addBodyParameter("solutions", deviceAddress.getText().toString().trim());
        params.addBodyParameter("faultAnalysis", analysisReason.getText().toString().trim());
        params.addBodyParameter("userSatisfaction", satisfied1);

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
                        toast("添加问题反馈信息成功");
                        userName.setText("");
                        contactMode.setText("");
                        customerUnit.setText("");
                        devicePhenomenon.setText("");
                        deviceAddress.setText("");
                        analysisReason.setText("");
                    } else if (commonResponseBean.getCode() == 0) {
                        Intent intent = new Intent(SatisfactionSurveyActivity.this, LoginActivity.class);
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

    // 数据的非空判断
    private boolean isEmpty() {
        if (userName.getText().length() < 1) {
            toast("请填写用户姓名");
            return false;
        }
        if(contactMode.getText().length() < 1){
            toast("请填写联系方式");
            return false;
        }
        if (deviceNumber.getText().length() < 1) {
            toast("请填写设备编号");
            return false;
        }
        if (customerUnit.getText().length() < 1) {
            toast("请填写客户单位");
            return false;
        }
        if (devicePhenomenon.getText().length() < 1) {
            toast("请填写设备现象");
            return false;
        }
        if(deviceAddress.getText().length() < 1){
            toast("请填写设备地址");
            return false;
        }
        if (analysisReason.getText().length() < 1) {
            toast("请填写分析原因");
            return false;
        }
        return true;
    }

    // Toast提醒的封装
    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
