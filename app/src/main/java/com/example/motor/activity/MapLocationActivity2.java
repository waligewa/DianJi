package com.example.motor.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.navisdk.adapter.BNCommonSettingParam;
import com.baidu.navisdk.adapter.BNOuterLogUtil;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.baidu.BNDemoGuideActivity;
import com.example.motor.baidu.BNEventHandler;
import com.example.motor.service.LocationServer;
import com.example.motor.util.DoubleClickUtils;

import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 这个是老的，我打算弄成海洋那种，因为这种模式还要启动百度导航的引擎，太慢了，尤其是在pda手机
 * 或者沈工给我的4.4.4的手机上面，等好久好久才能开始导航，非常影响用户体验，
 * 如果海洋的activity弄不好的话就直接调用这个就可以。
 *
 */
public class MapLocationActivity2 extends AppCompatActivity {

    @ViewInject(R.id.mapView)
    MapView mMapView;
    private double latitude2, longitude2; // 这里面的经纬度是开始点的经纬度
    private String latitude, longitude; // 这里面的经纬度是结束点的经纬度
    private LocationClient mLocationClient;
    private BaiduMap baiduMap;
    private MapStatusUpdate update;
    private Activity mActivity;
    private Intent intent;
    private static final String APP_FOLDER_NAME = "BNSDKSimpleDemo";
    private String mSDCardPath = null;
    private static final String ROUTE_PLAN_NODE = "routePlanNode";
    private final static String authBaseArr[] =
            { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION };
    private final static String authComArr[] = { Manifest.permission.READ_PHONE_STATE };
    private final static int authBaseRequestCode = 1; //认证
    private final static int authComRequestCode = 2;
    private boolean hasInitSuccess = false;
    private boolean hasRequestComAuth = false;
    private String authinfo = null;
    private BNRoutePlanNode.CoordinateType mCoordinateType = null;
    private SplashHandler ttsHandler = new SplashHandler();
    private BitmapDescriptor mIconMaker;
    // 当前定位的模式
    private MyLocationConfiguration.LocationMode mCurrentMode =
            MyLocationConfiguration.LocationMode.NORMAL;
    // 导航始终点的经纬度
    private LatLng start, end;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map_location);
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);

        init();
    }

    private void init(){
        mActivity = this;
        intent = new Intent();
        preferences = getSharedPreferences("DataName", Context.MODE_PRIVATE);
        editor = preferences.edit();
        prefs2 = getSharedPreferences("device", Context.MODE_PRIVATE);
        // 终点的经纬度
        latitude = prefs2.getString("Latitude", "");
        longitude = prefs2.getString("Longitude", "");
        // 获得地图的实例
        baiduMap = mMapView.getMap();
        mIconMaker = BitmapDescriptorFactory.fromResource(R.mipmap.maker);
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
        baiduMap.setMapStatus(msu);
        baiduMap.setMyLocationEnabled(true); // 开启这个功能   地图上面的那个小光标。
        BNOuterLogUtil.setLogSwitcher(true);

        initMyLocation();
        // 不加这个initDirs()的话会导致点击导航之后图层是黑色的。18-10-01
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
            }
        }
    }

    // 初始化定位相关代码
    private void initMyLocation() {

        // 定位初始化
        mLocationClient = new LocationClient(mActivity);
        mLocationClient.registerLocationListener(new MyLocationListener());
        // 设置定位的相关配置
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocationClient.setLocOption(option);
    }

    // 这个监听器在onStart()方法里面得以启动18-10-01，这样来做的话还解决了我GIS项目的一个问题。
    private class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location){
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) return;
            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius()) // accuracy精确
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build();
            // 设置定位数据
            baiduMap.setMyLocationData(locData);
            // 设置自定义图标
            BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
                    .fromResource(R.mipmap.navi_map_gps_locked);
            MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);
            baiduMap.setMyLocationConfigeration(config);
            // 这个是开始的经纬度
            latitude2 = location.getLatitude();
            longitude2 = location.getLongitude();
            start = new LatLng(location.getLatitude(), location.getLongitude());
        }
    }

    public void addInfosOverlay() {
        baiduMap.clear();
        LatLng latLng = null;
        OverlayOptions overlayOptions = null;
        // 位置，终点的位置
        latLng = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
        end = latLng;
        // 图标
        overlayOptions = new MarkerOptions().position(latLng).icon(mIconMaker).zIndex(5);
        baiduMap.addOverlay(overlayOptions);
        // 将地图移到到最后一个经纬度位置
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(end);
        baiduMap.setMapStatus(u);

        if (initDirs()) {
            initNavi();
        }
    }

    // 不加这个initDirs()的话会导致点击导航之后图层是黑色的。18-10-01
    private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) return false;
        File f = new File(mSDCardPath, APP_FOLDER_NAME); // app文件名字为"BNSDKSimpleDemo"
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

    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
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

        BaiduNaviManager.getInstance().init(this, mSDCardPath, APP_FOLDER_NAME, new BaiduNaviManager.NaviInitListener() {
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
                //toast("百度导航引擎初始化失败");
            }
        }, null, ttsHandler, ttsPlayStateListener);
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
                eNode = new BNRoutePlanNode(Double.parseDouble(longitude),
                        Double.parseDouble(latitude), "", null, coType);
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
            Intent intent = new Intent(mActivity, BNDemoGuideActivity.class);
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

    @Event(value = { R.id.iv_back, R.id.bt_map_go },
            type = View.OnClickListener.class)
    private void btnClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.bt_map_go:
                if(!DoubleClickUtils.isFastDoubleClick()){
                    routeplanToNavi(BNRoutePlanNode.CoordinateType.BD09LL);
                }
                break;
        }
    }

    private void toast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        init();
        // 开启图层定位
        baiduMap.setMyLocationEnabled(true);
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        }
        // 停止百度鹰眼的服务（里面还有给王珂上传坐标的一个网络请求方法）
        stopService(new Intent(this, LocationServer.class));
        addInfosOverlay();
    }

    @Override
    protected void onStop() {
        // 关闭图层定位
        baiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        baiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
        // 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mIconMaker.recycle();
        mMapView = null;
        // 开启百度鹰眼的服务（里面还有给王珂上传坐标的一个网络请求方法）
        startService(new Intent(this, LocationServer.class));
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
}
