package com.example.motor.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.motor.R;
import com.example.motor.constant.ConstantsField;
import com.example.motor.util.CommenUrl;
import com.example.motor.util.Loadding;
import com.example.motor.util.SPUtils;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 *
 * 直饮水设备动画的activity
 *
 */
public class DrinkWaterAnimationActivity extends Activity implements View.OnClickListener {

    @ViewInject(R.id.webview)
    LinearLayout webViewLayout;
    @ViewInject(R.id.graph_title)
    TextView titleTextView;
    @ViewInject(R.id.back)
    LinearLayout back;
    private String EquipmentID, gatewayAddress, equipmentType;
    private SharedPreferences prefs1, prefs2;
    private Loadding loading;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_water_animation);
        x.view().inject(this);
        back.setOnClickListener(this);
        initView();
    }

    private void initView() {
        mContext = this;
        prefs1 = getSharedPreferences("UserInfo", 0);
        prefs2 = getSharedPreferences("device", Context.MODE_PRIVATE);
        gatewayAddress = prefs1.getString("add", "");
        equipmentType = prefs2.getString("EquipmentType", "");
        titleTextView.setText(getSharedPreferences("device", Context.MODE_PRIVATE).getString("comName", "设备曲线图"));
        EquipmentID = prefs2.getString("EquipmentID", "");
        loading = new Loadding(this);
        if (!EquipmentID.isEmpty() && !EquipmentID.equals("")) {
            initData();
        }
    }

    private void initData() {
        loading.show("加载数据中...");
        WebView webView = new WebView(mContext);
        webViewLayout.addView(webView);

        WebSettings webSettings = webView.getSettings();// 获取webview设置属性
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);// 把html中的内容放大webview等宽的一列中
        webSettings.setBuiltInZoomControls(true); // 显示放大缩小
        webSettings.setSupportZoom(true); // 可以缩放

        webView.setBackgroundColor(0);   // 背景透明
        // 设置WebView属性，能够执行Javascript脚本
        webView.getSettings().setJavaScriptEnabled(true);
        // 不缓存
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.setInitialScale(150);
        // 设置Web视图
        webView.setWebViewClient(new HelloWebViewClient());
        if(TextUtils.isEmpty(SPUtils.getStringData(this, ConstantsField.ADDRESS))){
            //webView.loadUrl(CommenUrl.iPSetString + "Single/gui/" + EquipmentID);
            webView.loadUrl(CommenUrl.iPSetString + "Single/gui?" + "EquipmentID=" + EquipmentID + "&Guitype=2");
        } else {
            //webView.loadUrl(gatewayAddress + "Single/gui/" + EquipmentID);
            webView.loadUrl(gatewayAddress + "Single/gui?" + "EquipmentID=" + EquipmentID + "&Guitype=2");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            default:
                break;
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
            }
        }
    }
}
