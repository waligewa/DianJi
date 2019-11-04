package com.example.motor.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.motor.R;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

public class WebviewActivity extends Activity implements View.OnClickListener {

    @ViewInject(R.id.graph_title)
    TextView titleTextView;
    @ViewInject(R.id.back)
    LinearLayout back;
    @ViewInject(R.id.webview)
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        x.view().inject(this);
        back.setOnClickListener(this);
        initView();
    }

    private void initView() {
        titleTextView.setText(getSharedPreferences("device", Context.MODE_PRIVATE)
                .getString("comName", "设备曲线图"));
        initData();
    }

    private void initData() {
        // 设置WebView属性，能够执行Javascript脚本
        webView.getSettings().setJavaScriptEnabled(true);
        // 不缓存
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        // 设置Web视图
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("http://218.17.198.162:10041/UploadImg/mp4/201810102107230597.mp4");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
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
}
