package com.example.motor.workorderlist.repair;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.base.BaseActivity;
import com.example.motor.util.DoubleClickUtils;
import com.example.motor.util.Loadding;
import com.example.motor.workorderlist.FragmentAdapter;

import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

public class RepairWorkOrderActivity extends BaseActivity {

    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private Activity mActivity;
    private Intent intent;
    private Loadding loadding;

    @Override
    public int setLayout() {
        return R.layout.activity_work_repport;
    }

    @Override
    public void setTitle() {
        mBaseTitleBarView.setTitleText("维修待办任务");
        mBaseTitleBarView.setRightTitleGone();
        mBaseTitleBarView.setLeftDrawable(-1);
    }

    @Override
    public void initWidget() {
        x.view().inject(this);
        MyApplication.getInstance().addActivity(this);
        initView();
    }

    @Override
    public void widgetClick(View v) {}

    @Override
    public void onTitleLeftPressed() {
        if(!DoubleClickUtils.isFastDoubleClick()){
            onBackPressed();
        }
    }

    private void initView(){
        intent = new Intent();
        mActivity = this;
        loadding = new Loadding(this);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        initViewPager();
    }

    private void initViewPager() {
        List<String> titles = new ArrayList<>();
        titles.add("执行中");
        titles.add("已转发");
        titles.add("已处理");

        for (int i = 0; i < titles.size(); i++) {
            mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(i)));
        }
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new ExecutingFragment());
        fragments.add(new TransmitedFragment());
        fragments.add(new HandledlFragment());
        FragmentAdapter mFragmentAdapter =
                new FragmentAdapter(getSupportFragmentManager(), fragments, titles);
        // 给ViewPager设置适配器
        mViewPager.setAdapter(mFragmentAdapter);
        mViewPager.setOffscreenPageLimit(fragments.size());
        // 将TabLayout和ViewPager关联起来。
        mTabLayout.setupWithViewPager(mViewPager);
        // 给TabLayout设置适配器
        mTabLayout.setTabsFromPagerAdapter(mFragmentAdapter);
    }
}
