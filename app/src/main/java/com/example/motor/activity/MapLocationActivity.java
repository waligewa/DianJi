package com.example.motor.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.baidu.navisdk.adapter.BNOuterLogUtil;
import com.cretin.www.externalmaputilslibrary.OpenExternalMapAppUtils;
import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.service.LocationServer;
import com.example.motor.util.DoubleClickUtils;

import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * 从设备状态界面跳转过来的地图位置activity
 *
 */

public class MapLocationActivity extends AppCompatActivity {

    @ViewInject(R.id.mapView)
    MapView mMapView;
    private String latitude, longitude;  // 这里面的经纬度是结束点的经纬度
    private LocationClient mLocationClient;
    private BaiduMap baiduMap;
    private Activity mActivity;
    private Intent intent;
    private BitmapDescriptor mIconMaker;
    // 当前定位的模式
    private MyLocationConfiguration.LocationMode mCurrentMode =
            MyLocationConfiguration.LocationMode.NORMAL;
    // 导航始终点的经纬度
    private LatLng end;
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
    }

    @Event(value = { R.id.iv_back, R.id.bt_map_go }, type = View.OnClickListener.class)
    private void btnClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.bt_map_go:
                if(!DoubleClickUtils.isFastDoubleClick()){
                    OpenExternalMapAppUtils.openMapMarker(this,
                            end.longitude + "",
                            end.latitude + "",
                            prefs2.getString("comName", ""),
                            prefs2.getString("DevicePosition", ""),
                            "三利水务", true);
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
