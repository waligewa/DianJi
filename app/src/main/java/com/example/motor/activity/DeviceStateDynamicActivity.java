package com.example.motor.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.adapter.DeviceStateAdapter;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.DataInfoItem;
import com.example.motor.db.DeviceStateInfo;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DoubleClickUtils;
import com.example.motor.util.Loadding;
import com.example.motor.util.NetWorkUtil;
import com.example.motor.util.SPUtils;
import com.example.motor.widget.MyGridView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Author : 赵彬彬
 * Date   : 2019/6/19
 * Time   : 11:26
 * Desc   : 设备状态动态activity，从主页进入的
 */
public class DeviceStateDynamicActivity extends Activity {

    @ViewInject(R.id.ic_a_title)
    TextView mtitleTextView;
    @ViewInject(R.id.gv_state)
    ListView mListView;
    @ViewInject(R.id.mGridView)
    MyGridView mGridView;
    @ViewInject(R.id.change_data)
    TextView changeData;
    private DeviceStateAdapter madapter;
    private List<DeviceStateInfo> mInfos = new ArrayList<>();

    private MyAsyncTask task = null;
    private int TIME = 10 * 1000, userId;
    private boolean running = true;
    private String gatewayAddress, guidString, equNo;
    private SharedPreferences prefs1, prefs2;
    private Loadding loading;
    private Intent intent;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Activity mActivity;
    private List<DataInfoItem> dataInfo = new ArrayList<>(), dataInfoOnShow = new ArrayList<>();
    private String[] dataInfoItemName = null;
    private boolean[] onShow = null;
    private mGridViewAdapter mAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_state_dynamic);
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);

        initView();
        // 得到点击选取显示数据之后的数据
        getDataInfo();
        // 得到getDataInfo之后的显示数据
        getData2();
    }

    private void initView() {
        intent = new Intent();
        mActivity = this;
        loading = new Loadding(this);
        preferences = getSharedPreferences("DataName", Context.MODE_PRIVATE);
        editor = preferences.edit();
        prefs2 = getSharedPreferences("device", Context.MODE_PRIVATE);
        prefs1 = getSharedPreferences("UserInfo", 0);
        gatewayAddress = prefs1.getString("add", "");
        guidString = prefs1.getString("guid", "");
        userId = prefs1.getInt("UserID", 0);
        equNo = prefs2.getString("EquipmentNo", "");
        mtitleTextView.setText(prefs2.getString("comName", "设备状态").trim());
        madapter = new DeviceStateAdapter(this, mInfos);
        mListView.setAdapter(madapter);
        mAdapter = new mGridViewAdapter(this, dataInfoOnShow);
        mGridView.setAdapter(mAdapter);
        // 最顶部的控件获取焦点
        changeData.setFocusable(true);
        changeData.setFocusableInTouchMode(true);
        changeData.requestFocus();
    }

    @Event(value = { R.id.iv_back, R.id.start_stop_record, R.id.change_data, R.id.location }, type = View.OnClickListener.class)
    private void btnClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.start_stop_record:
                if( !DoubleClickUtils.isFastDoubleClick() ){
                    intent.setClass(this, StartStopRecordActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.change_data:
                if (dataInfoItemName.length > 0) {
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle("请选择要显示的数据")
                            .setIcon(R.mipmap.ic_launcher)
                            /*.setNegativeButton("取消", null)*/
                            .setCancelable(false)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // dataInfoOnShow集合为和mAdapter适配器结合用于显示的集合
                                    dataInfoOnShow.clear();
                                    for (DataInfoItem dataInfoItem : dataInfo) {
                                        if (dataInfoItem.isOnShow()) {
                                            dataInfoOnShow.add(dataInfoItem);
                                        }
                                    }
                                    mAdapter.notifyDataSetChanged();
                                }
                            })
                            .setMultiChoiceItems(dataInfoItemName, onShow, new DialogInterface.OnMultiChoiceClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                    // onShow数组是存储选取还是非选取状态的
                                    onShow[which] = isChecked;
                                    // which为位置
                                    dataInfo.get(which).setOnShow(isChecked);
                                    // 保存List
                                    setDataList("dataList", dataInfo);
                                }
                            })
                            .create();
                    dialog.show();
                } else {
                    toast("数据库没有可选字段！");
                }
                break;
            case R.id.location:
                if(!DoubleClickUtils.isFastDoubleClick()){
                    intent.setClass(mActivity, MapLocationActivity.class);
                    startActivity(intent);
                }
                break;
        }
    }

    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {

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
                getData();// 为主线程 更新UI
                getData2();// 为主线程 更新UI
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

    // 获取1号泵和2号泵的实时监控设备信息
    private void getData() {
        // 下面两句代码这样写，是因为当时出现了一个bug，现象：消失的时候点击列表闪退了。
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
                toast("服务器异常");
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
                        intent.setClass(DeviceStateDynamicActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")) {
                        mInfos.clear();
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

    // 得到点击选取显示数据之后的数据
    private void getDataInfo() {
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadShowDataInfo");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadShowDataInfo");
        }
        params.addBodyParameter("EquipmentType", "1");// 这个1代表无负压
        x.http().get(params, new CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast("服务器异常");
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
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")) {
                        dataInfo.clear();
                        Gson gson = new GsonBuilder().serializeNulls().create();
                        Type listType = new TypeToken<List<DataInfoItem>>() {}.getType();
                        List<DataInfoItem> list = gson.fromJson(object1.getString("Data"), listType);
                        dataInfo.addAll(list);
                        if (dataInfo.size() > 0) {
                            // 数据信息（汉字名称）子项数组初始化
                            dataInfoItemName = new String[dataInfo.size()];
                            onShow = new boolean[dataInfo.size()];
                            int i = 0;
                            for (DataInfoItem dataInfoItem : dataInfo) {
                                dataInfoItemName[i] = dataInfoItem.getCNName(); // 汉字名称
                                dataInfoItem.setOnShow(false); // OnShow字段全部赋值为false，就是未选中状态
                                dataInfoItem.setDataValue(""); // DataValue字段全部赋值为“”，就是空字符串
                                onShow[i] = false; // onShow数组每一项都赋值为false
                                i++;
                            }
                        }
                        // 先从接口里面取出所有的未选中的数据，然后通过getDataList取出上一次已经操作过的数据
                        List<DataInfoItem> dataInfos = getDataList("dataList", DataInfoItem.class);
                        for (int i = 0; i < dataInfo.size(); i++) {
                            for (int j = 0; j < dataInfos.size(); j++) {
                                if (dataInfo.get(i).getID() == dataInfos.get(j).getID()) {
                                    // 这两句代码先set一下，然后再is(get)一下，是不是可以和二为一，直接写成一句代码
                                    // 把选中的标识加到dataInfo的身上，目的在于重新进入的时候进行选中的标记。
                                    dataInfo.get(i).setOnShow(dataInfos.get(j).isOnShow());
                                    // 是否选中的数组里面的数据也进行相应的变化。
                                    onShow[i] = dataInfo.get(i).isOnShow();
                                }
                            }
                        }
                    } else {
                        toast(object1.getString("Message"));
                    }
                    dataInfoOnShow.clear();
                    // 遍历dataInfo，将第一次或者已经选择过的集合添加到——适配器相关集合中
                    for (DataInfoItem dataInfoItem : dataInfo) {
                        if (dataInfoItem.isOnShow()) {
                            dataInfoOnShow.add(dataInfoItem);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    // 得到getDataInfo之后的显示数据
    private void getData2() {

        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadDeviceJKData");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadDeviceJKData");
        }
        String guidString = getSharedPreferences("UserInfo", 0).getString("guid", "");
        String equNo = getSharedPreferences("device", Context.MODE_PRIVATE).getString("EquipmentNo", "");
        params.addBodyParameter("guid", guidString);
        params.addBodyParameter("equNo", equNo);
        params.addBodyParameter("userId", getSharedPreferences("UserInfo", 0).getInt("UserID", 0) + "");
        x.http().get(params, new CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast("服务器异常");
            }

            @Override
            public void onFinished() { }

            @Override
            public void onSuccess(String arg0) {
                Log.e("==设备监控返回值==", arg0);
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        Intent intent = new Intent(mActivity, LoginActivity.class);
                        startActivity(intent);
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")) {
                        JSONArray array = new JSONArray(object1.getString("Equipments"));
                        JSONObject object = array.getJSONObject(0);
                        for (DataInfoItem dataInfoItem : dataInfo) {
                            /*if (object.toString().contains(dataInfoItem.getENName())) {
                                if(object.has(dataInfoItem.getENName())){
                                    dataInfoItem.setDataValue(
                                            object.get(dataInfoItem.getENName()).toString().equals("null")
                                                    ? "0.0" : object.get(dataInfoItem.getENName()).toString());
                                } else {
                                    dataInfoItem.setDataValue("0.0");
                                }
                            }*/
                            if(object.has(dataInfoItem.getENName())){
                                dataInfoItem.setDataValue(
                                        object.get(dataInfoItem.getENName()).toString().equals("null")
                                                ? "0.0" : object.get(dataInfoItem.getENName()).toString());
                            } else {
                                dataInfoItem.setDataValue("无数据");
                            }
                        }
                        JSONArray array2 = new JSONArray(object1.getString("Pump"));
                        JSONObject object2 = array2.getJSONObject(0);
                        for (DataInfoItem dataInfoItem : dataInfo) {
                            if (object2.toString().contains(dataInfoItem.getENName())) {
                                dataInfoItem.setDataValue(object2.get(dataInfoItem.getENName()).toString());
                            }
                        }
                    } else {
                        toast(object1.getString("Message"));
                    }
                    mAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    class mGridViewAdapter extends BaseAdapter {
        private Context context;
        private List<DataInfoItem> listItem;

        public mGridViewAdapter(Context context, List<DataInfoItem> listItem) {
            this.context = context;
            this.listItem = listItem;
        }

        @Override
        public int getCount() {
            return listItem.size();
        }

        @Override
        public Object getItem(int position) {
            return listItem.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.layout_grid_view_item, null);
            }
            TextView data_name = (TextView) convertView.findViewById(R.id.data_name);
            TextView data_value = (TextView) convertView.findViewById(R.id.data_value);

            DataInfoItem infoItem = listItem.get(position);
            if (infoItem.getENName().contains("PumpState")) {
                String text = "";
                data_name.setText(infoItem.getCNName().replace("\r\n", "") + "信息:");
                if (!infoItem.getDataValue().isEmpty() && infoItem.getDataValue().length() > 0) {
                    String a = infoItem.getDataValue().replaceAll("\"", "")
                            .replace("[", "").replace("]", "");
                    String[] e = a.split(",");
                    text = "运行状态:" + e[0]
                            + "\n运行方式:" + e[1]
                            + "\n运行电流:" + e[2] + "A";
                }

                data_value.setText(text);
            } else {
                if (infoItem.getUnit().isEmpty() || infoItem.getUnit().equals("")
                        || infoItem.getUnit().equals(null) || infoItem.getUnit().equals("#")) {
                    data_name.setText(infoItem.getCNName().replace("\r\n", ""));
                } else {
                    data_name.setText(infoItem.getCNName().replace("\r\n", "")
                            + ":(" + infoItem.getUnit() + ")");
                }
                data_value.setText(infoItem.getDataValue());
            }
            return convertView;
        }
    }

    @Override
    protected void onStart() {
        startTask();
        initView();
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
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void toast(String text){
        Toast.makeText(DeviceStateDynamicActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    // 保存List
    public <T> void setDataList(String tag, List<T> datalist) {
        if (null == datalist || datalist.size() <= 0) return;
        Gson gson = new Gson();
        // 转换成json数据，再保存
        String strJson = gson.toJson(datalist);
        editor.clear();
        editor.putString(tag, strJson);
        editor.apply();
        Log.e("===set===", strJson);
    }

    // 获取List
    public <T> List<T> getDataList(String tag, Class<T> cls) {
        List<T> datalist = new ArrayList<T>();
        String strJson = preferences.getString(tag, null);
        if (null == strJson) {
            return datalist;
        } else {
            Log.e("===get===", strJson);
        }
        Gson gson = new Gson();
        JsonArray arry = new JsonParser().parse(strJson).getAsJsonArray();
        for (JsonElement jsonElement : arry) {
            datalist.add(gson.fromJson(jsonElement, cls));
        }
        return datalist;
    }
}
