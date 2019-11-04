package com.example.motor.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MapViewLayoutParams;
import com.baidu.mapapi.map.MapViewLayoutParams.ELayoutMode;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.base.BaseActivity;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.PositionItem;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Author : 赵彬彬
 * Date   : 2019/6/25
 * Time   : 23:11
 * Desc   :
 */
public class MarkerOverlayActivity extends BaseActivity {
    private View pop;
    private TextView tvTitle;
    private String latitude, longitude, address;
    private SharedPreferences prefs, prefs2;
    @ViewInject(R.id.bmapView)
    MapView mMapView;
    private Button mapGo;
    private BaiduMap mapController;
    protected LatLng hmPos;
    private List<PositionItem> positionList = new ArrayList<>();

//  layout文件
    @Override
    public int setLayout() {
        SDKInitializer.initialize(getApplicationContext());
        return R.layout.activity_marker_overlay;
    }

//  在这里面设置TitleBar相关的
    @Override
    public void setTitle() {
        mBaseTitleBarView.setTitleText("电机位置");
        mBaseTitleBarView.setLeftDrawable(-1);
    }

//  初始化控件，设置监听
    @Override
    public void initWidget() {
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);
        prefs = getSharedPreferences("device", Context.MODE_PRIVATE);
        prefs2 = getSharedPreferences("UserInfo", 0);
        address = prefs2.getString("add", "");
        latitude = prefs.getString("Latitude", "");
        longitude = prefs.getString("Longitude", "");
        hmPos = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
        mapController = mMapView.getMap();
        //MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(hmPos);
        //mapController.setMapStatus(mapStatusUpdate);
        //mapStatusUpdate = MapStatusUpdateFactory.zoomTo(18);
        //mapController.setMapStatus(mapStatusUpdate);
        /**
         * 设置用户点击标志覆盖物后显示位置描述的气泡
         */
        mapController.setOnMarkerClickListener(onMarkerClickListener);
        /**
         * 设置用户拖拽标志覆盖物时位置描述气泡跟随
         */
        mapController.setOnMarkerDragListener(onMarkerDragListener);
        mapGo = (Button) findViewById(R.id.bt_map_go);
        mapGo.setOnClickListener(this);
//      得到位置信息
        getPositionData();
    }

    // View的click事件
    @Override
    public void widgetClick(View v) {
        switch (v.getId()){
            case R.id.bt_map_go:
                /*if(!DoubleClickUtils.isFastDoubleClick()){
                    OpenExternalMapAppUtils.openMapMarker(this,
                            end.longitude + "",
                            end.latitude + "",
                            prefs.getString("comName", ""),
                            prefs.getString("DevicePosition", ""),
                            "龙华水务", true);
                }*/
                break;
        }
    }

    // 返回键事件
    @Override
    public void onTitleLeftPressed() {
        onBackPressed();
    }

    private BaiduMap.OnMarkerDragListener onMarkerDragListener = new BaiduMap.OnMarkerDragListener() {

        @Override
        public void onMarkerDragStart(Marker marker) {
            tvTitle.setText(marker.getTitle());
            mMapView.updateViewLayout(pop,createLayoutParams(marker.getPosition()));
        }

        @Override
        public void onMarkerDrag(Marker marker) {
            mMapView.updateViewLayout(pop,createLayoutParams(marker.getPosition()));
        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
            mMapView.updateViewLayout(pop,createLayoutParams(marker.getPosition()));
        }
    };

    private BaiduMap.OnMarkerClickListener onMarkerClickListener = new BaiduMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            onMarkerOperated(marker);
            return true;
        }
    };

    /**
     * 当操作Marker覆盖物的操作被监听到之后的操作
     * @param marker
     */
    private void onMarkerOperated(Marker marker) {
        if (pop == null) {
            pop = View.inflate(MarkerOverlayActivity.this, R.layout.pop, null);
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
        // 把View添加到什么样的布局中,就使用什么样的LayoutParams布局参数对象
        MapViewLayoutParams layoutParams = new MapViewLayoutParams.Builder()
                .layoutMode(ELayoutMode.mapMode)     // 设置坐标类型为经纬度
                .position(position)                  // 设置标志的位置
                .yOffset(-85)                        // 设置View向上移
                .build();
        return layoutParams;
    }

    /**
     * 初始化标志覆盖物，此处可用for循环
     */
    private void initMarker() {
        LatLng l = new LatLng(Double.valueOf(positionList.get(0).getWD1() + positionList.get(0).getWD2()),
                Double.valueOf(positionList.get(0).getJD1() + positionList.get(0).getJD2()));
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(l);
        mapController.setMapStatus(mapStatusUpdate);
        mapStatusUpdate = MapStatusUpdateFactory.zoomTo(10);
        mapController.setMapStatus(mapStatusUpdate);
        MarkerOptions options = new MarkerOptions();
//      根据资源id创建bitmap的描述信息
        BitmapDescriptor descriptor = new BitmapDescriptorFactory().fromResource(R.mipmap.maker);
        for(int i = 0; i < positionList.size(); i++){
            LatLng lat = new LatLng(Double.valueOf(positionList.get(i).getWD1() + positionList.get(i).getWD2()),
                    Double.valueOf(positionList.get(i).getJD1() + positionList.get(i).getJD2()));
            options.position(lat)                  // 位置
                    .icon(descriptor)                // 图标
                    .title("AAA" + "\n" + "BBB")                    // title
                    .draggable(true);                // 图标可以拖动
            mapController.addOverlay(options);
        }
        /*options.position(new LatLng(hmPos.latitude + 0.001,hmPos.longitude))// 位置
                .icon(descriptor)                // 图标
                .title("BBB")                    // title
                .draggable(true);                // 图标可以拖动
        mapController.addOverlay(options);
        options.position(new LatLng(hmPos.latitude,hmPos.longitude + 0.001))// 位置
                .icon(descriptor)                // 图标
                .title("CCC")                    // title
                .draggable(true);                // 图标可以拖动
        mapController.addOverlay(options);
        options.position(new LatLng(hmPos.latitude - 0.001,hmPos.longitude - 0.001))// 位置
                .icon(descriptor)                // 图标
                .title("DDD")                    // title
                .draggable(true);                // 图标可以拖动
        mapController.addOverlay(options);*/
    }

    private void getPositionData() {
        RequestParams params;
        if(TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadDianJiData");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/LoadDianJiData");
        }
        params.addBodyParameter("EquipmentID", "0");
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {}

            @Override
            public void onError(Throwable ex, boolean arg1) { }

            @Override
            public void onFinished() {}

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("1")) {
                        Gson gson = new Gson();
                        Type type = new TypeToken<List<PositionItem>>() {}.getType();
                        positionList = gson.fromJson(object1.getString("Data"), type);
                        initMarker();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    private void toast(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
