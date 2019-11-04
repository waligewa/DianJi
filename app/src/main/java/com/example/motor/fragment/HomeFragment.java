package com.example.motor.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.motor.R;
import com.example.motor.activity.ChronicleActivity2;
import com.example.motor.activity.DataAnalysisActivity;
import com.example.motor.activity.DetailTaskActivity3;
import com.example.motor.activity.DeviceAnimationActivity;
import com.example.motor.activity.DeviceStateDynamicActivity;
import com.example.motor.activity.DrinkWaterAnimationActivity;
import com.example.motor.activity.DrinkingWaterActivity;
import com.example.motor.activity.GraphActivity;
import com.example.motor.activity.HomeDevicePositionActivity;
import com.example.motor.activity.LoginActivity;
import com.example.motor.activity.MainActivity;
import com.example.motor.activity.MaintenanceManagementActivity;
import com.example.motor.activity.MotorMonitoringActivity;
import com.example.motor.activity.PendingTaskActivity;
import com.example.motor.banner.LocalImageHolderView;
import com.example.motor.constant.ConstantsField;
import com.example.motor.db.TaskItemBean;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.DoubleClickUtils;
import com.example.motor.util.NetWorkUtil;
import com.example.motor.util.SPUtils;
import com.example.motor.util.SpinWindowAreaUntil;
import com.example.motor.banner.convenientbanner.ConvenientBanner;
import com.example.motor.banner.convenientbanner.holder.CBViewHolderCreator;
import com.example.motor.banner.convenientbanner.listener.OnItemClickListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * Author : 赵彬彬
 * Date   : 2019/6/14
 * Time   : 0:09
 * Desc   : 首页fragment
 */
public class HomeFragment extends Fragment implements View.OnClickListener {

    private TextView seeMore, commonEntry, tvTitle, tvTime, tvMusic;
    public static TextView titleTextView, influentPressure, cumulativeFlow, waterPressure,
    eqPower, accumulativePowerConsumption, conductivity, pumpOrPressure, setPressure, phValue,
            residualChlorine, turbidity, orpValue, salinity, dissolvedOxygen, waterQuantity;
    private LinearLayout navigation;
    private SharedPreferences prefs1, prefs2;
    private SharedPreferences.Editor editor;
    private String guidString, address, equNo, userId, authorityString, equipmentType;
    private RelativeLayout breakNote, animation, runCurve, dataAnalysis, motorPosition, deviceDetails;
    private LinearLayout linearLayout1, pendingTask, pendingTaskTotal;
    public static LinearLayout linearLayout2, linearLayout3;
    private Intent intent;
    private ConvenientBanner convenientBanner;                                                        // 顶部广告栏控件
    private LinkedList<Integer> localImages = new LinkedList<Integer>();
//  很多的Java初学者不明白ArrayList与LinkedList之间的区别，所以，他们完全只用相对简单的ArrayList，
//  甚至不知道JDK中还存在LinkedList。但是，在某些具体场景下，这两种List的选择会导致程序性能的巨大
//  差异。简单而言：当应用场景中有很多的add/remove操作，只有少量的随机访问操作时，应该选择
//  LinkedList;在其他的场景下，考虑使用ArrayList。
    private List<TaskItemBean> datas = new LinkedList<>();                                            // 维修进行中任务集合
    private List<TaskItemBean> datas2 = new LinkedList<>();                                           // 巡检进行中任务集合
    private int TIME = 5 * 1000;
    private boolean running = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view1 = inflater.inflate(R.layout.main_home_fragment, null);
        x.view().inject(getActivity());
        titleTextView = (TextView) view1.findViewById(R.id.ic_a_title);
//      设备列表的导航栏使用
        navigation = (LinearLayout) view1.findViewById(R.id.navigation);
//      故障记录、设备动画、运行曲线、数据分析的点击事件
        breakNote = (RelativeLayout) view1.findViewById(R.id.break_note);
        animation = (RelativeLayout) view1.findViewById(R.id.animation);
        runCurve = (RelativeLayout) view1.findViewById(R.id.run_curve);
        dataAnalysis = (RelativeLayout) view1.findViewById(R.id.data_analysis);
        motorPosition = (RelativeLayout) view1.findViewById(R.id.motor_position);
        deviceDetails = (RelativeLayout) view1.findViewById(R.id.device_details);
        linearLayout1 = (LinearLayout) view1.findViewById(R.id.linearLayout1);
        linearLayout2 = (LinearLayout) view1.findViewById(R.id.linearlayout2);
        linearLayout3 = (LinearLayout) view1.findViewById(R.id.linearlayout3);
//      进入任务详情的按钮和查看更多的按钮、共有条目的按钮
        pendingTask = (LinearLayout) view1.findViewById(R.id.pending_task);
        pendingTaskTotal = (LinearLayout) view1.findViewById(R.id.pending_task_total);
        seeMore = (TextView) view1.findViewById(R.id.see_more);
        commonEntry = (TextView) view1.findViewById(R.id.common_entry);
//      进水压力、累计流量、0出水压力、瞬时流量、设定压力、累计电耗、设定流量
        influentPressure = (TextView) view1.findViewById(R.id.influent_pressure);                            // 进水压力
        cumulativeFlow = (TextView) view1.findViewById(R.id.cumulative_flow);
        waterPressure = (TextView) view1.findViewById(R.id.water_pressure);                                  // 出水压力
        eqPower = (TextView) view1.findViewById(R.id.eq_power);                                              // 设备功率
        accumulativePowerConsumption = (TextView) view1.findViewById(R.id.accumulative_power_consumption);
        pumpOrPressure = (TextView) view1.findViewById(R.id.pump_or_pressure);                               // 泵站或者无负压
        setPressure = (TextView) view1.findViewById(R.id.set_pressure);                                      // 设定流量或者设定压力
        conductivity = (TextView) view1.findViewById(R.id.conductivity);                                     // 电导率
        phValue = (TextView) view1.findViewById(R.id.ph_value);                                              // ph值
        residualChlorine = (TextView) view1.findViewById(R.id.residual_chlorine);                            // 余氯
        turbidity = (TextView) view1.findViewById(R.id.turbidity);                                           // 浊度
        orpValue = (TextView) view1.findViewById(R.id.orp_value);                                            // orp值
        salinity = (TextView) view1.findViewById(R.id.salinity);                                             // 盐度
        dissolvedOxygen = (TextView) view1.findViewById(R.id.dissolved_oxygen);                              // 溶解氧
        waterQuantity = (TextView) view1.findViewById(R.id.water_quality);                                   // 水质硬度
//      待办任务块里面的从上到下3个TextView
        tvTitle = (TextView) view1.findViewById(R.id.tv_title);
        tvTime = (TextView) view1.findViewById(R.id.tv_time);
        tvMusic = (TextView) view1.findViewById(R.id.tv_music);
        init();
        initBanner(view1);
        if(!NetWorkUtil.isNetworkConnected(getActivity())){
            toast("无网络");
        } else {
//          待办任务的接口
            getData2();
        }
        return view1;
    }

    private void init(){
        intent = new Intent();
        prefs1 = getActivity().getSharedPreferences("device", Context.MODE_PRIVATE);
        prefs2 = getActivity().getSharedPreferences("UserInfo", 0);
        editor = prefs2.edit();
//      标题的赋值为从偏好文件上面赋值（如果提出来的数据为“”的话就赋值为“首页”，如果剔除的数据不为“”
//      的话就赋值为偏好文件提取出来的数据）
        titleTextView.setText(prefs1.getString("comName", "首页").equals("") ? "首页" : prefs1.getString("comName", "首页"));
        address = prefs2.getString("add", "");
        guidString = prefs2.getString("guid", "");
        authorityString = prefs2.getString("authorityrole", "");
        userId = String.valueOf(prefs2.getInt("UserID", 0));
        equipmentType = prefs1.getString("EquipmentType", "");
        if(equipmentType.equals("2")){
            linearLayout2.setVisibility(View.GONE);
            linearLayout3.setVisibility(View.VISIBLE);
        } else if (equipmentType.equals("1")){
            linearLayout2.setVisibility(View.VISIBLE);
            linearLayout3.setVisibility(View.GONE);
        }
//      2018年6月30日，以前一直在HomeFragment中无法实现设备状态即使刷新的功能，这次可以了，原来是
//      以前2了，就是简单的在标题赋值之后走网络请求的方法，这样就可以每次进入这个界面都进行数据的更新，
//      以前那么呆滞，一直没有实现
        if(!titleTextView.getText().toString().equals("首页")){
//          设备状态的赋值操作
            getData();
        }
//      取出authorityString，如果这个字符串里面存在“待办任务”的话就让pendingTaskTotal显示，
//      如果不存在“待办任务”的话就让pendingTaskTotal不显示
        if (!authorityString.contains("待办任务")){
            pendingTaskTotal.setVisibility(View.GONE);
        } else {
            pendingTaskTotal.setVisibility(View.VISIBLE);
        }
        navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SpinWindowAreaUntil.showSpinWindowArea(getActivity(), navigation, titleTextView);
            }
        });
        breakNote.setOnClickListener(this);
        animation.setOnClickListener(this);
        runCurve.setOnClickListener(this);
        dataAnalysis.setOnClickListener(this);
        motorPosition.setOnClickListener(this);
        deviceDetails.setOnClickListener(this);
        linearLayout1.setOnClickListener(this);
        pendingTask.setOnClickListener(this);
        seeMore.setOnClickListener(this);
    }

    public void onClick(View v) {
        String deviceidString = prefs1.getString("deviceId", "");
        equipmentType = prefs1.getString("EquipmentType", "");
        switch (v.getId()) {
//          故障记录
            case R.id.break_note:
//              SpinWindowAreaUntil的mfistCustemInfors肯定为空，因为这是个static变量，在程序启动
//              的时候就会被初始化，就会为null，然后这个mfirstCustemInfors的大小为0，是小于1的
                if (deviceidString.length() < 2 /*|| SpinWindowAreaUntil.mfistCustemInfors.isEmpty()
                        || SpinWindowAreaUntil.mfistCustemInfors.size() < 1*/) {
                    toast("请点击左上角按钮，选择相应设备");
                } else if (!DoubleClickUtils.isFastDoubleClick()) {
                    intent.setClass(getActivity(), ChronicleActivity2.class);
                    startActivity(intent);
                }
                break;
//          设备动画
            case R.id.animation:
                if (deviceidString.length() < 2 /*|| SpinWindowAreaUntil.mfistCustemInfors.isEmpty()
                        || SpinWindowAreaUntil.mfistCustemInfors.size() < 1*/) {
                    toast("请点击左上角按钮，选择相应设备");
                } else if (!DoubleClickUtils.isFastDoubleClick()) {
                    if(!equipmentType.equals("2")){
                        intent.setClass(getActivity(), DeviceAnimationActivity.class);
                        startActivity(intent);
                    } else {
                        intent.setClass(getActivity(), DrinkWaterAnimationActivity.class);
                        startActivity(intent);
                    }
                }
                break;
//          运行曲线
            case R.id.run_curve:
                //startActivity(new Intent(getActivity(), TaskListActivity.class));
                if (deviceidString.length() < 2 /*|| SpinWindowAreaUntil.mfistCustemInfors.isEmpty()
                        || SpinWindowAreaUntil.mfistCustemInfors.size() < 1*/) {
                    toast("请点击左上角按钮，选择相应设备");
                } else if (!DoubleClickUtils.isFastDoubleClick()) {
                    intent.setClass(getActivity(), GraphActivity.class);
                    startActivity(intent);
                }
                break;
//          数据分析
            case R.id.data_analysis:
                if (deviceidString.length() < 2 /*|| SpinWindowAreaUntil.mfistCustemInfors.isEmpty()
                        || SpinWindowAreaUntil.mfistCustemInfors.size() < 1*/) {
                    toast("请点击左上角按钮，选择相应设备");
                } else if (!DoubleClickUtils.isFastDoubleClick()) {
                    intent.setClass(getActivity(), DataAnalysisActivity.class);
                    startActivity(intent);
                }
                break;
//          设备状态
            case R.id.linearLayout1:
                if (deviceidString.length() < 2 /*|| SpinWindowAreaUntil.mfistCustemInfors.isEmpty()
                        || SpinWindowAreaUntil.mfistCustemInfors.size() < 1*/) {
                    toast("请点击左上角按钮，选择相应设备");
                } else if (!DoubleClickUtils.isFastDoubleClick()) {
                    if(equipmentType.equals("1") || equipmentType.equals("3")){
                        intent.setClass(getActivity(), DeviceStateDynamicActivity.class);
                        startActivity(intent);
                    } else if(equipmentType.equals("2")){
                        intent.setClass(getActivity(), DrinkingWaterActivity.class);
                        startActivity(intent);
                    }
                }
                break;
//          电机位置
            case R.id.motor_position:
                if (deviceidString.length() < 2 ) {
                    toast("请点击左上角按钮，选择相应设备");
                } else if (!DoubleClickUtils.isFastDoubleClick()) {
                    if(equipmentType.equals("4")){
                        //intent.setClass(getActivity(), MarkerOverlayActivity.class);
                        intent.setClass(getActivity(), MotorMonitoringActivity.class);
                        startActivity(intent);
                    } else {
                        intent.setClass(getActivity(), HomeDevicePositionActivity.class);
                        //intent.setClass(getActivity(), MotorMonitoringActivity.class);
                        startActivity(intent);
                    }
                }
                break;
//          设备详情
            case R.id.device_details:
                if (deviceidString.length() < 2) {
                    toast("请在首页点击左上角，选择相应设备");
                } else if (!DoubleClickUtils.isFastDoubleClick()) {
                    intent.setClass(getActivity(), MaintenanceManagementActivity.class);
                    startActivity(intent);
                }
                break;
//          进入任务详情
            case R.id.pending_task:
                if (!DoubleClickUtils.isFastDoubleClick() && !((datas.size() + datas2.size()) == 0)) {
                    // 如果datas的长度是0的话就直接返回，因为如果不加这句代码，就会报异常、闪退
                    if(datas.size() == 0) return;
                    TaskItemBean bean = datas.get(0);
                    editor.putString("ownid", bean.getWOID());
                    editor.apply();
                    intent.setClass(getActivity(), DetailTaskActivity3.class);
                    intent.putExtra("taskitembean1", bean);
                    startActivityForResult(intent, 7);// 先加一个requestCode 7
                }
                break;
//          查看更多
            case R.id.see_more:
                if (!DoubleClickUtils.isFastDoubleClick()) {
                    intent.setClass(getActivity(), PendingTaskActivity.class);
                    startActivityForResult(intent, 6);// 先加一个requestCode 6
                }
                break;
            default:
                break;
        }
    }

    /**
     * 设置banner
     */
    private void initBanner(View view) {
        convenientBanner = (ConvenientBanner) view.findViewById(R.id.convenient_banner);
        initImageLoader();
        loadTestDatas();
        convenientBanner.setPages(
                new CBViewHolderCreator<LocalImageHolderView>() {
                    @Override
                    public LocalImageHolderView createHolder() {
                        return new LocalImageHolderView();
                    }
                }, localImages)
//              设置两个点图片作为翻页指示器，不设置则没有指示器，可以根据自己需求自行配合自己的指示器,不需要圆点指示器可用不设
                .setPageIndicator(new int[]{R.drawable.ic_page_indicator, R.drawable.ic_page_indicator_focused})
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) { }
                });
    }

//  初始化网络图片缓存库
    private void initImageLoader() {
//      网络图片例子,结合常用的图片缓存库UIL,你可以根据自己需求自己换其他网络图片库
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().
                showImageForEmptyUri(R.drawable.ic_default_adimage)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(getActivity()
                .getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        ImageLoader.getInstance().init(config);
    }

    private void loadTestDatas() {
//      本地图片集合
        localImages.clear();
        /*localImages.add(getResId("splash1", R.mipmap.class));
        localImages.add(getResId("splash2", R.mipmap.class));
        localImages.add(getResId("splash3", R.mipmap.class));
        localImages.add(R.mipmap.splash4);
        localImages.add(R.mipmap.splash5);*/
        localImages.add(getResId("banner1", R.drawable.class));
        localImages.add(getResId("banner2", R.drawable.class));
        localImages.add(getResId("banner3", R.drawable.class));
        localImages.add(R.drawable.banner4);
    }

    public static int getResId(String variableName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(variableName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

//  待办任务的接口，这里有个问题，是待办任务显示10条的问题，找来找去，最后发现是pageSize小了，因此我弄了
//  100，以前是10
    private void getData2() {
        datas.clear();
        datas2.clear();
        RequestParams params;
        if(TextUtils.isEmpty(SPUtils.getStringData(getActivity(), ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/LoadWorkOrderList");
        }

        params.addBodyParameter("userID", userId);
        params.addBodyParameter("pageSize", "100");
        params.addBodyParameter("beginDate", "1900-01-01");
        params.addBodyParameter("endDate", "2100-12-31");
        params.addBodyParameter("guid", guidString);
        params.addBodyParameter("pageIndex", "1");
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {}

            @Override
            public void onError(Throwable ex, boolean arg1) {
                //toast(ex.getMessage());
                //toast("没有待办任务");
            }

            @Override
            public void onFinished() {}

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("0")) {
                        toast(object1.getString("Message"));
                        MainActivity.instance2.finish();
                        intent.setClass(getActivity(), LoginActivity.class);
                        startActivity(intent);
                    } else if (object1.getString("Code").equals("1")){
                        Type listType = new TypeToken<List<TaskItemBean>>() {}.getType();
                        Gson gson = new Gson();
//                      维修任务加巡检任务总集合
                        List<TaskItemBean> gsonDatas = gson.fromJson(object1.getString("Data"), listType);
                        for(int i = 0; i < gsonDatas.size(); i++){
//                          这个直接根据TaskActivity里面的维修任务那个摘抄过来
                            if(gsonDatas.get(i).getWOType().equals("1") &&
                                    gsonDatas.get(i).getWOState().equals("false") &&
                                    !gsonDatas.get(i).getWOIssuedUser().equals(userId)){
                                datas.add(gsonDatas.get(i));
                            }
//                          这个直接根据TaskListActivity2里面的巡检任务那个摘抄过来
                            if(gsonDatas.get(i).getWOType().equals("2") &&
                                    gsonDatas.get(i).getWOState().equals("false") &&
                                    gsonDatas.get(i).getIsIssue().equals("false") &&
                                    !gsonDatas.get(i).getWOIssuedUser().equals(userId) &&
                                    TextUtils.isEmpty(gsonDatas.get(i).getDevCheckID())){
                                datas2.add(gsonDatas.get(i));
                            }
                        }
                        commonEntry.setText(Html.fromHtml("您共有"+ "<big>" + datas.size() + "</big>" + "条维修待办任务和"
                                + "<big>" + datas2.size() + "</big>" + "条巡检待办任务"));
                        seeMore.setText("(" + gsonDatas.size() + ")" + "查看更多");
//                      10月22日，这里有个数组下标越界异常
                        if(datas.size() > 0){
                            tvTitle.setText(TextUtils.isEmpty(datas.get(0).getWOTitle()) ? "标题：" :
                                    "标题：" + datas.get(0).getWOTitle());
                            tvMusic.setText(TextUtils.isEmpty(datas.get(0).getWOContent()) ? "内容：" :
                                    "内容：" + datas.get(0).getWOContent());
                            tvTime.setText(TextUtils.isEmpty(datas.get(0).getWOIssuedDate()) ? "时间：" :
                                    ("时间：" + datas.get(0).getWOIssuedDate().split("T")[0] + " " +
                                            datas.get(0).getWOIssuedDate().split("T")[1].substring(0, 8)));
                        } else {
                            tvTitle.setText("无维修待办任务");
                            tvMusic.setText("无维修待办任务");
                            tvTime.setText("无维修待办任务");
                        }
                    } else {
                        //toast(object1.getString("Message"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

//  设备状态的赋值操作
    private void getData() {
//      请求参数
        RequestParams params;
        if(TextUtils.isEmpty(SPUtils.getStringData(getActivity(), ConstantsField.ADDRESS))){
            params = new RequestParams(CommenUrl.iPSetString + "Service/C_WNMS_API.asmx/LoadDianJiData");
        } else {
            params = new RequestParams(address + "Service/C_WNMS_API.asmx/LoadDianJiData");
        }
        params.addBodyParameter("EquipmentID", prefs1.getString("EquipmentID", ""));
        params.addBodyParameter("UserID", userId);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onCancelled(CancelledException arg0) {}

            @Override
            public void onError(Throwable ex, boolean arg1) {
                toast("不存在电机状态信息");
                HomeFragment.influentPressure.setText("0");      // 进水压力
                HomeFragment.waterPressure.setText("0");         // 出水压力
                HomeFragment.setPressure.setText("0");           // 设定压力
                HomeFragment.eqPower.setText("0");               // 设备功率
            }

            @Override
            public void onFinished() {}

            @Override
            public void onSuccess(String arg0) {
                try {
                    JSONObject object1 = new JSONObject(arg0);
                    if (object1.getString("Code").equals("1")) {
                        JSONArray ja = object1.getJSONArray("Data");
                        JSONObject js = ja.getJSONObject(0);
                        JSONArray jy = js.getJSONArray("Pumplist");
                        JSONObject jt = jy.getJSONObject(0);
                        HomeFragment.influentPressure.setText(jt.getString("InPDec"));     // 进水压力
                        HomeFragment.waterPressure.setText(jt.getString("OutPDec"));       // 出水压力
                        HomeFragment.setPressure.setText(jt.getString("SetP"));            // 设定压力
                        HomeFragment.eqPower.setText(jt.getString("ScrEquipPower"));       // 设备功率
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        super.onActivityResult(requestCode, resultCode, data);  // 这个super可不能落下，否则可能回调不了
        switch(requestCode){
            case 6:
//              如果既是6又是RESULT_OK的话就可以执行下面的
                if(resultCode == getActivity().RESULT_OK){
//                  待办任务的接口，这里有个问题，是待办任务显示10条的问题，找来找去，
//                  最后发现是pageSize小了，因此我弄了100，以前是10
                    getData2();
                }
                break;
            case 7:
//              如果既是7又是RESULT_OK的话就可以执行下面的
                if(resultCode == getActivity().RESULT_OK){
//                  待办任务的接口，这里有个问题，是待办任务显示10条的问题，找来找去，
//                  最后发现是pageSize小了，因此我弄了100，以前是10
                    getData2();
                }
                break;
        }
    }

    private void toast(String text){
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
//      开始自动翻页
        convenientBanner.startTurning(4000);
    }

    @Override
    public void onPause() {
        super.onPause();
//      停止翻页
        convenientBanner.stopTurning();
    }

    @Override
    public void onStart() {
        super.onStart();
//      startTask();
        titleTextView.setText(prefs1.getString("comName", "首页").equals("") ? "首页" :
                prefs1.getString("comName", "首页"));
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void showPopupMenu(View view) {

//      View当前PopupMenu显示的相对View的位置
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);
//      menu布局
        popupMenu.getMenuInflater().inflate(R.menu.main, popupMenu.getMenu());
//      menu的item点击事件
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.equmentList:
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }
}
