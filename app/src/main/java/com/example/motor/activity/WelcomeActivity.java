package com.example.motor.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.motor.MyApplication;
import com.example.motor.R;
import com.example.motor.adapter.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.jpush.android.api.JPushInterface;

public class WelcomeActivity extends AppCompatActivity implements OnClickListener,
        ViewPager.OnPageChangeListener {

	private SharedPreferences prefs1;
	private Intent intent = new Intent();
	private ViewPager vp;
	private ViewPagerAdapter vpAdapter;
	private List<View> views;
	// 引导图片资源
	private static final int[] pics = { R.drawable.lead1, R.drawable.lead2,
			R.drawable.lead3 };
	// 底部圆点图片
	//private ImageView[] dots;
	// 记录当前选中的位置
	private int currentIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*this.requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置Activity标题不显示*/
		if(Build.VERSION.SDK_INT > 18){
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		setContentView(R.layout.welcome);
		MyApplication.getInstance().addActivity(this);
		prefs1 = getSharedPreferences("isFirst", Context.MODE_PRIVATE);
		boolean isFirstIn = prefs1.getBoolean("isFirstIn", false);
		if (isFirstIn) {
			intent.setClass(this, SplashActivity.class);
			startActivity(intent);
			finish();
		}
		views = new ArrayList<View>();
		LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		// 初始化引导图片列表 
		for (int i = 0; i < pics.length; i++) {
			ImageView iv = new ImageView(this);
			iv.setLayoutParams(mParams);
			iv.setImageResource(pics[i]);
			views.add(iv);
	     	vp = (ViewPager) findViewById(R.id.viewpager);
			// 初始化Adapter
			vpAdapter = new ViewPagerAdapter(views);
			vp.setAdapter(vpAdapter);
			
			// 绑定回调
			vp.setOnPageChangeListener(this);

			// 初始化底部小点
			//initDots();
		}
	}

	/*private void initDots() {
		LinearLayout ll = (LinearLayout) findViewById(R.id.ll);

		dots = new ImageView[pics.length];

		// 循环取得小点图片
		for (int i = 0; i < pics.length; i++) {
			dots[i] = (ImageView) ll.getChildAt(i);
			dots[i].setEnabled(true);// 都设为灰色
			dots[i].setOnClickListener(this);
			dots[i].setTag(i);// 设置位置tag，方便取出与当前位置对应
		}

		currentIndex = 0;
		dots[currentIndex].setEnabled(false);// 设置为白色，即选中状态
	}*/

	/**
	 * 设置当前的引导页
	 */
	private void setCurView(int position) {
		if (position < 0 || position >= pics.length) {
			return;
		}

		vp.setCurrentItem(position);
	}

	/**
	 * 设置当前引导小点的选中
	 */
	/*private void setCurDot(int positon) {
		if (positon < 0 || positon > pics.length - 1 || currentIndex == positon) {
			return;
		}

		dots[positon].setEnabled(false);//设置为白色，即选中状态
		dots[currentIndex].setEnabled(true);//设置为灰色

		currentIndex = positon;
	}*/

	// 当滑动状态改变时调用
	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
	}

	// 当当前页面被滑动时调用
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
	}

	// 当新的页面被选中时调用
	@Override
	public void onPageSelected(int position) {
		// 设置底部小点选中状态
		//setCurDot(position);
		if (position == views.size() - 1) {
			Editor editor = prefs1.edit();
			editor.putBoolean("isFirstIn", true);
			editor.apply();
			intent.setClass(getApplicationContext(), LoginActivity.class);
			Timer timer = new Timer();
			class MyTask extends TimerTask {
				@Override
				public void run() {
					// System.out.println("dddd");
					startActivity(intent);
					WelcomeActivity.this.finish();
				}
			}
			timer.schedule(new MyTask(), 1000);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		JPushInterface.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		JPushInterface.onPause(this);
	}

	@Override
	public void onClick(View v) {
		int position = (Integer) v.getTag();
		setCurView(position);
		//setCurDot(position);
	}
}
