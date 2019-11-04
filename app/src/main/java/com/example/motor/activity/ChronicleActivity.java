package com.example.motor.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.ChronicleInfo;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DateTimePickDialogUtil;
import com.example.motor.util.DateUtils;
import com.example.motor.util.Loadding;
import com.example.motor.util.SPUtils;
import com.example.motor.widget.BarChartView;
import com.example.motor.widget.DountChartView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 2018年11月19日已废弃，故障记录的历史数据界面
 *
 */
public class ChronicleActivity extends Activity implements View.OnClickListener {

    @ViewInject(R.id.chronicl_back)  // chronicle编年史
    LinearLayout back;
    @ViewInject(R.id.chronicl_title)
    TextView title;
    @ViewInject(R.id.bt_chack_charts)
    ImageView replaceCharts;
    @ViewInject(R.id.tv_time_point)
    TextView timePoint;
    @ViewInject(R.id.fault_details)
    TextView faultDetails;
    @ViewInject(R.id.lv_chronicle)
    ListView mListView;
    @ViewInject(R.id.time_choice)
    RadioGroup timeChoice;
    @ViewInject(R.id.time_week)
    RadioButton timeWeek;
    @ViewInject(R.id.time_month)
    RadioButton timeMonth;
    @ViewInject(R.id.time_year)
    RadioButton timeYear;
    @ViewInject(R.id.chronicl_dount_char)
    LinearLayout chroniclDountChar;  // 编年史圆形环图
    @ViewInject(R.id.search)
    LinearLayout search;

    DountChartView mDountChartView;  // 圆形环图
    BarChartView mBarChartView;  // 柱状图
    List<ChronicleInfo> infos = new ArrayList<>();
    SimpleAdapter mSimpleAdapter;
    List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();

    private Loadding loading;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  // 设置日期格式
    DateTimePickDialogUtil dateTimePicKDialog;
    int Tag = 0;
    private SharedPreferences prefs1;
    private String gatewayAddress;
    private Intent intent;
    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chronicle);
        MyApplication.getInstance().addActivity(this);
        x.view().inject(this);

        back.setOnClickListener(this);
        timePoint.setOnClickListener(this);  // 时间点
        faultDetails.setOnClickListener(this);  // 故障详情
        search.setOnClickListener(this);
        dateTimePicKDialog = new DateTimePickDialogUtil(this, df.format(new Date()));
        timePoint.setText(df.format(new Date()));
        // RadioGroup的点击事件开始了
        timeChoice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 根据日期，设备ID，获取报警信息
                getFaultData();
            }
        });
        DateTimePickDialogUtil.setOnItemSelectChangeListener(
                new DateTimePickDialogUtil.OnSetTimeChangeListener() {
            @Override
            public void OnSetTimeChange() {
                // 根据日期，设备ID，获取报警信息
                getFaultData();
            }
        });
        findViewById(R.id.chack_charts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tag = Tag == 0 ? 1 : 0;
                replaceCharts.setRotation(Tag == 0 ? 0 : 180); // rotation 旋转。让这个图片旋转180度。
                setChart();
            }
        });
        if (android.os.Build.VERSION.SDK_INT < 19) {
            findViewById(R.id.chack_charts).setVisibility(View.GONE);
        }
    }

    private void initData() {
        prefs1 = getSharedPreferences("UserInfo", 0);
        gatewayAddress = prefs1.getString("add", "");
        loading = new Loadding(this);
        intent = new Intent();
        mActivity = this;
        title.setText(getSharedPreferences("device", Context.MODE_PRIVATE)
                .getString("comName", "故障记录") + " 历史故障");
        mSimpleAdapter = new SimpleAdapter(this, listItems,
                R.layout.chronicle_list_item, new String[]{"name", "num"},
                new int[]{R.id.chronicle_name, R.id.chronicle_num});
        mListView.setAdapter(mSimpleAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // 这句是因为listview的第一项是手动添加的一项，不应该让其存在按钮点击事件
                // if(i == 0) return;
                intent.setClass(mActivity, ChronicleActivity1.class);
                intent.putExtra("fault_name", String.valueOf(listItems.get(i).get("name")));
                intent.putExtra("start_date", getDate());
                intent.putExtra("end_date", timePoint.getText().toString());
                startActivity(intent);
            }
        });
        // 根据日期，设备ID，获取报警信息
        getFaultData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chronicl_back:
                finish();
                break;
            case R.id.tv_time_point:
                dateTimePicKDialog.dateTimePicKDialog(timePoint);
                break;
            case R.id.search:
                intent.setClass(this, ChronicleActivity4.class);
                startActivity(intent);
                break;
            /*case R.id.fault_details:
                Intent intent = new Intent(ChronicleActivity.this, ChronicleActivity1.class);
                startActivity(intent);
                break;*/
        }
    }

    // 根据日期，设备ID，获取报警信息
    private void getFaultData() {
        loading.show("加载数据中...");
        infos.clear();
        listItems.clear();
        /*Map<String, Object> listItem1 = new HashMap<>();
        listItem1.put("name", "故障类型");
        listItem1.put("num", "故障数量");
        listItems.add(listItem1);
        mSimpleAdapter.notifyDataSetChanged();*/
        mBarChartView = null;
        mDountChartView = null;
        chroniclDountChar.removeAllViews(); // dount 甜甜圈  chronicle  编年史
        chroniclDountChar.removeAllViewsInLayout();
        RequestParams params;
        if (TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadEventDate");
        } else {
            params = new RequestParams(gatewayAddress + "Service/C_WNMS_API.asmx/LoadEventDate");
        }
        String guidString = prefs1.getString("guid", "");
        String equID = getSharedPreferences("device", Context.MODE_PRIVATE).getString("EquipmentID", "");
        params.addBodyParameter("endDate", timePoint.getText().toString());
        params.addBodyParameter("beginDate", getDate());
        params.addBodyParameter("guid", guidString);
        params.addBodyParameter("equID", equID);
        x.http().get(params, new CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) { }

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast(ex.getMessage());
                if (loading.isShow()) {
                    loading.close();
                }
            }

            @Override
            public void onFinished() {}

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        Intent intent = new Intent(ChronicleActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        toast(object1.getString("Message"));
                    } else if (object1.getString("Code").equals("1")) {
                        JSONArray array = new JSONArray(object1.getString("Data"));
                        int sum = 0, max = 0;
                        Map<String, Object> listItem2;
                        // 从此往下的到空行处都是故障记录的排序代码
                        List<JSONObject> jsonList = new ArrayList<>();
                        for(int i = 0; i < array.length(); i++){
                            jsonList.add(array.getJSONObject(i));
                        }
                        Collections.sort(jsonList, new Comparator<JSONObject>() {
                            private static final String key = "num";
                            public int compare(JSONObject a, JSONObject b) {
                                Integer valA = null;
                                Integer valB = null;
                                try {
                                    valA = a.getInt(key);
                                    valB = b.getInt(key);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                // 此处如果是：valA.compareTo(valB)指的是从小到大排序。
                                return valB.compareTo(valA);  // 从大到小排序
                            }
                        });
                        // 把jsonList.toString()放进JSONArray里面的时候我心里也没谱，抱着试一试的心态来做的，没想到成了（18.11.13）
                        JSONArray array2 = new JSONArray(jsonList.toString());

                        for (int i = 0; i < array2.length(); i++) {
                            JSONObject object = array2.getJSONObject(i);
                            ChronicleInfo info = new ChronicleInfo();
                            info.setEventMessage(object.get("EventMessage").toString().trim());
                            info.setNum(object.getInt("num"));
                            sum = sum + object.getInt("num");
                            if (object.getInt("num") > max) {
                                max = object.getInt("num");
                            }
                            listItem2 = new HashMap<>();
                            listItem2.put("name", object.get("EventMessage").toString().trim());
                            listItem2.put("num", object.getInt("num"));
                            listItems.add(listItem2);
                            infos.add(info);
                        }
                        mSimpleAdapter.notifyDataSetChanged();
                        mBarChartView = new BarChartView(ChronicleActivity.this, max, infos);
                        mDountChartView = new DountChartView(ChronicleActivity.this, sum, infos);
                        setChart();
                        // 下面注释的是如果秀姐写排序，就直接用下面的代码
                        /*for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            ChronicleInfo info = new ChronicleInfo();
                            info.setEventMessage(object.get("EventMessage").toString().trim());
                            info.setNum(object.getInt("num"));
                            sum = sum + object.getInt("num");
                            if (object.getInt("num") > max) {
                                max = object.getInt("num");
                            }
                            listItem2 = new HashMap<String, Object>();
                            listItem2.put("name", object.get("EventMessage").toString().trim());
                            listItem2.put("num", object.getInt("num"));
                            listItems.add(listItem2);
                            infos.add(info);
                        }
                        mSimpleAdapter.notifyDataSetChanged();
                        mBarChartView = new BarChartView(ChronicleActivity.this, max, infos);
                        mDountChartView = new DountChartView(ChronicleActivity.this, sum, infos);
                        setChart();*/
                    } else {
                        mSimpleAdapter.notifyDataSetChanged();
                        toast(object1.getString("Message"));
                    }
                } catch (Exception e) {
                    mSimpleAdapter.notifyDataSetChanged();
                    e.printStackTrace();
                }
                if (loading.isShow()) {
                    loading.close();
                }
            }
        });
    }

    // RadioGroup的点击事件
    private String getDate() {
        String endDate = "";
        try {
            if (timeWeek.isChecked()) {
                endDate = DateUtils.getPreviousOrNextDateOfTheDay(timePoint.getText().toString(),
                        DateUtils.USE_WEEK);
            } else if (timeMonth.isChecked()) {
                endDate = DateUtils.getPreviousOrNextDateOfTheDay(timePoint.getText().toString(),
                        DateUtils.USE_MONTH);
            } else if (timeYear.isChecked()) {
                endDate = DateUtils.getPreviousOrNextDateOfTheDay(timePoint.getText().toString(),
                        DateUtils.USE_YEAR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return endDate;
    }

    private void setChart() {
        // RadioGroup
        chroniclDountChar.removeAllViewsInLayout();
        if (Tag == 0) {
            DisplayMetrics dm = getResources().getDisplayMetrics();// metrics 韵律学
            int scrWidth = (int) (dm.widthPixels);
            int scrHeight = (int) (dm.heightPixels * 0.35);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(scrWidth, scrHeight);
            if (mDountChartView != null) {
                // 图表view放入布局中，也可直接将图表view放入Activity对应的xml文件中
                chroniclDountChar.addView(mDountChartView, layoutParams);
            }
        } else {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            int scrWidth = (int) (dm.widthPixels * 0.97);
            int scrHeight = (int) (dm.heightPixels * 0.32);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(scrWidth, scrHeight);
            if (mDountChartView != null) {
                // 图表view放入布局中，也可直接将图表view放入Activity对应的xml文件中
                chroniclDountChar.addView(mBarChartView, layoutParams);
            }
        }
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        initData();
    }

    private void toast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
