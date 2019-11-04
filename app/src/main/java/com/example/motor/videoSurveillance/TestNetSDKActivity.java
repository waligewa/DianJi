package com.example.motor.videoSurveillance;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.company.NetSDK.CB_fDisConnect;
import com.company.NetSDK.CB_fHaveLogin;
import com.company.NetSDK.CB_fHaveReConnect;
import com.company.NetSDK.CB_fMessageCallBack;
import com.company.NetSDK.CB_fSubDisConnect;
import com.company.NetSDK.CFG_CAP_ALARM_INFO;
import com.company.NetSDK.CFG_DSPENCODECAP_INFO;
import com.company.NetSDK.DEV_PLAY_RESULT;
import com.company.NetSDK.FinalVar;
import com.company.NetSDK.INetSDK;
import com.company.NetSDK.LOG_SET_PRINT_INFO;
import com.company.NetSDK.NET_DEVICEINFO;
import com.company.NetSDK.NET_DEVICEINFO_Ex;
import com.company.NetSDK.NET_IN_MEMBERNAME;
import com.company.NetSDK.NET_OUT_MEMBERNAME;
import com.company.NetSDK.NET_PARAM;
import com.company.NetSDK.SDKDEV_DSP_ENCODECAP_EX;
import com.company.NetSDK.SDK_DEV_ENABLE_INFO;
import com.company.NetSDK.SDK_DEV_FUNC;
import com.company.NetSDK.SDK_PRODUCTION_DEFNITION;
import com.company.NetSDK.SDK_SYS_ABILITY;
import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.util.DoubleClickUtils;
import com.example.motor.util.Loadding;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import static com.example.motor.activity.VideoSurveillanceActivity.data1;
import static com.example.motor.videoSurveillance.GlobalSettingActivity.m_nGlobalChn;

public class TestNetSDKActivity extends Activity {

    @ViewInject(R.id.back)
    LinearLayout back;
    @ViewInject(R.id.ip_address)
    EditText ipAddress;
    @ViewInject(R.id.port)
    EditText port;
    @ViewInject(R.id.username)
    EditText username;
    @ViewInject(R.id.password)
    EditText password;
    @ViewInject(R.id.login)
    TextView login;
    @ViewInject(R.id.channel)
    Spinner channel;
    private ListView monitors;
    private Resources res;
    private MonitorInfo2 monitorInfo;
    private List<String> data = new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    static long m_loginHandle = 0;
    static NET_DEVICEINFO deviceInfo;
    static boolean m_speedCtrl;
    static int m_schedule;
    static int nStreaMask = 0;

    private SDKDEV_DSP_ENCODECAP_EX stEncodeCapOld = new SDKDEV_DSP_ENCODECAP_EX();
    private CFG_DSPENCODECAP_INFO stEncodeCapNew = new CFG_DSPENCODECAP_INFO();

    private int nSpecCap = 20;

    static CFG_CAP_ALARM_INFO stCfgCapAlarm = new CFG_CAP_ALARM_INFO();

    private int nExtraChnNum;
    private int nExtraAlarmOutPortNum;
    private Intent intent;
    //private String position;
    private Loadding loadding;

    public class DeviceDisConnect implements CB_fDisConnect {
        @Override
        public void invoke(long lLoginID, String pchDVRIP, int nDVRPort) {
            ToolKits.writeLog("Device " + pchDVRIP + " DisConnect!");
            Intent intent = new Intent();
            intent.setAction(TestInterfaceActivity.DISCONNECTED_BROAST);        //设置Action
            sendBroadcast(intent);                  //发送Intent
            return;
        }
    }

    public class DeviceReConnect implements CB_fHaveReConnect {
        @Override
        public void invoke(long lLoginID, String pchDVRIP, int nDVRPort) {
            ToolKits.writeLog("Device " + pchDVRIP + " ReConnect!");
            Intent intent = new Intent();
            intent.setAction(TestInterfaceActivity.AUTOCONNECTED_BROAST);        //设置Action
            sendBroadcast(intent);                  //发送Intent
        }
    }

    public class DeviceSubDisConnect implements CB_fSubDisConnect {  //  替补的
        @Override
        public void invoke(int emInterfaceType, boolean bOnline,
                           long lOperateHandle, long lLoginID) {

            ToolKits.writeLog("Device SubConnect DisConnect");
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_net_sdk);
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);
        intent = new Intent();
        loadding = new Loadding(this);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i < data1.size() - 1; i++){
                    for(int j = data1.size() - 1; j > i; j--)  {
                        if (data1.get(j).getIP().equals(data1.get(i).getIP())){
                            data1.remove(j);
                        }
                    }
                }
                finish();
            }
        });
        ArrayList<String> alChn = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            alChn.add("通道" + i);
        }
        ArrayAdapter<String> aaChn = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, alChn);
        channel.setAdapter(aaChn);
        channel.setSelection(m_nGlobalChn);
        channel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                m_nGlobalChn = channel.getSelectedItemPosition();
                GlobalSettingActivity.m_nGlobalPbStream = 0;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
        monitorInfo = (MonitorInfo2) getIntent().getSerializableExtra("monitorInfo");
        ipAddress.setText(monitorInfo.getIP());
        port.setText(monitorInfo.getPort());
        username.setText(monitorInfo.getEquName());
        password.setText(monitorInfo.getEquPwd());
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!DoubleClickUtils.isFastDoubleClick()){
                    loadding.show("正在加载中...");
                    if (m_loginHandle != 0) {
                        INetSDK.Logout(m_loginHandle);
                        m_loginHandle = 0;
                    }

                    deviceInfo = new NET_DEVICEINFO();
                    Integer error = new Integer(0);

                    DeviceReConnect reConnect = new DeviceReConnect();
                    INetSDK.SetAutoReconnect(reConnect);

                    DeviceSubDisConnect subDisConnect = new DeviceSubDisConnect();
                    INetSDK.SetSubconnCallBack(subDisConnect);

                    INetSDK.SetDVRMessCallBack(new Test_CB_fMessageCallBack());  //  数字视频录像机dvr

                    String strIp = "", strPort = "", strUser = "", strPassword = "";
                    strIp = ipAddress.getText().toString().trim();
                    strPort = port.getText().toString().trim();
                    strUser = username.getText().toString().trim();
                    strPassword = password.getText().toString().trim();

                    int nDevPort = 37777;
                    try {
                        nDevPort = Integer.parseInt(strPort);
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToolKits.showMessage(v.getContext(), res.getString(R.string.login_activity_port_err));
                        return;
                    }

                    //boolean bOptimeze = INetSDK.SetOptimizeMode(EM_OPTIMIZE_TYPE.EM_OPT_TYPE_MOBILE_V1, null);

                    m_loginHandle = INetSDK.LoginEx(strIp, nDevPort, strUser, strPassword, nSpecCap,null, deviceInfo, error);

                    if (m_loginHandle != 0) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Looper.prepare();
                                // stream Type
                                if (INetSDK.QueryDevState(TestNetSDKActivity.m_loginHandle,
                                        FinalVar.SDK_DEVSTATE_DSP_EX, stEncodeCapOld,3000)) {
                                    nStreaMask = stEncodeCapOld.dwStreamCap;
                                } else if (ToolKits.GetDevConfig(FinalVar.CFG_CMD_HDVR_DSP,
                                        stEncodeCapNew, TestNetSDKActivity.m_loginHandle,
                                        GlobalSettingActivity.m_nGlobalChn, 1024 * 70)) {
                                    nStreaMask = stEncodeCapNew.dwStreamCap;
                                }
                                Looper.loop();
                            }
                        }).start();

                        ToolKits.showMessage(v.getContext(), res.getString(R.string.login_activity_login_success));

                        NET_IN_MEMBERNAME inMember = new NET_IN_MEMBERNAME();
                        inMember.szCommand = new String();
                        NET_OUT_MEMBERNAME outMember = new NET_OUT_MEMBERNAME(50, 260);
                        boolean zMemberRet = INetSDK.GetMemberNames(m_loginHandle, inMember, outMember,3000);

                        m_speedCtrl = false;
                        m_schedule = 0;
                        SDK_DEV_ENABLE_INFO stEnableInfo = new SDK_DEV_ENABLE_INFO();
                        if (INetSDK.QuerySystemInfo(TestNetSDKActivity.m_loginHandle,
                                SDK_SYS_ABILITY.ABILITY_DEVALL_INFO, stEnableInfo, 3000)) {//  ability能力
                            if (stEnableInfo.IsFucEnable[SDK_DEV_FUNC.EN_PLAYBACK_SPEED_CTRL] != 0) {
                                m_speedCtrl = true;
                            }
                            m_schedule = stEnableInfo.IsFucEnable[SDK_DEV_FUNC.EN_SCHEDULE];
                        }

                        stCfgCapAlarm = new CFG_CAP_ALARM_INFO();
                        char szOutBuffer[] = new char[10240];
                        Integer stError = new Integer(0);
                        boolean bQN = INetSDK.QueryNewSystemInfo(m_loginHandle, FinalVar.CFG_CAP_ALARM,
                                0, szOutBuffer, stError,5000);
                        if (bQN) {
                            bQN = INetSDK.ParseData(FinalVar.CFG_CAP_ALARM, szOutBuffer, stCfgCapAlarm, null);
                            if (!bQN) {
                                ToolKits.writeErrorLog("INetSDK.ParseData CFG_CAP_ALARM error");
                            }
                        } else {
                            ToolKits.writeErrorLog("INetSDK.QueryNewSystemInfo CFG_CAP_ALARM error");
                        }

                        //TestNetSDKActivity.deviceInfo在activity直接使用出现过问题，改用putextra方式
                        nExtraChnNum = TestNetSDKActivity.deviceInfo.byChanNum;
                        if (-1 == TestNetSDKActivity.deviceInfo.byChanNum) {
                            SDK_PRODUCTION_DEFNITION stDef = new SDK_PRODUCTION_DEFNITION();  //  production 生产  definition  定义
                            boolean bRet = INetSDK.QueryProductionDefinition(TestNetSDKActivity.m_loginHandle, stDef, 3000);
                            if (bRet) {
                                nExtraChnNum = stDef.nVideoInChannel + stDef.nMaxRemoteInputChannels;
                            }
                        }
                        nExtraAlarmOutPortNum = TestNetSDKActivity.deviceInfo.byAlarmOutPortNum;

                        jumpToContentListActivity();
                    } else {
                        System.out.println(error);
                        ToolKits.showErrorMessage(v.getContext(), res.getString(R.string.login_activity_login_failed));
                    }
                }
            }
        });
        // 一般不需要这个函数，偶尔出现过程序退入后台较长时间，无法找到native方法的情况
        INetSDK.LoadLibrarys();

        res = this.getResources();
        //shardPreferences = this.getPreferences(Activity.MODE_WORLD_READABLE);

        ToolKits.showMessage(this, res.getString(R.string.DemoInit));

        // NetSDK 动态库日志
        LOG_SET_PRINT_INFO logInfo = new LOG_SET_PRINT_INFO();
        logInfo.bSetFilePath = true;
        String file = new String("/mnt/sdcard/sdk_log.log");
        System.arraycopy(file.getBytes(),0, logInfo.szLogFilePath,0, file.length());
        INetSDK.LogOpen(logInfo);

        DeviceDisConnect disConnect = new DeviceDisConnect();  //  disconnect 断开
        boolean zRet = INetSDK.Init(disConnect);

        INetSDK.SetConnectTime(4000, 3);

        NET_PARAM stNetParam = new NET_PARAM();
        stNetParam.nWaittime = 3000;   //  函数等待超时时间
        stNetParam.nSearchRecordTime = 30000;  //  录像回放超时时间
        INetSDK.SetNetworkParam(stNetParam);

        // 34317138 -> 3.43.17138
        int dbVersion = INetSDK.GetSDKVersion();
        int nBig = dbVersion / 10000000;
        int nMid = (dbVersion - (nBig * 10000000)) / 100000;
        int nRev = dbVersion - (nBig * 10000000) - (nMid * 100000);
        nSpecCap = 20;
        //getData();
        /*monitors = (ListView) findViewById(R.id.lv_monitor);
        monitors.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //  这个if语句的作用是禁止连续点击
                if(!DoubleClickUtils.isFastDoubleClick()){
                    if (m_loginHandle != 0) {
                        INetSDK.Logout(m_loginHandle);
                        m_loginHandle = 0;
                    }

                    deviceInfo = new NET_DEVICEINFO();
                    Integer error = new Integer(0);

                    DeviceReConnect reConnect = new DeviceReConnect();
                    INetSDK.SetAutoReconnect(reConnect);

                    DeviceSubDisConnect subDisConnect = new DeviceSubDisConnect();
                    INetSDK.SetSubconnCallBack(subDisConnect);

                    INetSDK.SetDVRMessCallBack(new Test_CB_fMessageCallBack());  //  数字视频录像机dvr

                    String strIp = "", strPort = "", strUser = "", strPassword = "";
                    if (position == 0) {
                        strIp = monitorInfo.getIP();
                        strPort = monitorInfo.getPort();
                        strUser = monitorInfo.getEquName();
                        strPassword = monitorInfo.getEquPwd();
                    } else if (position == 1) {
                        strIp = monitorInfo.getIP2();
                        strPort = monitorInfo.getPort2();
                        strUser = monitorInfo.getEquName2();
                        strPassword = monitorInfo.getEquPwd2();
                    } else if (position == 2) {
                        strIp = monitorInfo.getIP3();
                        strPort = monitorInfo.getPort3();
                        strUser = monitorInfo.getEquName3();
                        strPassword = monitorInfo.getEquPwd3();
                    }

                    int nDevPort = 37777;
                    try {
                        nDevPort = Integer.parseInt(strPort);
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToolKits.showMessage(v.getContext(), res.getString(R.string.login_activity_port_err));
                        return;
                    }

                    //  boolean bOptimeze = INetSDK.SetOptimizeMode(EM_OPTIMIZE_TYPE.EM_OPT_TYPE_MOBILE_V1, null);

                    m_loginHandle = INetSDK.LoginEx(strIp, nDevPort,
                            strUser, strPassword, nSpecCap,null, deviceInfo, error);

                    if (m_loginHandle != 0) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Looper.prepare();
                                // stream Type
                                if (INetSDK.QueryDevState(TestNetSDKActivity.m_loginHandle,
                                        FinalVar.SDK_DEVSTATE_DSP_EX, stEncodeCapOld,3000)) {
                                    nStreaMask = stEncodeCapOld.dwStreamCap;
                                } else if (ToolKits.GetDevConfig(FinalVar.CFG_CMD_HDVR_DSP,
                                        stEncodeCapNew, TestNetSDKActivity.m_loginHandle,
                                        GlobalSettingActivity.m_nGlobalChn, 1024 * 70)) {
                                    nStreaMask = stEncodeCapNew.dwStreamCap;
                                }
                                Looper.loop();
                            }
                        }).start();

                        ToolKits.showMessage(v.getContext(), res.getString(R.string.login_activity_login_success));

                        NET_IN_MEMBERNAME inMember = new NET_IN_MEMBERNAME();
                        inMember.szCommand = new String();
                        NET_OUT_MEMBERNAME outMember = new NET_OUT_MEMBERNAME(50, 260);
                        boolean zMemberRet = INetSDK.GetMemberNames(m_loginHandle, inMember, outMember,3000);

                        m_speedCtrl = false;
                        m_schedule = 0;
                        SDK_DEV_ENABLE_INFO stEnableInfo = new SDK_DEV_ENABLE_INFO();
                        if (INetSDK.QuerySystemInfo(TestNetSDKActivity.m_loginHandle,
                                SDK_SYS_ABILITY.ABILITY_DEVALL_INFO, stEnableInfo, 3000)) {//  ability能力
                            if (stEnableInfo.IsFucEnable[SDK_DEV_FUNC.EN_PLAYBACK_SPEED_CTRL] != 0) {
                                m_speedCtrl = true;
                            }
                            m_schedule = stEnableInfo.IsFucEnable[SDK_DEV_FUNC.EN_SCHEDULE];
                        }

                        stCfgCapAlarm = new CFG_CAP_ALARM_INFO();
                        char szOutBuffer[] = new char[10240];
                        Integer stError = new Integer(0);
                        boolean bQN = INetSDK.QueryNewSystemInfo(m_loginHandle, FinalVar.CFG_CAP_ALARM,
                                0, szOutBuffer, stError,5000);
                        if (bQN) {
                            bQN = INetSDK.ParseData(FinalVar.CFG_CAP_ALARM, szOutBuffer, stCfgCapAlarm, null);
                            if (!bQN) {
                                ToolKits.writeErrorLog("INetSDK.ParseData CFG_CAP_ALARM error");
                            }
                        } else {
                            ToolKits.writeErrorLog("INetSDK.QueryNewSystemInfo CFG_CAP_ALARM error");
                        }

                        //  TestNetSDKActivity.deviceInfo在activity直接使用出现过问题，改用putextra方式
                        nExtraChnNum = TestNetSDKActivity.deviceInfo.byChanNum;
                        if (-1 == TestNetSDKActivity.deviceInfo.byChanNum) {
                            SDK_PRODUCTION_DEFNITION stDef = new SDK_PRODUCTION_DEFNITION();  //  production 生产  definition  定义
                            boolean bRet = INetSDK.QueryProductionDefinition(TestNetSDKActivity.m_loginHandle, stDef, 3000);
                            if (bRet) {
                                nExtraChnNum = stDef.nVideoInChannel + stDef.nMaxRemoteInputChannels;
                            }
                        }
                        nExtraAlarmOutPortNum = TestNetSDKActivity.deviceInfo.byAlarmOutPortNum;

                        jumpToContentListActivity();
                    } else {
                        System.out.println(error);
                        ToolKits.showErrorMessage(v.getContext(), res.getString(R.string.login_activity_login_failed));
                    }
                }
            }
        });*/
    }

    /*private void Back(){
        finish();
    }*/

    /*private void getData() {
        // 请求参数EquID；guid
        RequestParams params = new RequestParams("http://47.93.6.250:10041/Service/" +
                "C_WNMS_API.asmx/getCameraSetting");
        String guidString = getSharedPreferences("UserInfo", 0).getString("guid", "");
        String EquID = getSharedPreferences("device", 0).getString("EquipmentID", "");
        if (!(EquID.isEmpty() && EquID.equals(""))) {
            params.addBodyParameter("EquID", EquID);
            params.addBodyParameter("guid", guidString);
            x.http().get(params, new Callback.CommonCallback<String>() {

                @Override
                public void onCancelled(CancelledException arg0) {
                }

                @Override
                public void onError(Throwable ex, boolean arg1) {
                    toast(ex.getMessage());
                }

                @Override
                public void onFinished() {
                }

                @Override
                public void onSuccess(String arg0) {
                    Log.e("json1ScanCode", arg0);
                    try {
                        JSONObject object1 = new JSONObject(arg0);
                        if (object1.getString("Code").equals("0")) {
                            Intent intent = new Intent(TestNetSDKActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                            toast(object1.getString("Message"));
                        }
                        if (object1.getString("Code").equals("1")) {
                            Gson gson = new Gson();
                            monitorInfo = gson.fromJson(object1.getString("Data"), MonitorInfo.class);
                            if (!monitorInfo.getEquName().isEmpty() && !monitorInfo.getEquName().equals("")) {
                                data.add(monitorInfo.getIP());
                            } else if (!monitorInfo.getEquName2().isEmpty() && !monitorInfo.getEquName2().equals("")) {
                                data.add(monitorInfo.getIP2());
                            } else if (!monitorInfo.getEquName3().isEmpty() && !monitorInfo.getEquName3().equals("")) {
                                data.add(monitorInfo.getIP3());
                            } else {
                                //  Back();
                                toast("该设备没有对应的监控设备!");
                            }
                            //  创建ArrayAdapter  android.R.layout.simple_expandable_list_item_1
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                    TestNetSDKActivity.this, R.layout.simple_list_item_text, R.id.text1, data);
                            //  绑定适配器
                            monitors.setAdapter(arrayAdapter);
                        } *//*else {
                            finish();
                        }*//*
                        monitorInfo = new MonitorInfo();
                        monitorInfo.setCameraID(12);
                        monitorInfo.setEquipmentID(1472215084);
                        monitorInfo.setIP("222.173.103.228");
                        monitorInfo.setPort("37777");
                        monitorInfo.setEquName("admin");
                        monitorInfo.setEquPwd("123123");
                        monitorInfo.setIP2("222.173.103.228");
                        monitorInfo.setPort2("10065");
                        monitorInfo.setEquName2("admin");
                        monitorInfo.setEquPwd2("admin");
                        monitorInfo.setIP3("222.173.103.228");
                        monitorInfo.setPort3("10063");
                        monitorInfo.setEquName3("admin");
                        monitorInfo.setEquPwd3("admin");
                        data.add("222.173.103.228");  //  ===========================
                        data.add("222.173.103.228");
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(  //  ===========================
                                TestNetSDKActivity.this, R.layout.simple_list_item_text, R.id.text1, data);
                        monitors.setAdapter(arrayAdapter);  //  ===========================
                        toast(object1.getString("Message"));
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
        } else {
            toast("请点击首页左上角按钮，选择相应设备");
        }
    }*/


    public void jumpToContentListActivity() {
        //Intent intent = new Intent();
        //intent.putExtra("name_nExtraChnNum", nExtraChnNum);
        //intent.putExtra("name_nExtraAlarmOutPortNum", nExtraAlarmOutPortNum);
        //intent.setClass(this, ContentListActivity.class);
        //startActivityForResult(intent, 2);
        // 对象item赋值，然后遍历data1，删除data1里面和monitorInfo一样的子项，然后添加新的子项item
        MonitorInfo2 item = new MonitorInfo2();
        item.setIP(ipAddress.getText().toString().trim());
        item.setPort(port.getText().toString().trim());
        item.setEquName(username.getText().toString().trim());
        item.setEquPwd(password.getText().toString().trim());
        // 这个for循环在18年11月21号出过bug加上后一个else if之后就没问题了，因为mi和monitorInfo比较之后移除一个子项，然后在选择界面选择
        // 不同的通道之后mi再和monitorInfo比较就不合适了，应该与itme进行比较，因为monitorInfo是固定死的，已经添加了一个新的子项，应该
        // 与新的子项进行比较
        for(int i = 0; i < data1.size(); i++){
            MonitorInfo2 mi = data1.get(i);
            if(mi.getIP().equals(monitorInfo.getIP())
                    && mi.getPort().equals(monitorInfo.getPort())
                    && mi.getEquName().equals(monitorInfo.getEquName())
                    && mi.getEquPwd().equals(monitorInfo.getEquPwd())){
                data1.remove(mi);
                if(i > 0) i--;
            } else if (mi.getIP().equals(item.getIP())
                    && mi.getPort().equals(item.getPort())
                    && mi.getEquName().equals(item.getEquName())
                    && mi.getEquPwd().equals(item.getEquPwd())){
                data1.remove(mi);
                if(i > 0) i--;
            }
        }
        for(int i = 0; i < data1.size() - 1; i++){
            for(int j = data1.size() - 1; j > i; j--)  {
                if (data1.get(j).getIP().equals(data1.get(i).getIP())){
                    data1.remove(j);
                }
            }
        }
        data1.add(item);
        Intent intent = new Intent();
        intent.setClass(this, LiveActivity.class);
        startActivityForResult(intent, 2);
        loadding.close();
    }

    public void Logout() {
        if (m_loginHandle == 0) {
            return;
        }
        boolean bResult = INetSDK.Logout(m_loginHandle);

        if (bResult == true) {
            m_loginHandle = 0;
        }
    }

    @Override
    protected void onDestroy() {
        Logout();
        INetSDK.Cleanup();
        ToolKits.showMessage(this, res.getString(R.string.DemoExit));
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        // reset channel
        // 回到此activity的时候会调用onResume方法，然后给默认成通道0，这样不科学，于是将其去掉
        /*GlobalSettingActivity.m_nGlobalChn = 0;
        GlobalSettingActivity.m_nGlobalPbStream = 0;*/
        super.onResume();
    }

    public class TestfHaveLogin implements CB_fHaveLogin {
        @Override
        public void invoke(long lLoginID, String pchDVRIP, int nDVRPort, boolean bOnline, NET_DEVICEINFO_Ex stuDeviceInfo, int nError) {
            ToolKits.writeLog("TestfHaveLogin");
        }
    }

    class Test_CB_fMessageCallBack implements CB_fMessageCallBack {
        @Override
        public boolean invoke(int lCommand, long lLoginID, Object obj, String pchDVRIP, int nDVRPort) {
            ToolKits.writeLog("Event: " + lCommand);
            if (12295 == lCommand) {
                DEV_PLAY_RESULT stResult = (DEV_PLAY_RESULT) obj;
                ToolKits.writeLog("ResultCode: " + stResult.dwResultCode + ", PlayHandle: " + stResult.lPlayHandle);
            }
            return true;
        }
    }

    // 手机返回按钮返回功能
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                /*// 冒泡排序删除IP地址相同的子项
                for(int i = 0; i < data1.size(); i++){
                    for(int j = 0; j < data1.size() - 1 - i; j++){
                        if(data1.get(i).getIP().equals(data1.get(j + 1).getIP())){
                            data1.remove(data1.get(j + 1));
                            if(j + 1 > 0) j--;
                        }
                    }
                }*/
                for(int i = 0; i < data1.size() - 1; i++){
                    for(int j = data1.size() - 1; j > i; j--)  {
                        if (data1.get(j).getIP().equals(data1.get(i).getIP())){
                            data1.remove(j);
                        }
                    }
                }
                finish();
                break;
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void toast(String text){
        Toast.makeText(TestNetSDKActivity.this, text, Toast.LENGTH_SHORT).show();
    }
}