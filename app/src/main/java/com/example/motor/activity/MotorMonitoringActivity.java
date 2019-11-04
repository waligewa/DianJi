package com.example.motor.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MapViewLayoutParams;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.cretin.www.externalmaputilslibrary.OpenExternalMapAppUtils;
import com.example.motor.R;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.MotorDataBean;
import com.example.motor.db.MotorDataBeanFather;
import com.example.motor.db.PositionSaveBean;
import com.example.motor.service.LocationServer;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.NetWorkUtil;
import com.example.motor.util.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MotorMonitoringActivity extends Activity {

    @ViewInject(R.id.ic_a_title)
    TextView mtitleTextView;

    @ViewInject(R.id.motor_data_view)
    LinearLayout motor_data_view;
    @ViewInject(R.id.motor_rg)
    RadioGroup motor_rg;

    @ViewInject(R.id.motor_rb_1)
    RadioButton motor_rb_1;
    @ViewInject(R.id.motor_rb_2)
    RadioButton motor_rb_2;
    @ViewInject(R.id.motor_rb_3)
    RadioButton motor_rb_3;
    @ViewInject(R.id.motor_rb_4)
    RadioButton motor_rb_4;

    @ViewInject(R.id.mapView)
    MapView mMapView;

    @ViewInject(R.id.NowHz)
    TextView NowHz;
    @ViewInject(R.id.APhaseCurrent)
    TextView APhaseCurrent;
    @ViewInject(R.id.OutVoltage)
    TextView OutVoltage;
    @ViewInject(R.id.NowTemperature)
    TextView NowTemperature;
    @ViewInject(R.id.SysErrorCode1)
    TextView SysErrorCode1;
    @ViewInject(R.id.SysRunStatus1)
    TextView SysRunStatus1;
    @ViewInject(R.id.AI1ADCValue)
    TextView AI1ADCValue;
    @ViewInject(R.id.AI2ADCValue)
    TextView AI2ADCValue;
    @ViewInject(R.id.SoftVersion)
    TextView SoftVersion;
    @ViewInject(R.id.Counter3)
    TextView Counter3;

    @ViewInject(R.id.set_pressure)
    TextView sPressure;
    @ViewInject(R.id.influent_pressure)
    TextView iPressure;
    @ViewInject(R.id.out_pressure)
    TextView oPressure;
    @ViewInject(R.id.power)
    TextView power;
    @ViewInject(R.id.water_supply_status)
    TextView wStatus;                               // 供水状态
    @ViewInject(R.id.downtime_reason)
    TextView dtReason;                              // 停机原因

    @ViewInject(R.id.UpdateTime)
    TextView UpdateTime;
    @ViewInject(R.id.weizhi)
    TextView position;
    @ViewInject(R.id.position)
    TextView acPosition;
    @ViewInject(R.id.firstlinearlayout)
    LinearLayout flt;                               // 变频故障和状态码的隐藏
    @ViewInject(R.id.secondlinearlayout)
    LinearLayout slt;                               // 供水状态和停机原因的隐藏
    private View pop;
    private TextView tvTitle;

    private List<MotorDataBeanFather> dataInfo = new ArrayList<>();                 // 父类集合
    private List<PositionSaveBean> psList = new ArrayList<>();                      // 保存位置的集合
    private List<MotorDataBean> diList = new ArrayList<>();                         // 子类集合
    /**
     * 地图实例
     */
    private BaiduMap mBaiduMap;
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
     * 导航始终点的经纬度，sPosition为短的经纬度数字信息
     */
    private LatLng start, end;
    private MyAsyncTask task = null;
    private int TIME = 30 * 1000;
    private boolean running = true;
//  role判断是否显示
    private int flag = 0, role = 0;
    private String gatewayAddress, userId;
    private SharedPreferences prefs1;
//  往外传递的EquipmengID，因为不知道如何去获取全部的marker，所以每次点击一下marker，都将marker包裹的EquipmentID传递出来。初始的时候将最后一个marker对应的EquipmengID传递给此变量
    private String transferEid = "";
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//      注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_motor_monitoring);
        x.view().inject(this);

        prefs1 = getSharedPreferences("UserInfo", 0);
        editor = prefs1.edit();
        gatewayAddress = prefs1.getString("add", "");
        userId = String.valueOf(prefs1.getInt("UserID", 0));
        role = prefs1.getInt("Role", 0);
        mtitleTextView.setText("电机监控");

//      获得地图的实例
        mIconMaker = BitmapDescriptorFactory.fromResource(R.mipmap.maker);
        mBaiduMap = mMapView.getMap();
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(6.0f);
        mBaiduMap.setMapStatus(msu);
        mBaiduMap.setOnMarkerClickListener(onMarkerClickListener);
        /**
         * 设置用户拖拽标志覆盖物时位置描述气泡跟随
         */
        mBaiduMap.setOnMarkerDragListener(onMarkerDragListener);
//      地图的点击功能
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(pop != null){
                    pop.setVisibility(View.GONE);
                }
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
        initMyLocation();
//      RadioGroup的点击事件，最后diList里面的数据赋值给控件
        motor_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.motor_rb_1:
                        flag = 0;
                        break;
                    case R.id.motor_rb_2:
                        flag = 1;
                        break;
                    case R.id.motor_rb_3:
                        flag = 2;
                        break;
                    case R.id.motor_rb_4:
                        flag = 3;
                        break;
                }
//              如果集合diList的集合数量大于0就进行赋值操作
                if (diList.size() > 0) {
                    //toast(String.valueOf(flag));
                    setMotorData(diList.get(flag));
                }
            }
        });
        if(role == 157){
            flt.setVisibility(View.VISIBLE);
            slt.setVisibility(View.VISIBLE);
        } else {
            flt.setVisibility(View.GONE);
            slt.setVisibility(View.GONE);
        }

//      第一次请求数据，将点定位在最后一个Marker，显示的也是最后一个点的数据
        getData();
    }

    private BaiduMap.OnMarkerDragListener onMarkerDragListener = new BaiduMap.OnMarkerDragListener() {

        @Override
        public void onMarkerDragStart(Marker marker) {
            tvTitle.setText(marker.getTitle());
            mMapView.updateViewLayout(pop, createLayoutParams(marker.getPosition()));
        }

        @Override
        public void onMarkerDrag(Marker marker) {
            mMapView.updateViewLayout(pop, createLayoutParams(marker.getPosition()));
        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
            mMapView.updateViewLayout(pop, createLayoutParams(marker.getPosition()));
        }
    };

    private BaiduMap.OnMarkerClickListener onMarkerClickListener = new BaiduMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(final Marker marker) {
            flag = 0;
//          每次点击Marker都将第一个RadioButton弄成选中状态
            motor_rg.check(R.id.motor_rb_1);
//          通过bundle将Marker里面的EquipmengID取出来
            Bundle bundle = marker.getExtraInfo();
            String s = bundle.getString("id");
            transferEid = s;
            diList.clear();
            for(int i = 0; i < dataInfo.size(); i++){
                if(s.equals(dataInfo.get(i).getEquipmentID())){
                    List<MotorDataBean> nw = new ArrayList<>();
                    Type listType = new TypeToken<List<MotorDataBean>>() {}.getType();
                    Gson gson = new Gson();
                    nw = gson.fromJson(dataInfo.get(i).getPumplist(), listType);
//                  显示最上面的表格
                    initView(nw);
//                  此处如果和上一句代码互换位置会导致程序报异常，未解决，很奇怪
                    diList.addAll(nw);
//                  相当于初始化显示1号电机的数据
                    if (diList.size() > 0) {
                        setMotorData(diList.get(0));
                    }
                    if(!TextUtils.isEmpty(dataInfo.get(i).getUpdateTime())){
                        UpdateTime.setText(dataInfo.get(i).getUpdateTime().replace("T", " ").split("\\.")[0]);
                    }
//                  通过GeoCoder获取汉字位置
                    GeoCoder mSearch = GeoCoder.newInstance();
                    mSearch.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
                        @Override
                        public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) { }

                        @Override
                        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                            position.setText("北纬:" + marker.getPosition().latitude + ",  " + "东经:" + marker.getPosition().longitude);
                            if(TextUtils.isEmpty(reverseGeoCodeResult.getAddress())){
                                acPosition.setText("当前定位点位置：" + "未定位到详细位置");
                            } else {
                                acPosition.setText("当前定位点位置：" + reverseGeoCodeResult.getAddress());
                            }
                        }
                    });
//                  下面是传入对应的经纬度
                    mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude)));
//                  通过EquipmentId获取数据
                    getDataFromEquipmentId(s);
                }
            }
//          每次点击一次marker都将marker.position赋值到end上面
            end = marker.getPosition();
            return true;
        }
    };

    /**
     * 当操作Marker覆盖物的操作被监听到之后的操作
     * @param marker
     */
    private void onMarkerOperated(Marker marker) {
        if (pop == null) {
            pop = View.inflate(MotorMonitoringActivity.this, R.layout.pop, null);
            tvTitle = pop.findViewById(R.id.tv_title);
            mMapView.addView(pop, createLayoutParams(marker.getPosition()));
        } else {
            mMapView.updateViewLayout(pop, createLayoutParams(marker.getPosition()));
        }
        tvTitle.setText(marker.getTitle());
    }

    /**
     * 创建位置描述气泡的参数对象
     * @param position
     * @return
     */
    private ViewGroup.LayoutParams createLayoutParams(LatLng position) {
//      把View添加到什么样的布局中,就使用什么样的LayoutParams布局参数对象
        MapViewLayoutParams layoutParams = new MapViewLayoutParams
                .Builder()
                .layoutMode(MapViewLayoutParams.ELayoutMode.mapMode)                           // 设置坐标类型为经纬度
                .position(position)                                                            // 设置标志的位置
                .yOffset(-85)                                                                  // 设置View向上移
                .build();
        return layoutParams;
    }

    private void initView(List<MotorDataBean> t) {
        motor_rb_1.setVisibility(View.GONE);
        motor_rb_2.setVisibility(View.GONE);
        motor_rb_3.setVisibility(View.GONE);
        motor_rb_4.setVisibility(View.GONE);
        motor_data_view.setVisibility(View.GONE);                                               // 上面的表格结构LinearLayout
//      下面这个api没用过，是个清除所有选中效果的API
        motor_rg.clearCheck();
        if (t.size() > 0) {
            motor_data_view.setVisibility(View.VISIBLE);
            motor_rb_1.setVisibility(View.VISIBLE);
            motor_rb_1.setText("1号电机");
        }
        if (t.size() > 1) {
            motor_rb_2.setVisibility(View.VISIBLE);
            motor_rb_2.setText("2号电机");
        }
        if (t.size() > 2) {
            motor_rb_3.setVisibility(View.VISIBLE);
            motor_rb_3.setText("3号电机");
        }
        if (t.size() > 3) {
            motor_rb_4.setVisibility(View.VISIBLE);
            motor_rb_4.setText("4号电机");
        }
        switch (flag) {
            case 0:
                motor_rg.check(R.id.motor_rb_1);
                break;
            case 1:
                motor_rg.check(R.id.motor_rb_2);
                break;
            case 2:
                motor_rg.check(R.id.motor_rb_3);
                break;
            case 3:
                motor_rg.check(R.id.motor_rb_4);
                break;
        }
    }

    @Event(value = { R.id.iv_back, R.id.bt_map_go }, type = View.OnClickListener.class)
    private void btnClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.bt_map_go:
                /*OpenExternalMapAppUtils.openMapMarker(this,
                        end.longitude + "",
                        end.latitude + "",
                        getSharedPreferences("device", Context.MODE_PRIVATE).getString("comName", ""),
                        getSharedPreferences("device", Context.MODE_PRIVATE).getString("DevicePosition", ""),
                        "三利水务", true);*/
                OpenExternalMapAppUtils.openMapMarker(this,
                        end.longitude + "",
                        end.latitude + "",
                        mtitleTextView.getText().toString().trim(),
                        getSharedPreferences("device", Context.MODE_PRIVATE).getString("DevicePosition", ""),
                        "三利水务", true);
//              openMapMarker第三个字段是【电机名称】
                break;
        }
    }

//  第一次请求数据，将点定位在最后一个Marker，显示的也是最后一个点的数据
    private void getData() {
//      停止百度鹰眼的服务（里面还有给王珂上传坐标的一个网络请求方法）
        stopService(new Intent(this, LocationServer.class));
//      请求参数
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadDianJiData");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadDianJiData");
        }
        params.addBodyParameter("EquipmentID", "0");
        params.addBodyParameter("UserID", userId);
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
                    JSONObject object = new JSONObject(arg0);
                    List<PositionSaveBean> l = new ArrayList<>();
                    String s = prefs1.getString("positionSaveBean", "");
                    if (!TextUtils.isEmpty(s)) {
                        Gson g = new Gson();
                        Type listType = new TypeToken<List<PositionSaveBean>>() {}.getType();
                        l = g.fromJson(s, listType);
                    }
                    if (object.getString("Code").equals("1")) {
                        dataInfo.clear();
                        psList.clear();
                        mBaiduMap.clear();                                                               // 可以将地图上面的所有marker都清空
//                      将GPS设备采集的原始GPS坐标转换成百度坐标
                        CoordinateConverter converter = new CoordinateConverter();
                        converter.from(CoordinateConverter.CoordType.GPS);
                        JSONArray array = new JSONArray(object.getString("Data"));
                        for(int i = 0; i < array.length(); i++){
                            JSONObject jo = array.getJSONObject(i);
//                          如果设备填写错误，会没有WD2，直接在这个位置加continue，当出现获取数据错误的情况时，直接结束此次for循环，开始下一次循环
//                          if(jo.getString("WD2").equals("0")) continue;
                            if(!jo.has("WD2")) continue;
//                          如果在线状态是false的话，就结束本次循环，开始下一次循环
//                          if(jo.getString("IsOnLine").equals("False")) continue;
//                          位置
//                          LatLng latLng = new LatLng(Double.valueOf(jo.getString("WD1") + jo.getString("WD2")), Double.valueOf(jo.getString("JD1") + jo.getString("JD2")));
//                          将GPS设备采集的原始GPS坐标转换成百度坐标
//                          CoordinateConverter converter = new CoordinateConverter();
//                          converter.from(CoordinateConverter.CoordType.GPS);
//                          latLng 待转换坐标
//                          converter.coord(latLng);
//                          LatLng desLatLng = converter.convert();

//                          位置
                            LatLng desLatLng = null;
                            LatLng latLng = null;
                            String nWd = jo.getString("WD1");
//                          WD1的中间替换变量
                            String sWd = "";
                            String nJd = jo.getString("JD1");
//                          JD1的中间替换变量
                            String sJd = "";
                            if(!TextUtils.isEmpty(s)){
                                if(jo.getString("WD2").equals("0")){
                                    for(int j = 0; j < l.size(); j++){
                                        if (l.get(j).getEquipmentId().equals(jo.getString("EquipmentID"))){
                                            latLng = new LatLng(Double.valueOf(l.get(j).getLatitude()), Double.valueOf(l.get(j).getLongitude()));
                                            converter.coord(latLng);
                                            desLatLng = converter.convert();
                                            break;
                                        } else {
                                            /*if(!jo.getString("JD1").contains(".")){
                                                latLng = new LatLng(Double.valueOf(jo.getString("WD1") + jo.getString("WD2")),
                                                        Double.valueOf(jo.getString("JD1") + ".00" + jo.getString("JD2")));
                                            } else if(jo.getString("JD1").length() < 5){
                                                latLng = new LatLng(Double.valueOf(jo.getString("WD1") + jo.getString("WD2")),
                                                        Double.valueOf(jo.getString("JD1") + jo.getString("JD2")));
                                            }*/
                                            if(!nWd.contains(".")){
                                                sWd = nWd + ".00";
                                            } else if (nWd.split("\\.")[1].length() < 2){
                                                sWd = nWd + "0";
                                            } else {
                                                sWd = nWd;
                                            }
                                            if(!nJd.contains(".")){
                                                sJd = nJd + ".00";
                                            } else if (nJd.split("\\.")[1].length() < 2){
                                                sJd = nJd + "0";
                                            } else {
                                                sJd = nJd;
                                            }
                                            latLng = new LatLng(Double.valueOf(sWd + jo.getString("WD2")),
                                                    Double.valueOf(sJd + jo.getString("JD2")));
                                            converter.coord(latLng);
                                            desLatLng = converter.convert();
                                        }
                                    }
                                } else {
                                    /*if(jo.getString("JD1").contains(".")){
                                        latLng = new LatLng(Double.valueOf(jo.getString("WD1") + jo.getString("WD2")),
                                                Double.valueOf(jo.getString("JD1") + jo.getString("JD2")));
                                    } else {
                                        latLng = new LatLng(Double.valueOf(jo.getString("WD1") + jo.getString("WD2")),
                                                Double.valueOf(jo.getString("JD1") + "." + jo.getString("JD2")));
                                    }*/
                                    if(!nWd.contains(".")){
                                        sWd = nWd + ".00";
                                    } else if (nWd.split("\\.")[1].length() < 2){
                                        sWd = nWd + "0";
                                    } else {
                                        sWd = nWd;
                                    }
                                    if(!nJd.contains(".")){
                                        sJd = nJd + ".00";
                                    } else if (nJd.split("\\.")[1].length() < 2){
                                        sJd = nJd + "0";
                                    } else {
                                        sJd = nJd;
                                    }
                                    latLng = new LatLng(Double.valueOf(sWd + jo.getString("WD2")),
                                            Double.valueOf(sJd + jo.getString("JD2")));
                                    converter.coord(latLng);
                                    desLatLng = converter.convert();
                                }
                            } else {
//                              位置
                                /*if(jo.getString("JD1").contains(".")){
                                    latLng = new LatLng(Double.valueOf(jo.getString("WD1") + jo.getString("WD2")),
                                            Double.valueOf(jo.getString("JD1") + jo.getString("JD2")));
                                } else {
                                    latLng = new LatLng(Double.valueOf(jo.getString("WD1") + jo.getString("WD2")),
                                            Double.valueOf(jo.getString("JD1") + "." + jo.getString("JD2")));
                                }*/
                                if(!nWd.contains(".")){
                                    sWd = nWd + ".00";
                                } else if (nWd.split("\\.")[1].length() < 2){
                                    sWd = nWd + "0";
                                } else {
                                    sWd = nWd;
                                }
                                if(!nJd.contains(".")){
                                    sJd = nJd + ".00";
                                } else if (nJd.split("\\.")[1].length() < 2){
                                    sJd = nJd + "0";
                                } else {
                                    sJd = nJd;
                                }
                                latLng = new LatLng(Double.valueOf(sWd + jo.getString("WD2")),
                                        Double.valueOf(sJd + jo.getString("JD2")));
                                converter.coord(latLng);
                                desLatLng = converter.convert();
                            }

//                          第一次进行网络请求的时候，将经纬度对象赋值给end，一直赋值，最后end就代表的是最后一个点的经纬度对象
                            if(desLatLng == null){
                                end = latLng;
                            } else {
                                end = desLatLng;
                            }
//                          通过bundle将每一个EquipmengID都与marker绑定
                            Bundle mBundle = new Bundle();
                            mBundle.putString("id", jo.getString("EquipmentID"));
//                          图标   没有加title
                            OverlayOptions overlayOptions = null;
                            if(desLatLng == null){
                                overlayOptions = new MarkerOptions()
                                        .position(latLng)                                                    // 位置
                                        .icon(mIconMaker)                                                    // 图标
                                        .draggable(true)
                                        .zIndex(5);                                                          // 图标可以拖动
                            } else {
                                overlayOptions = new MarkerOptions()
                                        .position(desLatLng)                                                 // 位置
                                        .icon(mIconMaker)                                                    // 图标
                                        .draggable(true)
                                        .zIndex(5);                                                          // 图标可以拖动
                            }
//                          mBaiduMap.addOverlay(overlayOptions);
                            Marker marker = (Marker) mBaiduMap.addOverlay(overlayOptions);
                            marker.setExtraInfo(mBundle);

                            MotorDataBeanFather mbf = new MotorDataBeanFather();
                            mbf.setPumpNum(jo.getString("PumpNum"));
                            mbf.setEquipmentID(jo.getString("EquipmentID"));
                            mbf.setUpdateTime(jo.getString("UpdateTime"));
                            mbf.setComState(jo.getString("ComState"));
                            mbf.setIsOnLine(jo.getString("IsOnLine"));

                            if(!nWd.contains(".")){
                                sWd = nWd + ".00";
                            } else if (nWd.split("\\.")[1].length() < 2){
                                sWd = nWd + "0";
                            } else {
                                sWd = nWd;
                            }
                            if(!nJd.contains(".")){
                                sJd = nJd + ".00";
                            } else if (nJd.split("\\.")[1].length() < 2){
                                sJd = nJd + "0";
                            } else {
                                sJd = nJd;
                            }

                            mbf.setWD1(sWd);
                            mbf.setWD2(jo.getString("WD2"));
                            mbf.setJD1(sJd);
                            mbf.setJD2(jo.getString("JD2"));
//                          问题：胶州电机传值为0，点的位置是保存上了，但是进入界面的时候直接定位在了几内亚湾，经纬度为0，
//                          位置也是未定位到详细位置，那是因为end在getData里面最终赋值是赋值的dataInfo里面的为0的值
//                          解决办法：每次进行dataInfo赋值的时候都进行判断，如果WD2为0就看看l里面是否有相同设备id的数据，
//                          如果有，将其经纬度赋值为mbf，如果没有，赋值为0
                            if(jo.getString("WD2").equals("0")){
                                for(int z = 0; z < l.size(); z++){
                                    if(l.get(z).getEquipmentId().equals(jo.getString("EquipmentID"))){
                                        mbf.setWD1(l.get(z).getLatitude());
                                        mbf.setWD2("0");
                                        mbf.setJD1(l.get(z).getLongitude());
                                        mbf.setJD2("0");
                                    }
                                }
                            }
                            mbf.setPumplist(jo.getString("Pumplist"));
//                          经过一个for循环之后这个dataInfo父集合就被填满了
                            dataInfo.add(mbf);

//                          经纬度不为0，生成一个经纬度保存集合
                            PositionSaveBean psBean = new PositionSaveBean();
                            if(!jo.getString("WD2").equals("0")){
                                psBean.setEquipmentId(jo.getString("EquipmentID"));
                                if(!nWd.contains(".")){
                                    sWd = nWd + ".00";
                                } else if (nWd.split("\\.")[1].length() < 2){
                                    sWd = nWd + "0";
                                } else {
                                    sWd = nWd;
                                }
                                if(!nJd.contains(".")){
                                    sJd = nJd + ".00";
                                } else if (nJd.split("\\.")[1].length() < 2){
                                    sJd = nJd + "0";
                                } else {
                                    sJd = nJd;
                                }
                                psBean.setLatitude(sWd + jo.getString("WD2"));
                                psBean.setLongitude(sJd + jo.getString("JD2"));
                                psList.add(psBean);
                            } else {
                                for(int q = 0; q < l.size(); q++){
                                    if(l.get(q).getEquipmentId().equals(jo.getString("EquipmentID"))){
                                        psBean.setEquipmentId(l.get(q).getEquipmentId());
                                        psBean.setLatitude(l.get(q).getLatitude());
                                        psBean.setLongitude(l.get(q).getLongitude());
                                        psList.add(psBean);
                                    }
                                }
                            }

//                          通过for循环也是在一个又一个的给transferEid赋值，最终结果就是transferEid赋值为最后一个电机设备的EquipmengId
//                          transferEid = jo.getString("EquipmentID");
                        }

//                      经纬度保存的集合生成完毕，将其保存在SP文件里面，命名为positionSaveBean
                        if (psList.size() > 0) {
                            Gson g = new Gson();
                            String data = g.toJson(psList);
                            editor.putString("positionSaveBean", data);
                            editor.apply();
                        }

//                      通过GeoCoder获取文字位置，而不是只是摆着经纬度信息
                        GeoCoder mSearch = GeoCoder.newInstance();
                        mSearch.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
                            @Override
                            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) { }

                            @Override
                            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                                position.setText("北纬:" + end.latitude + ",  " + "东经:" + end.longitude);
                                if(TextUtils.isEmpty(reverseGeoCodeResult.getAddress())){
                                    acPosition.setText("当前定位点位置：" + "未定位到详细位置");
                                } else {
                                    acPosition.setText("当前定位点位置：" + reverseGeoCodeResult.getAddress());
                                }
                            }
                        });

//                      小集合里面赋值为父集合的最后一个元素
                        diList.clear();
                        List<MotorDataBean> noWord = new ArrayList<>();
                        if(dataInfo.size() > 0) {
                            Type listType = new TypeToken<List<MotorDataBean>>() {}.getType();
                            Gson gson = new Gson();
//                          tRepresentId是首页左上角选择的设备的设备id
                            String tRepresentId = getSharedPreferences("device", Context.MODE_PRIVATE).getString("EquipmentID", "");
                            for(int i = 0; i < dataInfo.size(); i++){
                                if(dataInfo.get(i).getEquipmentID().equals(tRepresentId)){
//                                  取出dataInfo集合当中与tRepresentId相同的作为字符串传入GSON从而产生小集合diList
                                    noWord = gson.fromJson(dataInfo.get(i).getPumplist(), listType);
//                                  显示最上面的表格
                                    initView(noWord);
//                                  此处如果和上一句代码互换位置会导致程序报异常，未解决，很奇怪
                                    diList.addAll(noWord);
//                                  相当于初始化显示1号电机的数据
                                    if (diList.size() > 0) {
                                        setMotorData(diList.get(flag));
                                    }
                                    transferEid = tRepresentId;

                                    converter.coord(new LatLng(Double.valueOf(dataInfo.get(i).getWD1() + dataInfo.get(i).getWD2()),
                                            Double.valueOf(dataInfo.get(i).getJD1() + dataInfo.get(i).getJD2())));
                                    if(converter.convert() == null){
                                        end = new LatLng(Double.valueOf(dataInfo.get(i).getWD1() + dataInfo.get(i).getWD2()),
                                                Double.valueOf(dataInfo.get(i).getJD1() + dataInfo.get(i).getJD2()));
                                    } else {
                                        end = converter.convert();
                                    }
//                                  下面是传入对应的经纬度
                                    mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(end));
//                                  将地图移到到首页左上角选择的设备经纬度位置
                                    MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(end);
                                    mBaiduMap.setMapStatus(u);
                                }
                            }
                        }
//                      标题赋值
                        mtitleTextView.setText(getSharedPreferences("device", Context.MODE_PRIVATE).getString("comName", ""));
                    } else {
                        toast(object.getString("Message"));
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

//  三十秒刷新一次数据
    private void getDataFiveSecond() {
//      请求参数
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadDianJiData");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadDianJiData");
        }
        params.addBodyParameter("EquipmentID", "0");
        params.addBodyParameter("UserID", userId);
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
                    JSONObject object = new JSONObject(arg0);
                    List<PositionSaveBean> l = new ArrayList<>();
                    final String s = prefs1.getString("positionSaveBean", "");
                    if (!TextUtils.isEmpty(s)) {
                        Gson g = new Gson();
                        Type listType = new TypeToken<List<PositionSaveBean>>() {}.getType();
                        l = g.fromJson(s, listType);
                    }
                    if (object.getString("Code").equals("1")) {
                        dataInfo.clear();
                        psList.clear();
                        mBaiduMap.clear();                                                               // 可以将地图上面的所有marker都清空
//                      将GPS设备采集的原始GPS坐标转换成百度坐标
                        CoordinateConverter converter = new CoordinateConverter();
                        converter.from(CoordinateConverter.CoordType.GPS);
                        JSONArray array = new JSONArray(object.getString("Data"));
                        for(int i = 0; i < array.length(); i++){
                            JSONObject jo = array.getJSONObject(i);
//                          如果在线状态是false的话，就结束本次循环，开始下一次循环
//                          if(jo.getString("IsOnLine").equals("False")) continue;
                            if(!jo.has("WD2")) continue;
//                          位置
                            LatLng latLng = null;
                            LatLng desLatLng = null;
                            String nWd = jo.getString("WD1");
//                          WD1的中间替换变量
                            String sWd = "";
                            String nJd = jo.getString("JD1");
//                          JD1的中间替换变量
                            String sJd = "";
                            if(!TextUtils.isEmpty(s)){
                                if(jo.getString("WD2").equals("0")){
                                    for(int j = 0; j < l.size(); j++){
                                        if (l.get(j).getEquipmentId().equals(jo.getString("EquipmentID"))){
                                            latLng = new LatLng(Double.valueOf(l.get(j).getLatitude()), Double.valueOf(l.get(j).getLongitude()));
                                            break;
                                        } else {
                                            if(!nWd.contains(".")){
                                                sWd = nWd + ".00";
                                            } else if (nWd.split("\\.")[1].length() < 2){
                                                sWd = nWd + "0";
                                            } else {
                                                sWd = nWd;
                                            }
                                            if(!nJd.contains(".")){
                                                sJd = nJd + ".00";
                                            } else if (nJd.split("\\.")[1].length() < 2){
                                                sJd = nJd + "0";
                                            } else {
                                                sJd = nJd;
                                            }
                                            latLng = new LatLng(Double.valueOf(sWd + jo.getString("WD2")),
                                                    Double.valueOf(sJd + jo.getString("JD2")));
                                            converter.coord(latLng);
                                            desLatLng = converter.convert();
                                        }
                                    }
                                } else {
                                    if(!nWd.contains(".")){
                                        sWd = nWd + ".00";
                                    } else if (nWd.split("\\.")[1].length() < 2){
                                        sWd = nWd + "0";
                                    } else {
                                        sWd = nWd;
                                    }
                                    if(!nJd.contains(".")){
                                        sJd = nJd + ".00";
                                    } else if (nJd.split("\\.")[1].length() < 2){
                                        sJd = nJd + "0";
                                    } else {
                                        sJd = nJd;
                                    }
                                    latLng = new LatLng(Double.valueOf(sWd + jo.getString("WD2")),
                                            Double.valueOf(sJd + jo.getString("JD2")));
                                    converter.coord(latLng);
                                    desLatLng = converter.convert();
                                }
                            } else {
//                              位置
                                if(!nWd.contains(".")){
                                    sWd = nWd + ".00";
                                } else if (nWd.split("\\.")[1].length() < 2){
                                    sWd = nWd + "0";
                                } else {
                                    sWd = nWd;
                                }
                                if(!nJd.contains(".")){
                                    sJd = nJd + ".00";
                                } else if (nJd.split("\\.")[1].length() < 2){
                                    sJd = nJd + "0";
                                } else {
                                    sJd = nJd;
                                }
                                latLng = new LatLng(Double.valueOf(sWd + jo.getString("WD2")),
                                        Double.valueOf(sJd + jo.getString("JD2")));
                                converter.coord(latLng);
                                desLatLng = converter.convert();
                            }
//                          通过bundle将每一个EquipmengID都与marker绑定
                            Bundle mBundle = new Bundle();
                            mBundle.putString("id", jo.getString("EquipmentID"));
//                          图标   没有加title
                            OverlayOptions overlayOptions = null;
                            if(desLatLng == null){
                                overlayOptions = new MarkerOptions()
                                        .position(latLng)                                                    // 位置
                                        .icon(mIconMaker)                                                    // 图标
                                        .draggable(true)
                                        .zIndex(5);                                                          // 图标可以拖动
                            } else {
                                overlayOptions = new MarkerOptions()
                                        .position(desLatLng)                                                    // 位置
                                        .icon(mIconMaker)                                                    // 图标
                                        .draggable(true)
                                        .zIndex(5);                                                          // 图标可以拖动
                            }
                            Marker marker = (Marker) mBaiduMap.addOverlay(overlayOptions);
                            marker.setExtraInfo(mBundle);

                            MotorDataBeanFather mbf = new MotorDataBeanFather();
                            mbf.setPumpNum(jo.getString("PumpNum"));
                            mbf.setEquipmentID(jo.getString("EquipmentID"));
                            mbf.setUpdateTime(jo.getString("UpdateTime"));
                            mbf.setComState(jo.getString("ComState"));
                            mbf.setIsOnLine(jo.getString("IsOnLine"));

                            if(!nWd.contains(".")){
                                sWd = nWd + ".00";
                            } else if (nWd.split("\\.")[1].length() < 2){
                                sWd = nWd + "0";
                            } else {
                                sWd = nWd;
                            }
                            if(!nJd.contains(".")){
                                sJd = nJd + ".00";
                            } else if (nJd.split("\\.")[1].length() < 2){
                                sJd = nJd + "0";
                            } else {
                                sJd = nJd;
                            }
                            mbf.setWD1(sWd);
                            mbf.setWD2(jo.getString("WD2"));
                            mbf.setJD1(sJd);
                            mbf.setJD2(jo.getString("JD2"));
                            mbf.setPumplist(jo.getString("Pumplist"));
//                          经过一个for循环之后这个dataInfo父集合就被填满了
                            dataInfo.add(mbf);

//                          经纬度不为0，生成一个经纬度保存集合
                            PositionSaveBean psBean = new PositionSaveBean();
                            if(!jo.getString("WD2").equals("0")){
                                psBean.setEquipmentId(jo.getString("EquipmentID"));
                                if(!nWd.contains(".")){
                                    sWd = nWd + ".00";
                                } else if (nWd.split("\\.")[1].length() < 2){
                                    sWd = nWd + "0";
                                } else {
                                    sWd = nWd;
                                }
                                if(!nJd.contains(".")){
                                    sJd = nJd + ".00";
                                } else if (nJd.split("\\.")[1].length() < 2){
                                    sJd = nJd + "0";
                                } else {
                                    sJd = nJd;
                                }
                                psBean.setLatitude(sWd + jo.getString("WD2"));
                                psBean.setLongitude(sJd + jo.getString("JD2"));
                                psList.add(psBean);
                            } else {
                                for(int q = 0; q < l.size(); q++){
                                    if(l.get(q).getEquipmentId().equals(jo.getString("EquipmentID"))){
                                        psBean.setEquipmentId(l.get(q).getEquipmentId());
                                        psBean.setLatitude(l.get(q).getLatitude());
                                        psBean.setLongitude(l.get(q).getLongitude());
                                        psList.add(psBean);
                                    }
                                }
                            }
                        }

//                      经纬度保存的集合生成完毕，将其保存在SP文件里面，命名为positionSaveBean
                        if(psList.size() > 0){
                            Gson gson = new Gson();
                            String data = gson.toJson(psList);
                            editor.putString("positionSaveBean", data);
                            editor.apply();
                        }

//                      通过GeoCoder获取文字位置，而不是只是摆着经纬度信息
                        GeoCoder mSearch = GeoCoder.newInstance();
                        mSearch.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
                            @Override
                            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) { }

                            @Override
                            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                                position.setText("北纬:" + end.latitude + ",  " + "东经:" + end.longitude);
                                if(TextUtils.isEmpty(reverseGeoCodeResult.getAddress())){
                                    acPosition.setText("当前定位点位置：" + "未定位到详细位置");
                                } else {
                                    acPosition.setText("当前定位点位置：" + reverseGeoCodeResult.getAddress());
                                }
                            }
                        });

//                      小集合里面赋值为父集合的与transferEid一致的元素
                        diList.clear();
                        List<MotorDataBean> noWord = new ArrayList<>();
                        if(dataInfo.size() > 0) {
//                          三十秒刷新，将点击Marker传递出来的transferEid与dataInfo里面的EquipmengId进行匹配，一旦匹配成功，就生成一个diList集合，并刷新界面，界面进行赋值，
//                          第一个RadioButton添加点击效果
                            for(int i = 0; i < dataInfo.size(); i++){
                                if(transferEid.equals(dataInfo.get(i).getEquipmentID())){
                                    Type listType = new TypeToken<List<MotorDataBean>>() {}.getType();
                                    Gson gs = new Gson();
                                    noWord = gs.fromJson(dataInfo.get(i).getPumplist(), listType);
//                                  显示最上面的表格
                                    initView(noWord);
//                                  此处如果和上一句代码互换位置会导致程序报异常，未解决，很奇怪
                                    diList.addAll(noWord);
//                                  相当于初始化显示1号电机的数据
                                    if (diList.size() > 0) {
                                        setMotorData(diList.get(flag));
                                    }
//                                  下面是传入对应的经纬度
                                    mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(end));
                                }
                            }
                        }
                    } else {
                        toast(object.getString("Message"));
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

//  通过EquipmentId获取数据
    private void getDataFromEquipmentId(final String s) {
//      请求参数
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadJZDataList");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadJZDataList");
        }
        params.addBodyParameter("EquipmentID", s);
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
                    JSONObject object = new JSONObject(arg0);
                    if (object.getString("Code").equals("1")) {
                        JSONObject jbt = new JSONObject(object.getString("Data"));
//                      通过bundle将一个EquipmengID放在marker里面
                        Bundle mBundle = new Bundle();
                        mBundle.putString("id", s);
//                      图标   没有加title
                        OverlayOptions overlayOptions = new MarkerOptions()
                                .position(end)                                                       // 位置
                                .icon(mIconMaker)                                                    // 图标
                                .title(jbt.getString("DeviceName") + "\n" +
                                        "DTU编号：" + jbt.getString("DeviceID"))               // title
                                .draggable(true)
                                .zIndex(5);                                                          // 图标可以拖动
                        mtitleTextView.setText(jbt.getString("DeviceName"));
                        Marker marker = (Marker) mBaiduMap.addOverlay(overlayOptions);
                        marker.setExtraInfo(mBundle);
                        if(!TextUtils.isEmpty(marker.getTitle())){
                            onMarkerOperated(marker);
                            pop.setVisibility(View.VISIBLE);
                        }
                    } else {
                        toast(object.getString("Message"));
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    private void setMotorData(MotorDataBean motorData) {
        NowHz.setText(motorData.getFrequency());                                                //  设备频率
        APhaseCurrent.setText(motorData.getElectric());                                         //  电机电流
        OutVoltage.setText(motorData.getVoltage());                                             //  电机电压
        NowTemperature.setText(motorData.getTemperature());                                     //  电机温度
        SysErrorCode1.setText(motorData.getError());                                            //  错误码
        SysRunStatus1.setText(motorData.getState());                                            //  状态码
        AI1ADCValue.setText(motorData.getAI1_ADC());                                            //
        AI2ADCValue.setText(motorData.getAI2_ADC());                                            //
        SoftVersion.setText(motorData.getVesion());                                             //  版本号
        Counter3.setText(motorData.getTimer());                                                 //  计数器
        sPressure.setText(motorData.getSetP());                                                 //  设定压力
        oPressure.setText(motorData.getOutPDec());                                              //  输出压力
        iPressure.setText(motorData.getInPDec());                                               //  输入压力
        power.setText(motorData.getScrEquipPower());
        if(!TextUtils.isEmpty(motorData.getScrEquipPower())){
            if (motorData.getScrEquipPower().equals("3")){
                power.setText("0.37");                                                          //  设备功率
            } else if (motorData.getScrEquipPower().equals("5")){
                power.setText("0.55");                                                          //  设备功率
            } else if (motorData.getScrEquipPower().equals("7")){
                power.setText("0.75");                                                          //  设备功率
            } else {
                power.setText(motorData.getScrEquipPower());                                    //  设备功率
            }
        }
        wStatus.setText(motorData.getEquipOperateStatus());                                     //  供水状态
        dtReason.setText(motorData.getEquipAlarmStatus());                                      //  停机原因
//      通过for循环，匹配transferEid，相同就取出dataInfo的其中一个子项，用于时间赋值
        for(int i = 0; i < dataInfo.size(); i++){
            if(transferEid.equals(dataInfo.get(i).getEquipmentID())){
                if(!TextUtils.isEmpty(dataInfo.get(i).getUpdateTime())){                        //  更新时间
                    UpdateTime.setText(dataInfo.get(i).getUpdateTime().replace("T", " ").split("\\.")[0]);
                }
            }
        }
    }

    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            while (running) {
                try {
                    publishProgress();                                                         // 类似于给主线程发消息，通知更新UI
                    Thread.sleep(TIME);
                } catch (InterruptedException e) {
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
//              三十秒刷新一次数据
                getDataFiveSecond();                                                           // 为主线程 更新UI
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

    /**
     * 初始化定位相关代码
     */
    private void initMyLocation() {
//      定位初始化
        mLocationClient = new LocationClient(this);
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);
//      设置定位的相关配置
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);                                        // 打开gps
        option.setCoorType("bd09ll");                                   // 设置坐标类型
        option.setScanSpan(10000);
        mLocationClient.setLocOption(option);
    }

    /**
     * 实现实位回调监听
     */
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
//          map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) return;
            start = new LatLng(location.getLatitude(), location.getLongitude());
        }

    }

    private void toast(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
//      开启图层定位
        mBaiduMap.setMyLocationEnabled(true);
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        }
        startTask();
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
//      关闭图层定位
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
        stopTask();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//      关闭图层定位
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
//      在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mIconMaker.recycle();
        mMapView = null;
//      开启百度鹰眼的服务（里面还有给王珂上传坐标的一个网络请求方法）
        startService(new Intent(this, LocationServer.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
//      在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
//      在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    //  第一次请求数据，将点定位在最后一个Marker，显示的也是最后一个点的数据
    /*private void getData() {
//      停止百度鹰眼的服务（里面含有给王珂上传坐标的一个网络请求方法）
        stopService(new Intent(this, LocationServer.class));
//      请求参数
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadDianJiData");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadDianJiData");
        }
        params.addBodyParameter("EquipmentID", "0");
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
                    JSONObject object = new JSONObject(arg0);
                    if (object.getString("Code").equals("1")) {
                        dataInfo.clear();
                        mBaiduMap.clear();                                                               // 可以将地图上面的所有marker都清空
                        JSONArray array = new JSONArray(object.getString("Data"));
                        for(int i = 0; i < array.length(); i++){
                            JSONObject jo = array.getJSONObject(i);
                            MotorDataBeanFather mbf = new MotorDataBeanFather();
                            mbf.setPumpNum(jo.getString("PumpNum"));
                            mbf.setEquipmentID(jo.getString("EquipmentID"));
                            mbf.setUpdateTime(jo.getString("UpdateTime"));
                            mbf.setComState(jo.getString("ComState"));
                            mbf.setIsOnLine(jo.getString("IsOnLine"));
                            if(jo.getString("EquipmentID").equals("1561620775929")){
                                mbf.setWD1("36.23");
                                mbf.setWD2("5039");
                                mbf.setJD1("120.07");
                                mbf.setJD2("3183");
                            } else if (jo.getString("EquipmentID").equals("1561691634004")){
                                mbf.setWD1("36.32");
                                mbf.setWD2("3547");
                                mbf.setJD1("120.44");
                                mbf.setJD2("8623");
                            } else if (jo.getString("EquipmentID").equals("1561706589385")){
                                mbf.setWD1("36.29");
                                mbf.setWD2("5505");
                                mbf.setJD1("120.37");
                                mbf.setJD2("3759");
                            } else {
                                mbf.setWD1(jo.getString("WD1"));
                                mbf.setWD2(jo.getString("WD2"));
                                mbf.setJD1(jo.getString("JD1"));
                                mbf.setJD2(jo.getString("JD2"));
                            }
                            mbf.setPumplist(jo.getString("Pumplist"));
//                          经过一个for循环之后这个dataInfo父集合就被填满了
                            dataInfo.add(mbf);

//                          位置
                            LatLng latLng = new LatLng(Double.valueOf(mbf.getWD1() + mbf.getWD2()), Double.valueOf(mbf.getJD1() + mbf.getJD2()));
//                          第一次进行网络请求的时候，将经纬度对象赋值给end，一直赋值，最后end就代表的是最后一个点的经纬度对象
                            end = latLng;
//                          通过bundle将每一个EquipmengID都与marker绑定
                            Bundle mBundle = new Bundle();
                            mBundle.putString("id", jo.getString("EquipmentID"));
//                          图标   没有加title
                            OverlayOptions overlayOptions = new MarkerOptions()
                                    .position(latLng)                                                    // 位置
                                    .icon(mIconMaker)                                                    // 图标
                                    .draggable(true)
                                    .zIndex(5);                                                          // 图标可以拖动
                            Marker marker = (Marker) mBaiduMap.addOverlay(overlayOptions);
                            marker.setExtraInfo(mBundle);

//                          通过for循环也是在一个又一个的给transferEid赋值，最终结果就是transferEid赋值为最后一个电机设备的EquipmengId
                            transferEid = jo.getString("EquipmentID");
                        }

//                      通过GeoCoder获取文字位置，而不是只是摆着经纬度信息
                        GeoCoder mSearch = GeoCoder.newInstance();
                        mSearch.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
                            @Override
                            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) { }

                            @Override
                            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                                position.setText("当前定位点坐标：" + end.latitude + ", " + end.longitude);
                                if(TextUtils.isEmpty(reverseGeoCodeResult.getAddress())){
                                    acPosition.setText("当前定位点位置：" + "未定位到详细位置");
                                } else {
                                    acPosition.setText("当前定位点位置：" + reverseGeoCodeResult.getAddress());
                                }
                            }
                        });
//                      下面是传入对应的经纬度
                        mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(end));
//                      将地图移到到最后一个经纬度位置，上面的话，transferEid和end均为最后一个的设备id和设备位置
                        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(end);
                        mBaiduMap.setMapStatus(u);

//                      小集合里面赋值为父集合的最后一个元素
                        diList.clear();
                        List<MotorDataBean> noWord = new ArrayList<>();
                        if(dataInfo.size() > 0) {
                            Type listType = new TypeToken<List<MotorDataBean>>() {}.getType();
                            Gson gson = new Gson();
//                          取出dataInfo集合当中最后一个元素作为字符串传入GSON从而产生小集合diList
                            noWord = gson.fromJson(dataInfo.get(dataInfo.size() - 1).getPumplist(), listType);
//                          显示最上面的表格
                            initView(noWord);
//                          此处如果和上一句代码互换位置会导致程序报异常，未解决，很奇怪
                            diList.addAll(noWord);
//                          相当于初始化显示1号电机的数据
                            if (diList.size() > 0) {
                                setMotorData(diList.get(flag));
                            }
                        }
//                      标题赋值
                        setDataForTitle(transferEid);
                    } else {
                        toast(object.getString("Message"));
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }*/

//  三十秒刷新一次数据
    /*private void getDataFiveSecond() {
//      请求参数
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadDianJiData");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadDianJiData");
        }
        params.addBodyParameter("EquipmentID", "0");
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
                    JSONObject object = new JSONObject(arg0);
                    if (object.getString("Code").equals("1")) {
                        dataInfo.clear();
                        mBaiduMap.clear();                                                               // 可以将地图上面的所有marker都清空
                        JSONArray array = new JSONArray(object.getString("Data"));
                        for(int i = 0; i < array.length(); i++){
                            JSONObject jo = array.getJSONObject(i);
                            MotorDataBeanFather mbf = new MotorDataBeanFather();
                            mbf.setPumpNum(jo.getString("PumpNum"));
                            mbf.setEquipmentID(jo.getString("EquipmentID"));
                            mbf.setUpdateTime(jo.getString("UpdateTime"));
                            mbf.setComState(jo.getString("ComState"));
                            mbf.setIsOnLine(jo.getString("IsOnLine"));
                            if(jo.getString("EquipmentID").equals("1561620775929")){
                                mbf.setWD1("36.23");
                                mbf.setWD2("5039");
                                mbf.setJD1("120.07");
                                mbf.setJD2("3183");
                            } else if (jo.getString("EquipmentID").equals("1561691634004")){
                                mbf.setWD1("36.32");
                                mbf.setWD2("3547");
                                mbf.setJD1("120.44");
                                mbf.setJD2("8623");
                            } else if (jo.getString("EquipmentID").equals("1561706589385")){
                                mbf.setWD1("36.29");
                                mbf.setWD2("5505");
                                mbf.setJD1("120.37");
                                mbf.setJD2("3759");
                            } else {
                                mbf.setWD1(jo.getString("WD1"));
                                mbf.setWD2(jo.getString("WD2"));
                                mbf.setJD1(jo.getString("JD1"));
                                mbf.setJD2(jo.getString("JD2"));
                            }
                            mbf.setPumplist(jo.getString("Pumplist"));
//                          经过一个for循环之后这个dataInfo父集合就被填满了
                            dataInfo.add(mbf);

//                          位置
                            LatLng latLng = new LatLng(Double.valueOf(mbf.getWD1() + mbf.getWD2()), Double.valueOf(mbf.getJD1() + mbf.getJD2()));
//                          通过bundle将每一个EquipmengID都与marker绑定
                            Bundle mBundle = new Bundle();
                            mBundle.putString("id", jo.getString("EquipmentID"));
//                          图标   没有加title
                            OverlayOptions overlayOptions = new MarkerOptions()
                                    .position(latLng)                                                    // 位置
                                    .icon(mIconMaker)                                                    // 图标
                                    .draggable(true)
                                    .zIndex(5);                                                          // 图标可以拖动
                            Marker marker = (Marker) mBaiduMap.addOverlay(overlayOptions);
                            marker.setExtraInfo(mBundle);
                        }

//                      通过GeoCoder获取文字位置，而不是只是摆着经纬度信息
                        GeoCoder mSearch = GeoCoder.newInstance();
                        mSearch.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
                            @Override
                            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) { }

                            @Override
                            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                                position.setText("当前定位点坐标：" + end.latitude + ", " + end.longitude);
                                if(TextUtils.isEmpty(reverseGeoCodeResult.getAddress())){
                                    acPosition.setText("当前定位点位置：" + "未定位到详细位置");
                                } else {
                                    acPosition.setText("当前定位点位置：" + reverseGeoCodeResult.getAddress());
                                }
                            }
                        });
//                      下面是传入对应的经纬度
                        mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(end));

//                      小集合里面赋值为父集合的与transferEid一致的元素
                        diList.clear();
                        List<MotorDataBean> noWord = new ArrayList<>();
                        if(dataInfo.size() > 0) {
//                          十秒刷新，将点击Marker传递出来的transferEid与dataInfo里面的EquipmengId进行匹配，一旦匹配成功，就生成一个diList集合，并刷新界面，界面进行赋值，
//                          第一个RadioButton添加点击效果
                            for(int i = 0; i < dataInfo.size(); i++){
                                if(transferEid.equals(dataInfo.get(i).getEquipmentID())){
                                    Type listType = new TypeToken<List<MotorDataBean>>() {}.getType();
                                    Gson gs = new Gson();
                                    noWord = gs.fromJson(dataInfo.get(i).getPumplist(), listType);
//                                  显示最上面的表格
                                    initView(noWord);
//                                  此处如果和上一句代码互换位置会导致程序报异常，未解决，很奇怪
                                    diList.addAll(noWord);
//                                  相当于初始化显示1号电机的数据
                                    if (diList.size() > 0) {
                                        setMotorData(diList.get(flag));
                                    }
                                }
                            }
                        }
                    } else {
                        toast(object.getString("Message"));
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }*/
}
