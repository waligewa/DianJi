package com.example.motor.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.motor.activity.LoginActivity;
import com.example.motor.adapter.AbstractSpinerAdapter;
import com.example.motor.adapter.CustemSpinerAdapter;
import com.example.motor.base.CustemInfo;
import com.example.motor.constant.ConstantsField;
import com.example.motor.fragment.HomeFragment;
import com.example.motor.widget.RefreshListView;
import com.example.motor.widget.SpinerPopWindow;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2017/4/13.
 */
public class SpinWindowAreaUntil {

    public static CustemSpinerAdapter mAdapter;
    public static List<CustemInfo> mfistCustemInfors = new ArrayList<CustemInfo>();
    public static List<CustemInfo> msecondCustemInfors = new ArrayList<CustemInfo>();
    public static SpinerPopWindow mSpinerPopWindow;
    public static SharedPreferences sharedPreferences;
    public static int pageSize = 20, pageIndex = 1;
    public static boolean haveNext = true;
    public static OnItemSelectChangeListener mOnItemSelectChangeListener;

    public static void showSpinWindowArea(final Activity activity, View view, final TextView title) {
        sharedPreferences = activity.getSharedPreferences("device", Context.MODE_PRIVATE);
        mAdapter = new CustemSpinerAdapter(activity);
//      根据设备编号获取设备数据
        LoadRtuData(activity);
        mAdapter.refreshData(mfistCustemInfors, 0);
        mSpinerPopWindow = new SpinerPopWindow(activity);
        mSpinerPopWindow.setAdatper(mAdapter);
        mSpinerPopWindow.setItemListener(new AbstractSpinerAdapter.IOnItemSelectListener() {
            @Override
            public void onItemClick(int pos) {
                if (pos < mfistCustemInfors.size()) {
                    CustemInfo value = mfistCustemInfors.get(pos);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("index", pos);
                    editor.putString("deviceId", value.getId());
                    editor.putString("comAddress", value.getComAddress());
                    editor.putString("comName", value.getDeviceName());
                    editor.putString("EquipmentNo", value.getEquipmentNo());
                    editor.putString("EquipmentID", value.getEquipmentID());
                    editor.putString("EquipmentType", value.getEquipmentType());
                    editor.putString("Latitude", value.getLatitude());
                    editor.putString("Longitude", value.getLongitude());
                    editor.apply();
                    title.setText(value.getDeviceName());
                    HomeFragment.linearLayout2.setVisibility(View.VISIBLE);
                    HomeFragment.linearLayout3.setVisibility(View.GONE);
                    mSpinerPopWindow.dismiss();
//                  这是获取设备状态信息的接口
                    getData2(activity, value.getEquipmentID());
//                  mSpinerPopWindow.dismiss();
                    if (mOnItemSelectChangeListener != null) {
                        mOnItemSelectChangeListener.OnItemSelectChange();
                    }
                }
            }
        });
        mSpinerPopWindow.setOnRefreshListener(new RefreshListView.OnRefreshListener() {
            @Override
            public void onLoadingMore() {
                if (haveNext) {
                    getMoreData(activity);
                } else {
                    mSpinerPopWindow.removeFooterView();
                }
            }
        });

        mSpinerPopWindow.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        mSpinerPopWindow.showAsDropDown(view);
        mSpinerPopWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        mSpinerPopWindow.setOutsideTouchable(true);
    }

    /**
     * 设置刷新监听事件
     * @param listener
     */
    public static void setOnItemSelectChangeListener(OnItemSelectChangeListener listener) {
        mOnItemSelectChangeListener = listener;
    }

//  根据设备编号获取设备数据
    private static void LoadRtuData(final Activity activity) {
        pageIndex = 1;
        pageSize = 20;
        haveNext = true;
//      请求参数
        String address = activity.getSharedPreferences("UserInfo", 0).getString("add", "");
        RequestParams params;
        if(TextUtils.isEmpty(SPUtils.getStringData(activity, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadRtuData");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/LoadRtuData");
        }
        int userId = activity.getSharedPreferences("UserInfo", 0).getInt("UserID", 0);
        Log.e("看一看", userId + "===" + params.getUri());
        params.addBodyParameter("userId", userId + "");
        params.addBodyParameter("equNo", "");
        params.addBodyParameter("pageSize", pageSize + "");
        params.addBodyParameter("pageIndex", pageIndex + "");
        params.addBodyParameter("guid", "");
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                Toast.makeText(x.app(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinished() { }

            @Override
            public void onSuccess(String arg0) {
                Log.e("首页左上角请求数据", arg0);
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        Intent intent = new Intent(activity, LoginActivity.class);
                        activity.startActivity(intent);
                        activity.finish();
                        Toast.makeText(activity, object1.getString("Message"), Toast.LENGTH_SHORT).show();
                    } else if (object1.getString("Code").equals("1")) {
                        Type listType = new TypeToken<List<CustemInfo>>() {}.getType();
                        Gson gson = new Gson();
                        List<CustemInfo> list = gson.fromJson(object1.getString("Data"), listType);
                        if (list.size() < pageSize) {
                            haveNext = false;
                        }
                        mfistCustemInfors.clear();              // 集合的清空
                        mfistCustemInfors.addAll(list);         // 把array集合全部加入到mfirstCustemInfors集合中
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(activity, object1.getString("Message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

//  这是获取设备状态信息的接口
    private static void getData2(final Activity activity, final String s) {
        int userId = activity.getSharedPreferences("UserInfo", 0).getInt("UserID", 0);
        String address = activity.getSharedPreferences("UserInfo", 0).getString("add", "");
        RequestParams params;
        if(TextUtils.isEmpty(SPUtils.getStringData(activity, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadDianJiData");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/LoadDianJiData");
        }
        params.addBodyParameter("EquipmentID", s);
        params.addBodyParameter("UserID", String.valueOf(userId));
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                //Toast.makeText(activity, ex.getMessage(), Toast.LENGTH_SHORT).show();
                Toast.makeText(activity, "不存在电机状态信息", Toast.LENGTH_SHORT).show();
                HomeFragment.influentPressure.setText("0");      // 进水压力
                HomeFragment.waterPressure.setText("0");         // 出水压力
                HomeFragment.setPressure.setText("0");           // 设定压力
                HomeFragment.eqPower.setText("0");               // 设备功率
            }

            @Override
            public void onFinished() { }

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("1")) {
                        JSONArray ja = object1.getJSONArray("Data");
                        JSONObject js = ja.getJSONObject(0);
                        JSONArray jy = js.getJSONArray("Pumplist");
                        JSONObject jt = jy.getJSONObject(0);
                        HomeFragment.influentPressure.setText(jt.getString("InPDec"));        // 进水压力
                        HomeFragment.waterPressure.setText(jt.getString("OutPDec"));          // 出水压力
                        HomeFragment.setPressure.setText(jt.getString("SetP"));               // 设定压力
                        if(!TextUtils.isEmpty(jt.getString("ScrEquipPower"))){
                            if (jt.getString("ScrEquipPower").equals("3")){
                                HomeFragment.eqPower.setText("0.37");                                //  设备功率
                            } else if (jt.getString("ScrEquipPower").equals("5")){
                                HomeFragment.eqPower.setText("0.55");                                //  设备功率
                            } else if (jt.getString("ScrEquipPower").equals("7")){
                                HomeFragment.eqPower.setText("0.75");                                //  设备功率
                            } else {
                                HomeFragment.eqPower.setText(jt.getString("ScrEquipPower"));  //  设备功率
                            }
                        }
                    } else {
                        Toast.makeText(activity, object1.getString("Message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    // 下拉列表获取更多的设备
    private static void getMoreData(final Activity activity) {
        pageIndex = pageIndex + 1;
        pageSize = 20;
        // 请求参数
        String address = activity.getSharedPreferences("UserInfo", 0).getString("add", "");
        RequestParams params;
        if(TextUtils.isEmpty(SPUtils.getStringData(activity, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadRtuData");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/LoadRtuData");
        }
        int userId = activity.getSharedPreferences("UserInfo", 0).getInt("UserID", 0);
        params.addBodyParameter("userId", userId + "");
        params.addBodyParameter("equNo", "");
        params.addBodyParameter("pageSize", pageSize + "");
        params.addBodyParameter("pageIndex", pageIndex + "");
        params.addBodyParameter("guid", "");

        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {}

            @Override
            public void onError(Throwable ex, boolean arg1) {
                Toast.makeText(x.app(), "服务器异常", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinished() {}

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        Intent intent = new Intent(activity, LoginActivity.class);
                        activity.startActivity(intent);
                        activity.finish();
                        Toast.makeText(activity, object1.getString("Message"), Toast.LENGTH_SHORT).show();
                    } else if (object1.getString("Code").equals("1")) {
                        Type listType = new TypeToken<List<CustemInfo>>() {}.getType();
                        Gson gson = new Gson();
                        List<CustemInfo> array = gson.fromJson(object1.getString("Data"), listType);
                        if (array.size() < pageSize) {
                            haveNext = false;
                        }
                        if (array.size() > 0) {
                            mfistCustemInfors.addAll(array);
                            mAdapter.notifyDataSetChanged();
                        }
                        mSpinerPopWindow.hideFooterView();
                    } else {
                        Toast.makeText(activity, object1.getString("Message"), Toast.LENGTH_SHORT).show();
                    }
                    mSpinerPopWindow.hideFooterView();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    public interface OnItemSelectChangeListener {
        void OnItemSelectChange();
    }
}
