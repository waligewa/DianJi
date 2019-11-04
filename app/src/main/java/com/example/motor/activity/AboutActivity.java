package com.example.motor.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.base.BaseActivity;
import com.example.motor.util.Loadding;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

public class AboutActivity extends BaseActivity {
    @ViewInject(R.id.wv_about)
    WebView webView;
    private Loadding loading;
    private Intent intent;

    private String videoUrl = "http://47.93.6.250:10048/UploadImg/mp4/201805311749553130.mp4";
    private String html = "<html>视频如下：<br/><a href='" + videoUrl
            + "' width='650' height='450' frameborder='0'><img src='" + "' /></a></html>";

    // layout文件
    @Override
    public int setLayout() {
        return R.layout.activity_about;
    }

    // 在这里面设置TitleBar相关的
    @Override
    public void setTitle() {
        mBaseTitleBarView.setTitleText("公司简介");
        mBaseTitleBarView.setLeftDrawable(-1);
    }

    // 初始化控件，设置监听
    @SuppressLint("JavascriptInterface")
    @Override
    public void initWidget() {
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);
        init();
    }

    // View的click事件
    @Override
    public void widgetClick(View v) {}

    // 返回键事件
    @Override
    public void onTitleLeftPressed() {
        onBackPressed();
    }

    private void init() {
        intent = new Intent();
        loading = new Loadding(this);
        loading.show("正在拼命加载中...");
        webView.loadUrl("file:///android_asset/about.html");
        //webView.loadUrl("http://47.93.6.250:10048/UploadImg/mp4/201805311749553130.mp4");
        webView.setWebViewClient(new HelloWebViewClient());
        webView.setHorizontalScrollBarEnabled(false);// 水平不显示
        webView.setVerticalScrollBarEnabled(false); // 垂直不显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.setWebChromeClient(new WebChromeClient());
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

    private void toast(String text){
        Toast.makeText(AboutActivity.this, text, Toast.LENGTH_SHORT).show();
    }
}
