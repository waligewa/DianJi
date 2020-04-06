package com.example.motor.adapter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.View;

import java.util.List;

/**
 * ViewPager是用来展示一组数据的，所以肯定需要Adapter来绑定数据和view。先写一个Adapter：
 * @author wrj
 */
public class ViewPagerAdapter extends PagerAdapter {

	private SharedPreferences sharedPreferences;
	private Intent intent = new Intent();
	// 界面列表
	private List<View> views;

	public ViewPagerAdapter(List<View> views) {
		this.views = views;
	}

	// 销毁position位置的界面
	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView(views.get(position));
	}

	// 获得当前界面数
	@Override
	public int getCount() {
		if (views != null) {
			return views.size();
		}
		return 0;
	}

	// 初始化position位置的界面
	@Override
	public Object instantiateItem(View container, int position) {
		((ViewPager) container).addView(views.get(position), 0);

		return views.get(position);
	}

	// 判断是否由对象生成界面
	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return (arg0 == arg1);
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public Parcelable saveState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void startUpdate(View arg0) {
		// TODO Auto-generated method stub
	}
}
