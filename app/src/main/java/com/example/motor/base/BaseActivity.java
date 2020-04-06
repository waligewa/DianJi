package com.example.motor.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;

import com.example.motor.R;
import com.example.motor.base.title_bar.BaseTitleBarView;

/**
 * Author : yanftch
 * Date   : 2018/1/19
 * Time   : 09:18
 * Desc   :
 */
public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener,
        BaseTitleBarView.TitleBarClick {

    private static String TAG = "";
    protected Context context;
    private ViewGroup mWindowRootLayout;
    protected BaseTitleBarView mBaseTitleBarView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);// 取消标题
        context = this;
        TAG = "tag_" + context.getClass().getSimpleName();
        Log.e(TAG, "onCreate.activityName = " + getClass().getSimpleName());
        int resId = setLayout();
        View view = View.inflate(this, resId, null);
        setContentView(resId);
        initTitle();
        setTitle();
        initWidget();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.base_layout);
        mWindowRootLayout = (ViewGroup) findViewById(R.id.window_root_layout);

        mBaseTitleBarView = (BaseTitleBarView) findViewById(R.id.base_title_bar);

        View contentView = LayoutInflater.from(this).inflate(layoutResID, null);
        mWindowRootLayout.addView(contentView, new RelativeLayout.LayoutParams(ViewGroup
                .LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if (null != mBaseTitleBarView) {
            mBaseTitleBarView.setTitleBarClick(this);
            mBaseTitleBarView.setLeftContainerVisible();
        }
    }

    protected <T extends View> T findView(int resId) {
        return (T) (findViewById(resId));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void initTitle() {
    }

    @Override
    public void onClick(View v) {
        //  相当于通过这个方法将onClick方法给回调回去
        widgetClick(v);
    }

    /**
     * 布局ID
     *
     * @return
     */
    public abstract int setLayout();

    /**
     * 设置标题显示的内容
     */
    public abstract void setTitle();

    /**
     * 初始化控件
     */
    public abstract void initWidget();

    /**
     * 添加事件
     */
    public abstract void widgetClick(View v);

    @Override
    public void onTitlePressed() {}

    @Override
    public void onTitleLeftPressed() {}

    @Override
    public void onTitleRightTextPressed() {}

    @Override
    public void onTitleRightImagePressed() {}

    public void startActivityAndFinish(Class<?> clazz) {
        startActivityAndFinish(clazz, null);
    }

    public void startActivityAndFinish(Class<?> clazz, Bundle bundle) {
        startActivity(clazz, bundle);
        this.finish();
    }

    public void startActivity(Class<?> clazz, Bundle bundle) {
        Intent intent = new Intent();
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        intent.setClass(getApplicationContext(), clazz);
        startActivity(intent);
    }
}