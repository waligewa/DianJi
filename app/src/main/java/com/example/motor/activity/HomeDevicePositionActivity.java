package com.example.motor.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import com.example.motor.R;
import com.example.motor.base.CustemInfo;
import com.example.motor.constant.ConstantsField;
import com.example.motor.service.LocationServer;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DoubleClickUtils;
import com.example.motor.util.Loadding;
import com.example.motor.util.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.lang.reflect.Type;
import java.util.List;

/**
 *
 * Author : 赵彬彬
 * Date   : 2019/6/14
 * Time   : 9:15
 * Desc   : 服务界面的设备详情activity
 */
public class HomeDevicePositionActivity extends Activity {

    Activity mActivity;
    @ViewInject(R.id.bt_map_go)
    Button mapGo;
    @ViewInject(R.id.mm_title)
    TextView title;
    @ViewInject(R.id.mapView)
    MapView mMapView;
    /*@ViewInject(R.id.scrollview)
    ScrollView scrollView;*/
    /**
     * 地图实例
     */
    private BaiduMap mBaiduMap;
//  初始化全局 bitmap 信息，不用时及时 recycle
    private BitmapDescriptor mIconMaker;

    /**
     * 定位的客户端
     */
    private LocationClient mLocationClient;
    /**
     * 定位的监听器
     */
    public MyLocationListener mMyLocationListener;
    /**
     * 当前定位的模式
     */
    private MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
    /**
     * 导航始终点的经纬度
     */
    private LatLng end;
    private String longitude, latitude;
    private Loadding loading;
    private SharedPreferences prefs1, prefs2;
    private String equipmentNumber = "", address, guid;
    private int userId;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = HomeDevicePositionActivity.this;
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(mActivity.getApplicationContext());
        setContentView(R.layout.activity_home_device_position);
        x.view().inject(this);
        initView();
    }

//  初始化主界面
    protected void initView() {
        title.setText(getSharedPreferences("device", Context.MODE_PRIVATE).getString("comName", "设备详情"));
        prefs1 = mActivity.getSharedPreferences("UserInfo", 0);
        prefs2 = mActivity.getSharedPreferences("device", Context.MODE_PRIVATE);
        address = prefs1.getString("add", "");
        guid = prefs1.getString("guid", "");
        userId = prefs1.getInt("UserID", 0);
        equipmentNumber = prefs2.getString("EquipmentNo", "");
        loading = new Loadding(mActivity);
        intent = new Intent();
        mapGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!DoubleClickUtils.isFastDoubleClick()) {
                    OpenExternalMapAppUtils.openMapMarker(mActivity,
                            end.longitude+"",
                            end.latitude+"",
                            prefs2.getString("comName", ""),
                            prefs2.getString("DevicePosition", ""),
                            "龙华水务", true);
                }
            }
        });
//      这个网络请求方法是用来记住登录之后能直接进入这个功能的，不是登录之后每次都得点击
//      左上角按钮才能进入
        getData3(equipmentNumber);
//      获得地图的实例
        mBaiduMap = mMapView.getMap();
        mIconMaker = BitmapDescriptorFactory.fromResource(R.mipmap.maker);
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.setMapStatus(msu);
        BNOuterLogUtil.setLogSwitcher(true);

//      重写onTouch()事件,在事件里通过requestDisallowInterceptTouchEvent(boolean)方法来设置父类
//      的不可用，true表示父类的不可用
//      解决地图的touch事件和scrollView的touch事件冲突问题
        /*View v = mMapView.getChildAt(0);                                        // 这个view实际上就是我们看见的绘制在表面的地图图层

        v.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    scrollView.requestDisallowInterceptTouchEvent(false);
                } else {
                    scrollView.requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }
        });*/

//      初始化定位相关代码
        initMyLocation();
    }

    @Event(value = {R.id.mm_back}, type = View.OnClickListener.class)
    private void btnClick(View view) {
        switch (view.getId()) {
            case R.id.mm_back:
                finish();
                break;
            default:
                break;
        }
    }

    /**
     * 初始化定位相关代码
     */
    private void initMyLocation() {
//      定位初始化
        mMyLocationListener = new MyLocationListener();
//      设置定位的相关配置
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);                       // 打开gps
        option.setCoorType("bd09ll");                  // 设置坐标类型
        option.setScanSpan(1000);
        mLocationClient.setLocOption(option);
    }

    /**
     * 实现实位回调监听
     */
    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {

//          map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null)
                return;
//          构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();// accuracy 精确
//          设置定位数据
            mBaiduMap.setMyLocationData(locData);
//          设置自定义图标
            BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
                    .fromResource(R.mipmap.navi_map_gps_locked);
            MyLocationConfiguration config = new
                    MyLocationConfiguration(mCurrentMode,true, mCurrentMarker);
            mBaiduMap.setMyLocationConfigeration(config);
        }
    }

    public void addInfosOverlay(String st1, String st2) {
        mBaiduMap.clear();
        LatLng latLng = null;
        OverlayOptions overlayOptions = null;
//      位置
        latLng = new LatLng(Double.valueOf(st1), Double.valueOf(st2));
        end = latLng;
//      图标
        overlayOptions = new MarkerOptions().position(latLng).icon(mIconMaker).zIndex(5);
        mBaiduMap.addOverlay(overlayOptions);
//      将地图移到到最后一个经纬度位置
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(end);
        mBaiduMap.setMapStatus(u);
    }

    private void toast(String text){
        Toast.makeText(mActivity, text, Toast.LENGTH_SHORT).show();
    }

//  这个网络请求方法是用来记住登录之后能直接进入这个功能的，不是登录之后每次都得点击
//  左上角按钮才能进入
    private void getData3(String s) {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadRtuData");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/LoadRtuData");
        }
        params.addBodyParameter("userId", String.valueOf(userId));
        params.addBodyParameter("equNo", s);
        params.addBodyParameter("pageSize","100");
        params.addBodyParameter("pageIndex","1");
        params.addBodyParameter("guid", guid);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {}

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast(ex.getMessage());
            }

            @Override
            public void onFinished() {}

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
//                      如果集合是null的话就return
                        if(list == null) return;
                        CustemInfo info = list.get(0);
                        longitude = info.getLongitude();
                        latitude = info.getLatitude();
//                      2018年7月3日这句代码本来放在onCreateView里面的，因为getData3是耗时操作，
//                      因此将这句代码放在onCreateView里面，会先得到执行，这样就会报空指针异常。
                        addInfosOverlay(latitude, longitude);
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

    @Override
    public void onStart() {
//      这个网络请求方法是用来记住登录之后能直接进入这个功能的，不是登录之后每次都得点击
//      左上角按钮才能进入
        getData3(equipmentNumber);
//      开启图层定位
        mBaiduMap.setMyLocationEnabled(true);
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        }
//      停止百度鹰眼的服务（里面还有给王珂上传坐标的一个网络请求方法）
        stopService(new Intent(this, LocationServer.class));
        super.onStart();
    }

    @Override
    public void onStop() {
//      关闭图层定位
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
//      在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mIconMaker.recycle();
        mMapView = null;
        mBaiduMap.setMyLocationEnabled(false);
//      开启百度鹰眼的服务（里面还有给王珂上传坐标的一个网络请求方法）
        startService(new Intent(this, LocationServer.class));
    }

    @Override
    public void onResume() {
        super.onResume();
//      在activity执行onResume时执行mMapView. onResume()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
//      在activity执行onPause时执行mMapView. onPause()，实现地图生命周期管理
        mMapView.onPause();
    }
}
