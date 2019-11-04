package com.example.motor.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.navisdk.adapter.BNCommonSettingParam;
import com.baidu.navisdk.adapter.BNOuterLogUtil;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.example.motor.R;
import com.example.motor.adapter.DeviceStateAdapter;
import com.example.motor.baidu.BNDemoGuideActivity;
import com.example.motor.baidu.BNEventHandler;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.DeviceStateInfo;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DoubleClickUtils;
import com.example.motor.util.Loadding;
import com.example.motor.util.NetWorkUtil;
import com.example.motor.util.SPUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DeviceStateActivity extends Activity {

    @ViewInject(R.id.ic_a_title)
    TextView mtitleTextView;
    @ViewInject(R.id.gv_state)
    ListView mListView;
    // 18-09-25设定压力隐藏
    //@ViewInject(R.id.tv_pressure_set)
    //TextView mpressureSetTextView;
    @ViewInject(R.id.tv_pressure_out)
    TextView mpressureOutTextView;
    @ViewInject(R.id.tv_pressure_in)
    TextView mpressureInTextView;
    @ViewInject(R.id.tv_cumulative_flow) // cumulative  累积的
    TextView cumulativeFlowTextView;
    @ViewInject(R.id.tv_instantaneous_flow) // instantaneous  瞬时
    TextView instantaneousFlowTextView;
    @ViewInject(R.id.tv_accumulated_power_consumption)  // consumption  消费
    TextView accumulatedPowerConsumption;
    @ViewInject(R.id.tv_turbidity)
    TextView turbidity; // 浊度
    @ViewInject(R.id.tv_residual_chlorine)
    TextView residualChlorine; // 余氯
    @ViewInject(R.id.tv_ph_value)
    TextView phValue; // ph值
    @ViewInject(R.id.pump_or_pressure)
    TextView pumpOrPressure; // 设定压力或者设定流量
    @ViewInject(R.id.set_flow)
    TextView setFlow; // 设定流量或者设定压力
    @ViewInject(R.id.bt_state_map)
    ImageView bt_state_map;
    @ViewInject(R.id.state_data)
    LinearLayout state_data;
    @ViewInject(R.id.map)
    LinearLayout map;
    @ViewInject(R.id.mapView)
    MapView mMapView;
    @ViewInject(R.id.bt_map_go)
    Button mapGo;
    @ViewInject(R.id.start_stop_record)
    TextView startStopRecord;
    int Tag = 0;
    private DeviceStateAdapter madapter;
    private List<DeviceStateInfo> mInfos = new ArrayList<>();

    private MyAsyncTask task = null;
    private int TIME = 5 * 1000, userId;
    private boolean running = true;
    private String gatewayAddress, latitude, longitude, guidString, equNo;
    private double latitude2, longitude2;
    private SharedPreferences prefs1, prefs2;

    private LocationClient mLocationClient;
    private BaiduMap baiduMap;
    private MapStatusUpdate update;

    private static final String APP_FOLDER_NAME = "BNSDKSimpleDemo";
    private String mSDCardPath = null;
    public static final String ROUTE_PLAN_NODE = "routePlanNode";
    private final static String authBaseArr[] =
            { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION };
    private final static String authComArr[] = { Manifest.permission.READ_PHONE_STATE };
    private final static int authBaseRequestCode = 1; //认证
    private final static int authComRequestCode = 2;
    private boolean hasInitSuccess = false;
    private boolean hasRequestComAuth = false;
    private String authinfo = null;
    private BNRoutePlanNode.CoordinateType mCoordinateType = null;
    private Loadding loading;
    private Intent intent;
    private SplashHandler ttsHandler = new SplashHandler();

    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_device_state);
        x.view().inject(this);
        initView();
        BNOuterLogUtil.setLogSwitcher(true);
        showView();
        ttsHandler.sendEmptyMessageDelayed(3, 1000);
    }

    private void initView() {
        intent = new Intent();
        loading = new Loadding(this);
        prefs1 = getSharedPreferences("UserInfo", 0);
        prefs2 = getSharedPreferences("device", Context.MODE_PRIVATE);
        gatewayAddress = prefs1.getString("add", "");
        guidString = prefs1.getString("guid", "");
        userId = prefs1.getInt("UserID", 0);
        latitude = prefs2.getString("Latitude", "");
        longitude = prefs2.getString("Longitude", "");
        equNo = prefs2.getString("EquipmentNo", "");
        mtitleTextView.setText(prefs2.getString("comName", "设备状态").trim());
        madapter = new DeviceStateAdapter(this, mInfos);
        mListView.setAdapter(madapter);
        // 获取实时监控设备信息
        getData();
        baiduMap = mMapView.getMap();
        baiduMap.setMyLocationEnabled(true); // 开启这个功能，地图上面的那个小光标。
    }

    private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    // 内部TTS播报状态回传handler
    private class SplashHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BaiduNaviManager.TTSPlayMsgType.PLAY_START_MSG: {
                    break;
                }
                case BaiduNaviManager.TTSPlayMsgType.PLAY_END_MSG: {
                    break;
                }
                case 3:
                    if (initDirs()) {
                        initNavi();
                    }
                    requestLocation();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 内部TTS播报状态回调接口
     */
    private BaiduNaviManager.TTSPlayStateListener ttsPlayStateListener = new BaiduNaviManager
            .TTSPlayStateListener() {

        @Override
        public void playEnd() { }

        @Override
        public void playStart() { }
    };

    private boolean hasBasePhoneAuth() {
        // TODO Auto-generated method stub
        PackageManager pm = this.getPackageManager();
        for (String auth : authBaseArr) {
            if (pm.checkPermission(auth, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean hasCompletePhoneAuth() {
        // TODO Auto-generated method stub

        PackageManager pm = this.getPackageManager();
        for (String auth : authComArr) {
            if (pm.checkPermission(auth, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void initNavi() {

        BNOuterTTSPlayerCallback ttsCallback = null;

        // 申请权限
        if (android.os.Build.VERSION.SDK_INT >= 23) {

            if (!hasBasePhoneAuth()) {
                this.requestPermissions(authBaseArr, authBaseRequestCode);
                return;
            }
        }

        BaiduNaviManager.getInstance().init(this, mSDCardPath, APP_FOLDER_NAME, new BaiduNaviManager
                .NaviInitListener() {
            @Override
            public void onAuthResult(int status, String msg) {
                Log.e("status是什么", status + "");
                if (0 == status) {
                    authinfo = "key校验成功!";
                } else {
                    authinfo = "key校验失败！" + msg;
                }
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        toast(authinfo);
                    }
                });
            }

            public void initSuccess() {
                //toast("百度导航引擎初始化成功");
                hasInitSuccess = true;
                initSetting();
            }

            public void initStart() {
                //toast("百度导航引擎初始化开始");
            }

            public void initFailed() {
                toast("百度导航引擎初始化失败");
            }
        }, null, ttsHandler, ttsPlayStateListener);
    }

    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    private void routeplanToNavi(BNRoutePlanNode.CoordinateType coType) {
        mCoordinateType = coType;
        if (!hasInitSuccess) {
            toast("还未初始化!");
        }
        // 权限申请
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            // 保证导航功能完备
            if (!hasCompletePhoneAuth()) {
                if (!hasRequestComAuth) {
                    hasRequestComAuth = true;
                    this.requestPermissions(authComArr, authComRequestCode);
                    return;
                } else {
                    toast("没有完备的权限!");
                }
            }
        }
        BNRoutePlanNode sNode = null;
        BNRoutePlanNode eNode = null;
        switch (coType) {
            case BD09LL: {
                sNode = new BNRoutePlanNode(longitude2, latitude2, "", null, coType);
                eNode = new BNRoutePlanNode(Double.parseDouble(longitude), Double.parseDouble(latitude), "", null, coType);
                break;
            }
            default:
        }
        if (sNode != null && eNode != null) {
            List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
            list.add(sNode);
            list.add(eNode);

            // 开发者可以使用旧的算路接口,也可以使用新的算路接口,可以接收诱导信息等
            // BaiduNaviManager.getInstance().launchNavigator(this, list, 1, true, new DemoRoutePlanListener(sNode));
            BaiduNaviManager.getInstance().launchNavigator(this, list, 1,
                    true, new DemoRoutePlanListener(sNode),
                    eventListener);
        }
    }

    BaiduNaviManager.NavEventListener eventListener = new BaiduNaviManager.NavEventListener() {

        @Override
        public void onCommonEventCall(int what, int arg1, int arg2, Bundle bundle) {
            BNEventHandler.getInstance().handleNaviEvent(what, arg1, arg2, bundle);
        }
    };

    public class DemoRoutePlanListener implements BaiduNaviManager.RoutePlanListener {

        private BNRoutePlanNode mBNRoutePlanNode = null;

        public DemoRoutePlanListener(BNRoutePlanNode node) {
            mBNRoutePlanNode = node;
        }

        @Override
        public void onJumpToNavigator() {
            Intent intent = new Intent(DeviceStateActivity.this, BNDemoGuideActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        @Override
        public void onRoutePlanFailed() {
            // TODO Auto-generated method stub
            toast("算路失败");
        }
    }

    private void initSetting() {
        BNaviSettingManager
                .setShowTotalRoadConditionBar(BNaviSettingManager.PreViewRoadCondition.ROAD_CONDITION_BAR_SHOW_ON);
        BNaviSettingManager.setVoiceMode(BNaviSettingManager.VoiceMode.Veteran);
        BNaviSettingManager.setRealRoadCondition(BNaviSettingManager.RealRoadCondition.NAVI_ITS_ON);
        BNaviSettingManager.setIsAutoQuitWhenArrived(true);
        Bundle bundle = new Bundle();
        // 必须设置APPID，否则会静音
        bundle.putString(BNCommonSettingParam.TTS_APP_ID, "14271835");
        BNaviSettingManager.setNaviSdkParam(bundle);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == authBaseRequestCode) {
            for (int ret : grantResults) {
                if (ret == 0) {
                    continue;
                } else {
                    toast("缺少导航基本的权限!");
                    return;
                }
            }
            initNavi();
        } else if (requestCode == authComRequestCode) {
            for (int ret : grantResults) {
                if (ret == 0) {
                    continue;
                }
            }
            routeplanToNavi(mCoordinateType);
        }
    }

    private void requestLocation(){
        mLocationClient.start();
    }

    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location){
            if(location.getLocType() == BDLocation.TypeGpsLocation || location.getLocType() ==
                    BDLocation.TypeNetWorkLocation){
                navigateTo(location);
            }
        }
    }

    private void navigateTo(BDLocation location){
        // 如果从偏好设置device里面得出的经纬度有一者为空字符串那就获取手机的本地位置
        latitude2 = location.getLatitude();
        longitude2 = location.getLongitude();
        if(latitude.equals("") || longitude.equals("")){
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            // 以下五句代码的作用是构建小光标
            MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
            locationBuilder.latitude(location.getLatitude());
            locationBuilder.longitude(location.getLongitude());
            MyLocationData locationData = locationBuilder.build();
            baiduMap.setMyLocationData(locationData);
        }else{
            LatLng ll = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
            update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            // 以下五句代码的作用是构建小光标
            MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
            locationBuilder.latitude(Double.parseDouble(latitude));
            locationBuilder.longitude(Double.parseDouble(longitude));
            MyLocationData locationData = locationBuilder.build();
            baiduMap.setMyLocationData(locationData);
        }
    }

    @Event(value = { R.id.iv_back, R.id.bt_map_go, R.id.state_map, R.id.start_stop_record },
            type = View.OnClickListener.class)
    private void btnClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.bt_map_go:
                routeplanToNavi(BNRoutePlanNode.CoordinateType.BD09LL);
                break;
            case R.id.state_map:
                Tag = Tag == 0 ? 1 : 0;
                bt_state_map.setRotation(Tag == 0 ? 0 : 180);
                showView();
                break;
            case R.id.start_stop_record:
                if( !DoubleClickUtils.isFastDoubleClick() ){
                    intent.setClass(this, StartStopRecordActivity.class);
                    startActivity(intent);
                }
                break;
        }
    }

    private void showView() {
        if (Tag == 0) {
            state_data.setVisibility(View.VISIBLE);
            map.setVisibility(View.GONE);
        } else {
            state_data.setVisibility(View.GONE);
            map.setVisibility(View.VISIBLE);
        }
    }

    public class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            while (running) {
                try {
                    publishProgress();  // 类似于给主线程发消息，通知更新UI
                    Thread.sleep(TIME);
                } catch (InterruptedException e) {  // interrupt  打断
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if (!NetWorkUtil.isNetworkConnected(getApplicationContext())) {
                toast("网络连接错误，请检查网络...");
            } else {
                // 获取实时监控设备信息
                getData(); // 为主线程 更新UI
            }
            super.onProgressUpdate(values);
        }
    }

    private void startTask() {
        stopTask();
        running = true;
        task = (MyAsyncTask) new MyAsyncTask().execute();
    }

    private void stopTask() {
        if (task != null) {
            running = false;
            task.cancel(true);
            task = null;
        }
    }

    // 获取实时监控设备信息
    private void getData() {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadDeviceJKInfo");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadDeviceJKInfo");
        }
        params.addBodyParameter("guid", guidString);
        params.addBodyParameter("equNo", equNo);
        params.addBodyParameter("userId", String.valueOf(userId));
        x.http().get(params, new CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast("服务器异常，显示失败");
                if (loading.isShow()) {
                    loading.close();
                }
            }

            @Override
            public void onFinished() {}

            @Override
            public void onSuccess(String arg0) {
                try {
                    Log.e("DeviceStateActivity", arg0);
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        intent.setClass(DeviceStateActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")) {
                        mInfos.clear();
                        JSONObject jsonObject1 = object1.getJSONObject("Equipments");
                        // 18-09-25设定压力隐藏
                        //mpressureSetTextView.setText(jsonObject1.getString("PressureSet"));
                        mpressureOutTextView.setText(jsonObject1.getString("PressureOut"));
                        mpressureInTextView.setText(jsonObject1.getString("PressureIN"));
                        // 瞬时流量:0.00m³/s
                        instantaneousFlowTextView.setText(jsonObject1.getString("InstantFlow1"));
                        // 累计流量:0.00m³
                        cumulativeFlowTextView.setText(jsonObject1.getString("TotalFlow1"));
                        // 累计耗电:0.00
                        accumulatedPowerConsumption.setText(jsonObject1.getString("TotalPower1"));
                        turbidity.setText(jsonObject1.getString("Turbidity"));
                        residualChlorine.setText(jsonObject1.getString("CL"));
                        phValue.setText(jsonObject1.getString("PH"));
                        if(jsonObject1.has("SetFlow")){
                            pumpOrPressure.setText("设定流量(m³/h)");
                            setFlow.setText(jsonObject1.getString("SetFlow")); // 设定流量
                        } else {
                            pumpOrPressure.setText("设定压力(MPa)");
                            setFlow.setText(jsonObject1.getString("PressureSet")); // 设定压力
                        }
                        JSONArray jsonArray1 = new JSONArray(object1.getString("Pump"));
                        int pumpNum = jsonArray1.length();
                        // 遍历方法这次的i是1，下面的getJSONObject就得变为i-1，因为数组的下标是从0开始的
                        // 下面的语句只要有一条是null，就直接报异常出去，不会往下执行了
                        for (int i = 1; i <= pumpNum; i++) {
                            JSONObject jsonObject2 = jsonArray1.getJSONObject(i - 1);
                            DeviceStateInfo info = new DeviceStateInfo();
                            info.setIdString(String.valueOf(i));
                            info.setRunStateString(jsonObject2.getString("PState"));
                            info.setControlStateString(jsonObject2.getString("PFault"));
                            info.setElectricString(jsonObject2.getString("Electric"));
                            info.setNameString(i + "号水泵运行参数");
                            info.setFrequencyString(jsonObject2.getString("Frequency"));
                            mInfos.add(info);
                        }
                    } else {
                        toast(object1.getString("Message"));
                    }
                    madapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (loading.isShow()) {
                    loading.close();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        startTask();
        initView();
        showView();
        super.onStart();
    }

    @Override
    protected void onStop() {
        stopTask();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        // 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mMapView = null;
        baiduMap.setMyLocationEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    private void toast(String text){
        Toast.makeText(DeviceStateActivity.this, text, Toast.LENGTH_SHORT).show();
    }
}
