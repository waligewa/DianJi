package com.example.motor.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.util.Loadding;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 *
 * Author : 赵彬彬
 * Date   : 2020/2/26
 * Time   : 16:47
 * Desc   : 反渗透直饮水设备动画activity
 */
public class ROSDrinkWaterDeviceAnimationActivity extends AppCompatActivity {

    @ViewInject(R.id.webview)
    WebView mWebView;
    private Context mContext;
    private Loadding loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        // 不显示系统的标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_drink_water_animation2);
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);
        initView();
    }

    private void initView() {
        mContext = this;
//        setTitleViewVisible(false);
        loading = new Loadding(this);
        initData();
    }

    private void initData() {
        loading.show("加载数据中...");
        WebSettings webSettings = mWebView.getSettings();                                           // 获取webview设置属性
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);                  // 把html中的内容放大webview等宽的一列中
        webSettings.setBuiltInZoomControls(true);                                                   // 显示放大缩小
        webSettings.setSupportZoom(true);                                                           // 可以缩放

        mWebView.setBackgroundColor(0);                                                             // 背景透明
        // 设置WebView属性，能够执行Javascript脚本
        mWebView.getSettings().setJavaScriptEnabled(true);
        // 不缓存
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.setInitialScale(150);
        // 设置Web视图
        mWebView.setWebViewClient(new HelloWebViewClient());
//        mWebView.loadUrl("http://www.zlwart.com");
        mWebView.loadUrl("http://47.93.6.250:9000/GUI/zhiyinshui/index-zys.html");
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
                        .hideSoftInputFromWindow(mWebView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
}
