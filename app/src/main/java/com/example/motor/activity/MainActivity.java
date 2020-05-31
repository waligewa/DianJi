package com.example.motor.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.adapter.MainViewAdapter;
import com.example.motor.base.CustemInfo;
import com.example.motor.constant.ConstantsField;
import com.example.motor.doubleprocess.LocalService;
import com.example.motor.doubleprocess.RemoteService;
import com.example.motor.fragment.HomeFragment;
import com.example.motor.fragment.MineFragment;
import com.example.motor.fragment.OperateFragment;
import com.example.motor.fragment.ServiceFragment;
import com.example.motor.jobschedule.MyJobService;
import com.example.motor.keepliveactivity.KeepLiveManager;
import com.example.motor.listener.OnTabSelectedListener;
import com.example.motor.nfc.ByteArrayChange;
import com.example.motor.service.LongRunningService;
import com.example.motor.service.MyService;
import com.example.motor.util.ActionSheetDialog;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.ExampleUtil;
import com.example.motor.util.MyAlertDialog;
import com.example.motor.util.SPUtils;
import com.example.motor.widget.Tab;
import com.example.motor.widget.TabContainerView;
import com.example.motor.workorderlist.inspection.InspectionWorkOrderActivity;
import com.example.motor.workorderlist.repair.RepairWorkOrderActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
//import com.pgyersdk.javabean.AppBean;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static MainActivity instance2 = null;
    public static boolean isForeground = false;
    private Intent intent;
    public static final int SCAN_QR_CODE = 3;
    private String username;
    private SharedPreferences prefs1, prefs2;
    private Activity mActivity;

    // nfc相关
    private NfcAdapter nfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private String info = "";
    private List<String> result = new ArrayList<>();
    private List<String> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_container);
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);
        instance2 = this;
        init();
        initNFC();

        new PgyUpdateManager.Builder()
                .setForced(false)                                                                   //设置是否强制更新
                .setUserCanRetry(false)                                                             //失败后是否提示重新下载
                .setDeleteHistroyApk(true)                                                          //检查更新前是否删除本地历史 Apk
                .register();
        /*PgyUpdateManager.register(mActivity, new UpdateManagerListener() {
            @Override
            public void onUpdateAvailable(final String result) {
                // 将新版本信息封装到AppBean中
                final AppBean appBean = getAppBeanFromString(result);
                new MyAlertDialog(mActivity)
                        .builder()
                        .setTitle("更新软件")
                        .setMsg("请点击“确定”按钮下载新版本")
                        .setPositiveButton("确定", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startDownloadTask( MainActivity.this, appBean.getDownloadURL());
                            }
                        })
                        .show();
            }
            @Override
            public void onNoUpdateAvailable() { }
        });*/

        //解除注册
        //PgyUpdateManager.unregister();
    }

    private void init(){
        prefs1 = getSharedPreferences("UserInfo", 0);
        prefs2 = getSharedPreferences("device", Context.MODE_PRIVATE);
        intent = new Intent();
        mActivity = this;
        username = prefs1.getString("username", "");

        // 控件的初始化，然后加上适配器，控件的点击事件
        TabContainerView tabContainerView = (TabContainerView) findViewById(R.id.tab_container);
        MainViewAdapter mainViewAdapter = new MainViewAdapter(getSupportFragmentManager(),
                new Fragment[]{new HomeFragment(), new OperateFragment(), new ServiceFragment(), new MineFragment()});
        mainViewAdapter.setHasMsgIndex(0);
        tabContainerView.setAdapter(mainViewAdapter);
        tabContainerView.setOnTabSelectedListener(new OnTabSelectedListener() {
            @Override
            public void onTabSelected(Tab tab) {
                Log.e("Fuck the code!", tab.getIndex() + "");
                if (tab.getIndex() == 0) {
                    //HomeFragment.speechInput.setText("");
                }
            }
        });
        // 极光推送的广播接收器
        registerMessageReceiver();
        // 有网无网判断的广播接收器
        //registerMessageReceiver2();
        // 利用activity提升权限
        //keepLive();
        // 双进程守护
        //doubleProcess();
        // 启动长时间后台定时服务
        longRunning();
        // 使用job schedule拉活
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            // 如果小于5.0，就直接返回
            return;
        } else {
            //jobSchedule();
        }
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
            // 转换为字符串
            String equNo = change(tag);
            //toast("识别成功设备编号:" + equNo);
            // 如果设备编号为null的话就直接return，如果接着往下走就要闪退了
            if(equNo == null) return;
            if (equNo.length() > 0) {

                //TODO  拿到IC卡里携带的设备编号之后要执行的操作
                Long d = Long.valueOf(equNo); // 将字符串转成long型
                // 根据设备编号获取设备数据
                getData(String.valueOf(d));
                //toast("识别结果:" + String.valueOf(d));
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
        String changeInfo = "";
        String Ascll = "";
        // Enable I/O operations to the tag from this TagTechnology object.
        try {
            mfc.connect();
            // authenticate认证 sector扇形
            auth = mfc.authenticateSectorWithKeyA(1, MifareClassic.KEY_DEFAULT); // 非常重要---------------------------
            if (auth) {
                Log.e("change的auth验证成功", "开始读取模块信息");
                byte[] data = mfc.readBlock(4 * 1 + 1);//--------------
                // Byte数组转换为16进制字符串
                changeInfo = ByteArrayChange.ByteArrayToHexString(data);
                //String temp = ToStringHex.decode(ChangeInfo);
                //Ascll = ToStringHex.decode(changeInfo); // 将16进制数字解码成字符串,适用于所有字符（包括中文）
                //Log.e("彬彬", changeInfo);
                return changeInfo;
            }
        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        } finally {
            try {
                if(mfc == null) return null;
                mfc.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

    // 利用activity提升权限
    public void keepLive() {
        KeepLiveManager.getInstance().registerKeepLifeReceiver(this);
    }

    // 使用job schedule拉活
    public void jobSchedule() {
        MyJobService.startJob(this);
    }

    // 双进程守护
    public void doubleProcess() {
        startService(new Intent(this, LocalService.class));
        startService(new Intent(this, RemoteService.class));
    }

    // 启动长时间后台定时服务
    public void longRunning(){
        startService(new Intent(this, LongRunningService.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SCAN_QR_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String result = bundle.getString("result");
                    toast("设备编号为:" + result);
                }
                break;
        }
    }

    // 连续点击两次返回键，退出应用程序
    private long firstTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                long secondTime = System.currentTimeMillis();
                // 如果两次按键的时间间隔大于2秒，则不退出
                if (secondTime - firstTime > 2000) {
                    toast("再按一次退出程序");
                    firstTime = secondTime;// 更新firstTime
                    return true;
                } else {
                    MyApplication.getInstance().exit();
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    // for receive customer msg from jpush server
    private MessageReceiver mMessageReceiver;
    public static final String MESSAGE_RECEIVED_ACTION = "com.example.shenzhen.MESSAGE_RECEIVED_ACTION";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_EXTRAS = "extras";

    // 极光推送的广播接收器
    public void registerMessageReceiver() {
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY); // priority优先权
        filter.addAction(MESSAGE_RECEIVED_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);
    }

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
                    String messge = intent.getStringExtra(KEY_MESSAGE);
                    String extras = intent.getStringExtra(KEY_EXTRAS);
                    StringBuilder showMsg = new StringBuilder();
                    showMsg.append(KEY_MESSAGE + " : " + messge + "\n");
                    if (!ExampleUtil.isEmpty(extras)) {
                        showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
                    }
                    toast(showMsg.toString());
                }
            } catch (Exception e) { }
        }
    }

    private NetWorkStateReceiever m;

    // 有网无网判断的广播接收器
    public void registerMessageReceiver2() {
        m = new NetWorkStateReceiever();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(m, filter);
    }

    public class NetWorkStateReceiever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isAvailable()){
                //toast("有网络");
                Intent i = new Intent(MainActivity.this, MyService.class);
                startService(i);
            } else {
                //toast("无网络");
            }
        }
    }

    // 根据设备编号获取设备数据
    private void getData(String text) {
        // 请求参数
        String address = getSharedPreferences("UserInfo", 0).getString("add", "");
        RequestParams params;
        if(TextUtils.isEmpty(SPUtils.getStringData(mActivity, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadRtuData");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/LoadRtuData");
        }
        String guidString = getSharedPreferences("UserInfo", 0).getString("guid", "");
        int userId = getSharedPreferences("UserInfo", 0).getInt("UserID", 0);
        params.addBodyParameter("userId", String.valueOf(userId));
        params.addBodyParameter("equNo", text);
        params.addBodyParameter("pageSize", "20");
        params.addBodyParameter("pageIndex", "1");
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
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        Intent intent = new Intent(mActivity, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")) {
                        Type listType = new TypeToken<List<CustemInfo>>() {}.getType();
                        Gson gson = new Gson();
                        List<CustemInfo> list = gson.fromJson(object1.getString("Data"), listType);
                        CustemInfo value = list.get(0);
                        SharedPreferences.Editor editor = prefs2.edit();
                        editor.putString("deviceId", value.getId());
                        editor.putString("comAddress", value.getComAddress());
                        editor.putString("comName", value.getDeviceName());
                        editor.putString("EquipmentNo", value.getEquipmentNo());
                        editor.putString("EquipmentID", value.getEquipmentID());
                        editor.putString("EquipmentType", value.getEquipmentType());
                        editor.putString("Latitude", value.getLatitude());
                        editor.putString("Longitude", value.getLongitude());
                        editor.apply();
                        HomeFragment.titleTextView.setText(prefs2.getString("comName", "首页").equals("") ? "首页" :
                                 prefs2.getString("comName", "首页"));
                        new ActionSheetDialog(mActivity)
                                .builder()
                                .setCancelable(false)
                                .setCanceledOnTouchOutside(false)
                                .setTitle("请选择")
                                .addSheetItem("维修", ActionSheetDialog.SheetItemColor.Red,
                                        new ActionSheetDialog.OnSheetItemClickListener() {
                                            @Override
                                            public void onClick(int which) {
                                                intent.setClass(mActivity, RepairWorkOrderActivity.class);
                                                startActivity(intent);
                                            }
                                        })
                                .addSheetItem("巡检", ActionSheetDialog.SheetItemColor.Red,
                                        new ActionSheetDialog.OnSheetItemClickListener() {
                                            @Override
                                            public void onClick(int which) {
                                                intent.setClass(mActivity, InspectionWorkOrderActivity.class);
                                                startActivity(intent);
                                            }
                                        })
                                .show();
                    } else {
                        toast(object1.getString("Message"));
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    private void toast(String text) {
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        isForeground = true;
        super.onResume();
        // 设置处理优于所有其他NFC的处理
        if (nfcAdapter != null && nfcAdapter.isEnabled())
            nfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    @Override
    protected void onPause() {
        isForeground = false;
        super.onPause();
        // 恢复默认状态
        if (nfcAdapter != null) nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        unregisterReceiver(m);
        KeepLiveManager.getInstance().unregisterKeepLiveReceiver(this);
    }
}
