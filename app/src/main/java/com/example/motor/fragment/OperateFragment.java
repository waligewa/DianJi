package com.example.motor.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.motor.R;
import com.example.motor.activity.LoginActivity;
import com.example.motor.activity.VideoSurveillanceActivity;
import com.example.motor.constant.ConstantsField;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.SPUtils;
import com.example.motor.util.SpinWindowAreaUntil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

/**
 * Created by chengxi on 17/4/26.
 * 先让它选择具体设备，然后点击管理员审核通过之后存到偏好设置里面，再根据是否有那俩条数据进行点击操作
 *
 */
public class OperateFragment extends Fragment implements View.OnClickListener{

    private TextView administrators;
    private CheckBox longRangeOpenCloseLight, cbLongRangeOpenDoor, remoteStartStop, switchValve;
    private RelativeLayout videoSurveillance;
    private SharedPreferences prefs1, prefs2;
    private SharedPreferences.Editor editor;
    private String gatewayAddress, guidString;
    private Intent intent;
    private AlertDialog alertDialog;

    private OperateHandler mOperateHandler = new OperateHandler();
    private class OperateHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    cbLongRangeOpenDoor.setChecked(false);
                    break;
                default:
                    break;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_operate_fragment, null);
        //remoteStartStop = (Switch) view.findViewById(R.id.remote_start_stop);
        //remoteStartStop.setOnClickListener(this);
        cbLongRangeOpenDoor = (CheckBox) view.findViewById(R.id.cb_open_door);
        longRangeOpenCloseLight = (CheckBox) view.findViewById(R.id.long_range_open_close_light);
        init();
        String deviceidString = prefs2.getString("deviceId", "");
        if (deviceidString.length() < 2) {
            cbLongRangeOpenDoor.setEnabled(false);
            longRangeOpenCloseLight.setEnabled(false);
        } else if (prefs1.getString("AdministratorName", "").equals("") ||
                prefs1.getString("AdministratorPassword", "").equals("")) {
            cbLongRangeOpenDoor.setEnabled(false);
            longRangeOpenCloseLight.setEnabled(false);
        } else {
            cbLongRangeOpenDoor.setEnabled(true);
            longRangeOpenCloseLight.setEnabled(true);
        }
        //switchValve = (Switch) view.findViewById(R.id.switch_valve);
        //switchValve.setOnClickListener(this);
        videoSurveillance = (RelativeLayout) view.findViewById(R.id.video_surveillance);
        administrators = (TextView) view.findViewById(R.id.administrators);
        //  右上角管理员按钮的点击事件
        administrators.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCheckDialog2();
            }
        });
        cbLongRangeOpenDoor.setOnClickListener(this);
        longRangeOpenCloseLight.setOnClickListener(this);
        videoSurveillance.setOnClickListener(this);
        x.view().inject(getActivity());
        return view;
    }

    private void init(){
        prefs1 = getActivity().getSharedPreferences("UserInfo", 0);
        prefs2 = getActivity().getSharedPreferences("device", Context.MODE_PRIVATE);
        editor = prefs1.edit();
        gatewayAddress = prefs1.getString("add", "");
        guidString = prefs1.getString("guid", "");
        intent = new Intent();
    }

    public void onClick(View v){
        // 这一句我本来打算也给它写在init()方法里面的，后来发现，当进入HomeFragment的时候，这个
        // OperateFragment已经界面初始化完成了，这样可以相当于缓存吧，这样的话就会导致即使用户点击
        // HomeFragment的左上角按钮之后也不会让OperateFragment的deviceidString得到变化，所以还是放在
        // 这里等到用户进行点击操作的时候再触发合理
        String deviceidString = prefs2.getString("deviceId", "");
        if (deviceidString.length() < 2 || SpinWindowAreaUntil.mfistCustemInfors.isEmpty()
                || SpinWindowAreaUntil.mfistCustemInfors.size() < 1) {
            toast("请在首页点击左上角按钮，选择相应设备");
        } else if(prefs1.getString("AdministratorName", "").equals("") ||
                prefs1.getString("AdministratorPassword", "").equals("")) {
            toast("请点击右上角管理员按钮");
        } else {
            switch (v.getId()){
                // 远程启停
                /*case R.id.remote_start_stop:
                    if(remoteStartStop.isChecked()){
                        //toast("开启");
                        showCheckDialog("");
                    }else {
                        //toast("关闭");
                        showCheckDialog("");
                    }
                    break;*/
                // 远程开门
                case R.id.cb_open_door:
                    /*Timer timer = new Timer();
                    class MyTask extends TimerTask {
                        @Override
                        public void run() {
                            cbLongRangeOpenDoor.setChecked(false);
                        }
                    }
                    timer.schedule(new MyTask(), 2000);*/
                    if(cbLongRangeOpenDoor.isChecked()){
                        //toast("开门");
                        lightingControl("OPENTHEDOOR");
                        // 这是用于将CheckBox 5秒之后弄成false的
                        mOperateHandler.sendEmptyMessageDelayed(1, 5000);
                    }
                    break;
                // 远程开关灯
                case R.id.long_range_open_close_light:
                    if(longRangeOpenCloseLight.isChecked()){
                        //toast("开灯");
                        lightingControl("ON");
                    }else{
                        //toast("关灯");
                        lightingControl("OFF");
                    }
                    break;
                //  远程开关阀门
                /*case R.id.switch_valve:
                    if(switchValve.isChecked()){
                        //toast("开");
                        showCheckDialog("");
                    }else{
                        //toast("关");
                        showCheckDialog("");
                    }
                    break;*/
                //  视频监控
                case R.id.video_surveillance:
                    intent.setClass(getActivity(), VideoSurveillanceActivity.class);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    }

    private void showCheckDialog2() {
        View checkAdminView = LayoutInflater.from(getActivity().getBaseContext())
                .inflate(R.layout.check_admin_layout, null);
        final EditText admin_num = (EditText) checkAdminView.findViewById(R.id.admin_num);
        final EditText admin_pwd = (EditText) checkAdminView.findViewById(R.id.admin_pwd);
        Button check_admin = (Button) checkAdminView.findViewById(R.id.check_admin);
        Button cancel_check = (Button) checkAdminView.findViewById(R.id.cancel_check);
        check_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (admin_num.getText().toString().trim().equals("")) {
                    admin_num.setText("");
                    toast("管理员账号不可为空，请重新填写！");
                } else if (admin_pwd.getText().toString().trim().equals("")) {
                    admin_pwd.setText("");
                    toast("管理员密码不可为空，请重新填写！");
                } else {
                    checkAdmin2(admin_num.getText().toString().trim(), admin_pwd.getText().toString().trim());
                    alertDialog.dismiss();
                }
            }
        });
        cancel_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
            }
        });
        alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setCancelable(false);
        alertDialog.setView(new EditText(getActivity())); //  不加这句代码EditText无法填入东西
        alertDialog.show();
        alertDialog.getWindow().setContentView(checkAdminView); //  这句代码的意思是将布局填入AlertDialog
    }

    private void checkAdmin2(final String userName, final String userPwd) {
        RequestParams params;
        if(TextUtils.isEmpty(SPUtils.getStringData(getActivity(), ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/IsAdmin");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/IsAdmin");
        }
        params.addBodyParameter("userName", userName);
        params.addBodyParameter("userPwd", userPwd);
        params.addBodyParameter("guid", guidString);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast(ex.getMessage());
            }

            @Override
            public void onFinished() {}

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object = new JSONObject(arg0);
                    if (object.getString("Code").equals("0")) {
                        intent.setClass(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                        toast(object.getString("Message"));
                    } else if (object.getString("Code").equals("1")) {
                        editor.putString("AdministratorName", userName);
                        editor.putString("AdministratorPassword", userPwd);
                        editor.apply();
                        cbLongRangeOpenDoor.setEnabled(true);
                        longRangeOpenCloseLight.setEnabled(true);
                    }
                    toast(object.getString("Message"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void lightingControl(String state) {
        RequestParams params;
        // 请求参数
        if (state.equals("ON")) {
            if(TextUtils.isEmpty(SPUtils.getStringData(getActivity(), ConstantsField.ADDRESS))){
                params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadTurnOn");
            } else {
                params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadTurnOn");
            }
        } else if (state.equals("OFF")) {
            if(TextUtils.isEmpty(SPUtils.getStringData(getActivity(), ConstantsField.ADDRESS))){
                params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadTurnOff");
            } else {
                params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadTurnOff");
            }
        } else if (state.equals("OPENTHEDOOR")){
            if(TextUtils.isEmpty(SPUtils.getStringData(getActivity(), ConstantsField.ADDRESS))){
                params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadVFDReset");
            } else {
                params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadVFDReset");
            }
        } else {
            if(TextUtils.isEmpty(SPUtils.getStringData(getActivity(), ConstantsField.ADDRESS))){
                params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/");
            } else {
                params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/");
            }
        }
        // 这一句我本来打算也给它写在init()方法里面的，后来发现，当进入HomeFragment的时候，这个
        // OperateFragment已经界面初始化完成了，这样可以相当于缓存吧，这样的话就会导致即使用户点击
        // HomeFragment的左上角按钮之后也不会让OperateFragment的id得到变化，所以还是放在这里等到用户
        // 进行点击操作的时候再触发合理
        String id = getActivity().getSharedPreferences("device", Context.MODE_PRIVATE)
                .getString("EquipmentID", "");
        params.addBodyParameter("id", id);
        params.addBodyParameter("guid", guidString);
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
                    JSONObject object = new JSONObject(arg0.substring(1, arg0.length() - 1).replace("\\", ""));
                    toast(object.getString("Message"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*private void showCheckDialog(final String state) {
        View checkAdminView = LayoutInflater.from(getActivity().getBaseContext())
                .inflate(R.layout.check_admin_layout, null);
        final EditText admin_num = (EditText) checkAdminView.findViewById(R.id.admin_num);
        final EditText admin_pwd = (EditText) checkAdminView.findViewById(R.id.admin_pwd);
        Button check_admin = (Button) checkAdminView.findViewById(R.id.check_admin);
        Button cancel_check = (Button) checkAdminView.findViewById(R.id.cancel_check);
        check_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (admin_num.getText().toString().trim().equals("")) {
                    admin_num.setText("");
                    toast("管理员账号不可为空，请重新填写！");
                } else if (admin_pwd.getText().toString().trim().equals("")) {
                    admin_pwd.setText("");
                    toast("管理员密码不可为空，请重新填写！");
                } else {
                    checkAdmin(admin_num.getText().toString().trim(),
                            admin_pwd.getText().toString().trim(), state);
                    alertDialog.dismiss();
                }
            }
        });
        cancel_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
            }
        });
        alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setCancelable(false);
        alertDialog.setView(new EditText(getActivity())); //  不加这句代码EditText无法填入东西
        alertDialog.show();
        alertDialog.getWindow().setContentView(checkAdminView); //  这句代码的意思是将布局填入AlertDialog
    }*/

    //Toast提醒的封装
    private void toast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }
}
