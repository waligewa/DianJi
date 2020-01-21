package com.example.motor.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.motor.R;
import com.example.motor.adapter.MultiChoicAdapter;
import com.example.motor.base.CustemInfo;
import com.example.motor.constant.ConstantsField;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.Loadding;
import com.example.motor.util.SPUtils;
import com.example.motor.util.SpinWindowAreaUntil;
import com.example.motor.widget.MyPopWindow;

import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

/**
 * 2018年8月24日修改了很多，将4个RadioGroup给隐藏掉，直接显示水质分析
 *
 */
public class DataAnalysisActivity extends Activity {

    @ViewInject(R.id.wv_analysis)
    WebView webView;
    @ViewInject(R.id.analysis_title)
    TextView titleTextView;
    @ViewInject(R.id.analysis_set)
    ImageView analysisSet;
    @ViewInject(R.id.analysis_selection)
    RadioGroup analysisSelection;
    @ViewInject(R.id.ConsumptionCost)
    RadioButton consumptionCost;
    @ViewInject(R.id.WaterQualityAnalysis)
    RadioButton waterQualityAnalysis;
    @ViewInject(R.id.WaterConsumption)
    RadioButton waterConsumption;
    @ViewInject(R.id.EnergyConsumption)
    RadioButton energyConsumption;
    private Loadding loading;
    public MultiChoicAdapter mAdapter;  // multi 多种
    public List<CustemInfo> mfistCustemInfors = new ArrayList<>();
    public MyPopWindow mSpinerPopWindow;
    public int pageSize = 20, pageIndex = 1;
    public boolean haveNext = true;
    private boolean mBoolean[] = null;
    private String EquipmentID = "";
    private String EquipmentIDS = "";
    private String gatewayAddress, equipmentType;
    private SharedPreferences prefs1, prefs2;
    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_analysis);
        x.view().inject(this);
        initView();
        // 右上角的点击按钮
        SpinWindowAreaUntil.setOnItemSelectChangeListener(new SpinWindowAreaUntil.OnItemSelectChangeListener() {
            @Override
            public void OnItemSelectChange() {
                EquipmentID = getSharedPreferences("device", Context.MODE_PRIVATE).getString("EquipmentID", "");
                EquipmentIDS = EquipmentID;
                initWebData();
            }
        });
    }

    private void initView() {
        mActivity = this;
        prefs1 = getSharedPreferences("UserInfo", 0);
        prefs2 = getSharedPreferences("device", Context.MODE_PRIVATE);
        equipmentType = prefs2.getString("EquipmentType", "");
        gatewayAddress = prefs1.getString("add", "");
        titleTextView.setText(prefs2.getString("comName", "数据分析"));
        loading = new Loadding(this);
        // 先获取EquipmentIDS
        EquipmentID = getSharedPreferences("device",
                Context.MODE_PRIVATE).getString("EquipmentID", "");
        EquipmentIDS = EquipmentID;
        if (!EquipmentIDS.isEmpty() && !EquipmentIDS.equals("")) {
            Log.e("DataAnalysisActivity", EquipmentIDS);
            initWebData();
        } else {
            toast("请先点击右上角选取设备！");
        }
        analysisSelection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                initWebData();
            }
        });
    }

    @Event(value = { R.id.analysis_back, R.id.analysis_set }, type = View.OnClickListener.class )
    private void btnClick(View view) {
        switch (view.getId()) {
            case R.id.analysis_back:
                finish();
                break;
            case R.id.analysis_set:
                SpinWindowAreaUntil.showSpinWindowArea(this, analysisSet, titleTextView);
                break;
            default:
                break;
        }
    }

    private void initWebData() {
        // loading.show("加载数据中...");

        //WebSettings webSettings = webView.getSettings();// 获取webview设置属性
        //webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);// 把html中的内容放大webview等宽的一列中
        //webSettings.setBuiltInZoomControls(true); // 显示放大缩小
        //webSettings.setSupportZoom(true); // 可以缩放

        webView.setBackgroundColor(0);  //  背景透明
        // 设置WebView属性，能够执行Javascript脚本
        webView.getSettings().setJavaScriptEnabled(true);
        // 不缓存
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        // 设置Web视图
        webView.setWebViewClient(new HelloWebViewClient());
        if (EquipmentIDS.equals("")) {
            toast("您未选择任何设备！\n请先点击右上角选取设备");
        } else {
            if (consumptionCost.isChecked()) {  // 成本分析 消费、耗尽
                if (EquipmentIDS.contains(",")) {
                    toast("只能对一台设备进行成本分析\n您选取的设备过多\n自动选取第一台设备进行分析");
                    EquipmentID = (EquipmentIDS.split(","))[0];
                }
                if(TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
                    //webView.loadUrl(CommenUrl.iPSetString + "ChartPhone/ConsumptionCost/" + EquipmentIDS);
                    String s = CommenUrl.iPSetString + "Single/" + EquipmentIDS;
                    webView.loadUrl(CommenUrl.iPSetString + "Single/" + EquipmentIDS);
                } else {
                    webView.loadUrl(gatewayAddress + "Single/" + EquipmentIDS);
                }
            } else if (waterQualityAnalysis.isChecked()) {  // 水质分析
                if (equipmentType.equals("1") || equipmentType.equals("3")) {
                    if(TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
                        //webView.loadUrl(CommenUrl.iPSetString + "ChartPhone/WaterAnalysis/" + EquipmentIDS);
                        webView.loadUrl(CommenUrl.iPSetString + "Single/szfx/" + EquipmentIDS);
                    } else {
                        webView.loadUrl(gatewayAddress + "Single/szfx/" + EquipmentIDS);
                    }
                } else if (equipmentType.equals("2")) {
                    if(TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
                        webView.loadUrl(CommenUrl.iPSetString + "Single/Zszfx/" + EquipmentIDS);
                    } else {
                        webView.loadUrl(gatewayAddress + "Single/Zszfx/" + EquipmentIDS);
                    }
                }
            } else if (energyConsumption.isChecked()) {  // 能耗分析
                if(TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
                    //webView.loadUrl(CommenUrl.iPSetString + "ChartPhone/EnergyConsumption/" + EquipmentIDS);
                    webView.loadUrl(CommenUrl.iPSetString + "Single/nhfx/" + EquipmentIDS);
                } else {
                    webView.loadUrl(gatewayAddress + "Single/nhfx/" + EquipmentIDS);
                }
            } else if (waterConsumption.isChecked()) {  // 用水量分析
                if(TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
                    //webView.loadUrl(CommenUrl.iPSetString + "ChartPhone/WaterConsumption/" + EquipmentIDS);
                    webView.loadUrl(CommenUrl.iPSetString + "Single/yslfx/" + EquipmentIDS);
                } else {
                    webView.loadUrl(gatewayAddress + "Single/yslfx/" + EquipmentIDS);
                }
            }
        }
    }

    // Web视图
    private class HelloWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        // 开始加载
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
        }

        // 加载结束
        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);
            if (loading.isShow()) {
                loading.close();
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(webView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    private void toast(String text){
        Toast.makeText(DataAnalysisActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
    }
}
