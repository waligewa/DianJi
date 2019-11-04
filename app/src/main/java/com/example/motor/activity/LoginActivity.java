package com.example.motor.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.db.AuthorityRole;
import com.example.motor.db.CommonResponseBean;
import com.example.motor.service.LocationServer;
import com.example.motor.service.LongRunningService;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DoubleClickUtils;
import com.example.motor.util.Loadding;
import com.example.motor.util.MyAlertDialog;
import com.example.motor.util.NetWorkUtil;
import com.example.motor.util.TagAliasOperatorHelper;
import com.example.motor.util.UserInfoUtils;
import com.example.motor.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static com.example.motor.util.TagAliasOperatorHelper.ACTION_SET;
import static com.example.motor.util.TagAliasOperatorHelper.sequence;

/**
 * @RuntimePermissions 标记需要运行时判断的类
 * 一次性申请一个权限或多个
 * @NeedsPermission 当申请的权限被用户允许后，调用此方法
 * @OnShowRationale 当第一次申请权限时，用户选择拒绝，再次申请时调用此方法，在此方法中提示用户为什么需要这个权限
 * @OnPermissionDenied 当申请的权限被用户拒绝后，调用此方法
 * @OnNeverAskAgain 当用户点击不再询问后，调用此方法
 * 二、后来更改要增加权限的设置，我就把登录成功之后的Role字符串全部保存在SP当中了，竟然可以成功，
 * 这个东西的存量是可以的
 */
@RuntimePermissions
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "dah_LoginActivity";

    private String add, account, password;
    private SharedPreferences prefs, prefs2;
    private SharedPreferences.Editor editor, editor2;
    private Loadding loadding, loaddingTest;
    private Boolean isRemember = false;
    private Intent intent;
    private List<AuthorityRole> list1 = new ArrayList<>();
    private Handler handler = new Handler();
    private Activity mActivity;
    @ViewInject(R.id.username)
    EditText userName;
    @ViewInject(R.id.password)
    EditText passWord;
    @ViewInject(R.id.checkbox1)
    CheckBox checkBox;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (loaddingTest.isShow()) {
                loaddingTest.close();
            }
            new MyAlertDialog(LoginActivity.this)
                    .builder()
                    .setCancelable(false)
                    .setMsg("审核成功，请重新登录")
                    .show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > 18) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.login2);
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);

        mActivity = this;
        // 18年11月30日出现一个bug。如果在6秒审核期之内打开了其他应用返回的时候loaddingTest会一直存在，只能结束进程才能正常使用。这是个bug。
        // 后来了解到生命周期问题。原因在于之前loadding的初始化是在init里面的，重新进入登录activity的时候loadding对象被重新创建，
        // 之前的对象的close方法被执行，现在的loadding对象没有执行close方法，但是确实又显示出来了，因为是个进度条啊，就会一直持有loadding对象，
        // 一直使用这个loadding对象，然后就变成当前loadding对象不是之前的对象了。就会一直存在进度条，后来我将loadding的初始化放在了onCreate里面，
        // 这样这个对象就不会被init()方法所干扰，然后在onResume()方法里面加上结束loadding的方法，这样就可以解决问题。
        loaddingTest = new Loadding(mActivity);
        requestPermission();
    }

    // 初始化
    private void init() {
        loadding = new Loadding(this);
        prefs = getSharedPreferences("UserInfo", 0);  // 创建一个偏好文件
        prefs2 = getSharedPreferences("device", Context.MODE_PRIVATE);  // 创建一个偏好文件
        editor = prefs.edit();
        editor2 = prefs2.edit();
        // 从数据库中取出remember_password参数的值
        isRemember = prefs.getBoolean("ischeck", false);
        checkBox.setChecked(isRemember);
        // 从数据库中取出account参数的值
        account = prefs.getString("username", "");
        // 从数据库中取出password参数的值
        password = prefs.getString("userpass", "");
        intent = new Intent();
        if (isRemember) {
            userName.setText(account);
            userName.setSelection(account.length());
            passWord.setText(password);
            passWord.setSelection(password.length());
        } else {
            userName.setText(account);
            userName.setSelection(account.length());
            passWord.setText("");
        }
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    isRemember = true;
                } else {
                    isRemember = false;
                }
            }
        });
        // 18-09-28因为推送过来点击之后总是会登录过期（当然原因是guid的问题），
        // 所以只要进入登录界面就将这个服务给关闭
        if(Utils.isServiceExisted(LoginActivity.this, LocationServer.class.getName())){
            stopService(new Intent(this, LocationServer.class));
        }
        if(Utils.isServiceExisted(LoginActivity.this, LongRunningService.class.getName())){
            stopService(new Intent(this, LongRunningService.class));
        }
    }

    @Event(value = {R.id.setting, R.id.login_main_interface}, type = View.OnClickListener.class)
    private void btnClick(View v) {
        switch (v.getId()) {
            case R.id.setting:
                if (!DoubleClickUtils.isFastDoubleClick()) {
                    intent = new Intent(LoginActivity.this, GatewayAddressActivity1.class);
                    startActivity(intent);
                    finish();
                }
                break;
            case R.id.login_main_interface:
                if (!DoubleClickUtils.isFastDoubleClick()) {
                    if (userName.getText().toString().trim().length() <= 0) {
                        toast("请输入用户名");
                    } else if (passWord.getText().toString().trim().length() < 1) {
                        toast("密码少于1位");
                    } else if (!NetWorkUtil.isNetworkConnected(this)) {
                        toast("无网络");
                    } else if ("".equals(prefs.getString("add", ""))) {
                        // 如果SP文件里面地址为空字符串的话就直接赋值http://222.173.103.228:10034/
                        loadding.show("正在加载中...");
                        // 登录界面网络请求
                        getData(CommenUrl.iPSetString);
                    } else {
                        // 如果用户名不为空、密码不为空、网络存在、SP文件里面地址不为空字符串的话，
                        // 就将网关地址赋值为add
                        add = prefs.getString("add", "");
                        loadding.show("正在加载中...");
                        // 登录界面网络请求
                        getData(add);
                    }
                }
                break;
        }
    }

    // 登录界面网络请求
    private void getData(String string1) {
        list1.clear();
        String usernameValue = userName.getText().toString().trim();
        String passwordValue = passWord.getText().toString().trim();
        RequestParams params = new RequestParams(string1 + "Service/C_WNMS_API.asmx/LogIn");
        TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        params.addBodyParameter("name", usernameValue);
        params.addBodyParameter("password", passwordValue);
        //867323020957815   tm.getDeviceId() 867677028633517
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        params.addBodyParameter("IMEI", tm.getDeviceId());
        x.http().post(params, new org.xutils.common.Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {}

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
                /*if (loaddingTest.isShow()) {
                    loaddingTest.close();
                }*/
            }

            @Override
            public void onSuccess(String arg0) {
                try {
                    CommonResponseBean commonResponseBean = new CommonResponseBean().getcommenHit(arg0);
                    //Gson gson = new Gson();
                    //User user = gson.fromJson(commonResponseBean.getData(), User.class);
                    //Log.e(TAG, "onSuccess: " + user);
                    // 这句代码的作用是在cache偏好文件里面保存key为user，value为登录成功返回数据中的data
                    // 的值
                    UserInfoUtils.getInstance().saveUserInfoString(LoginActivity.this, commonResponseBean.getData());
                    if (commonResponseBean.getCode() == 1) {
                        editor.putString("authorityrole", commonResponseBean.getRole());
                        editor.putInt("UserID", new JSONObject(commonResponseBean.getData()).getInt("UserID")); // 用户id
                        // 如果用户名和prefs里面的username一致就继续进行if判断之外的代码，如果不一致
                        // 就将prefs2里的deviceId赋值为“”,这样在不同的用户名登录之后就会提示
                        // “请先点击左上角按钮选择泵站名称”。还要将comName赋值为“”，这样在不同的
                        // 用户名登录之后首页的标题不会显示上一个用户的设备
                        if(!userName.getText().toString().equals(prefs.getString("username", ""))){
                            editor2.putString("deviceId", "");
                            editor2.putString("comName", "");
                            editor2.putString("EquipmentNo", "");
                        }
                        editor.putInt("Role", new JSONObject(commonResponseBean.getData()).getInt("Role"));  // 用户角色
                        // guid 用户判断过时
                        editor.putString("guid", new JSONObject(arg0).getString("guid"));
                        editor.putString("username", userName.getText().toString().trim());
                        editor.putString("userfullname", new JSONObject(commonResponseBean.getData()).getString("UserFullName"));
                        editor.putString("RegionNo", commonResponseBean.getRegionNo());
                        if (isRemember) {
                            editor.putBoolean("ischeck", isRemember);
                            editor.putString("userpass", passWord.getText().toString().trim());
                        } else {
                            editor.putBoolean("ischeck", isRemember);
                            editor.putString("userpass", "");
                        }
                        // 这两条字段是用于操作界面判断可否进行远程操作
                        editor.putString("AdministratorName", "");
                        editor.putString("AdministratorPassword", "");
                        editor.apply();
                        editor2.apply();
                        intent.setClass(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        //toast("登录成功,请在左上角选择相应设备");
                        finish();
                        // 极光推送设置别名
                        TagAliasOperatorHelper.TagAliasBean tagAliasBean = new TagAliasOperatorHelper.TagAliasBean();
                        tagAliasBean.action = ACTION_SET; // ACTION_SET等于2
                        sequence++;
                        // 别名改变是为了区分漳州水务和龙华水务
                        tagAliasBean.alias = commonResponseBean.getSoft_key() + "_" +
                                new JSONObject(commonResponseBean.getData()).getString("UserID");
                        tagAliasBean.isAliasAction = true;
                        TagAliasOperatorHelper.getInstance().handleAction(getApplicationContext(), sequence, tagAliasBean);

                        initTrace();
                        // 给王珂上传坐标，有了initTrace()就不用下面这行代码了，initTrace()里面包含这句代码
                        //LoginActivity.this.startService(new Intent(LoginActivity.this, LocationServer.class));
                    } else if (commonResponseBean.getCode() == 2) {
                        if (loadding.isShow()) {
                            loadding.close();
                        }
                        loaddingTest.showNoCancelable("系统正在审核，请稍候...");
                        handler.postDelayed(runnable, 6000);
                    } else {
                        toast(commonResponseBean.getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (loadding.isShow()) {
                    loadding.close();
                }
                /*if (loaddingTest.isShow()) {
                    loaddingTest.close();
                }*/
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermission() {
        // 申请权限
        //PermissionsDispatcherActivityPermissionsDispatcher.openCameraWithCheck(this);
        LoginActivityPermissionsDispatcher.getPermissionsWithCheck(this);
    }

    @NeedsPermission({Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.NFC,
            Manifest.permission.READ_PHONE_STATE})
        // coarse  粗糙的  external  外部的 storage 存储
        // Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE,
        // Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION
    void getPermissions() { }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LoginActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode,
                grantResults);
    }

    @OnShowRationale({Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.NFC,
            Manifest.permission.READ_PHONE_STATE})
    void showRationale(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage("申请相关权限")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 再次执行请求
                        request.proceed();
                    }
                })
                .show();
    }

    @OnPermissionDenied({Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.NFC,
            Manifest.permission.READ_PHONE_STATE})
    void permissionsDenied() {
        toast("权限被拒绝，软件部分功能可能会失效！");
    }

    @OnNeverAskAgain({Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.NFC,
            Manifest.permission.READ_PHONE_STATE})
    void neverAskAgain() {
        toast("权限被拒绝，软件部分功能可能会失效！");
    }

    private void toast(String text){
        Toast.makeText(LoginActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    // 百度鹰眼轨迹
    private void initTrace() {
        // 请求参数
        RequestParams params = new RequestParams("http://yingyan.baidu.com/api/v3/entity/add");
        String entity_name = String.valueOf(getSharedPreferences("UserInfo", 0).getInt("UserID", 0));
        params.addBodyParameter("ak", "vzK9L9kahlLVsjh94NjGmOrYsiIP5Ugp");
        params.addBodyParameter("service_id","205193");
        params.addBodyParameter("entity_name", entity_name);
        params.addBodyParameter("mcode", "BE:E2:A3:09:0E:18:69:54:7D:03:16:44:EB:D4:73:2F:51:F4:61:57;com.example.shenzhen");
        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                //toast(ex.getMessage());
            }

            @Override
            public void onFinished() { }

            @Override
            public void onSuccess(String arg0) {
                Log.e("jsonTrace", arg0);
                try {
                    JSONObject jsonObject = new JSONObject(arg0);
                    if (jsonObject.getInt("status") == 0 || jsonObject.getInt("status") == 3005) {
                        // 检查系统是否开启了地理位置权限;
                        // 注意：此时的Manifest的导入包路径import android.Manifest;
                        LoginActivity.this.startService(new Intent(LoginActivity.this, LocationServer.class));
                    } else {
                        toast(jsonObject.getString("message"));
                        Log.e("initTrace", jsonObject.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStart() {
        super.onStart();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (loaddingTest.isShow()) {
            loaddingTest.close();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
