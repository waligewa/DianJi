package com.example.motor.util;

import android.app.Activity;
import android.app.ProgressDialog;

import com.example.motor.R;

public class Loadding {

	// 声明进度条对话框
	ProgressDialog progressDialog = null;
	Activity activity;

	public Loadding(Activity activity) {
		this.activity = activity;
		progressDialog = new ProgressDialog(this.activity);
	}

	/***
	 * 加载动画
	 * 
	 * @param text
	 * 提示内容
	 */
	public void show(String text) { // 创建ProgressDialog对象
		// 创建ProgressDialog对象

		// 设置进度条风格，风格为圆形，旋转的
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		// 设置ProgressDialog 标题
		// progressDialog.setTitle("提示");
		// 设置ProgressDialog 提示信息
		progressDialog.setMessage(text);
		// 设置ProgressDialog 标题图标
		progressDialog.setIcon(R.mipmap.ic_launcher);
		// 设置ProgressDialog 的进度条是否不明确
		progressDialog.setIndeterminate(false);
		// 设置ProgressDialog 是否可以按退回按键取消
		progressDialog.setCancelable(true);
		// 设置ProgressDialog 的一个Button
		// progressDialog.setButton("确定", new SureButtonListener());
		// 让ProgressDialog显示
		progressDialog.show();
	}

	/***
	 * 加载动画
	 *
	 * @param text
	 *            提示内容
	 */
	public void showNoCancelable(String text) { // 创建ProgressDialog对象
		// 创建ProgressDialog对象

		// 设置进度条风格，风格为圆形，旋转的
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		// 设置ProgressDialog 标题
		// progressDialog.setTitle("提示");
		// 设置ProgressDialog 提示信息
		progressDialog.setMessage(text);
		// 设置ProgressDialog 标题图标
		progressDialog.setIcon(R.mipmap.ic_launcher);
		// 设置ProgressDialog 的进度条是否不明确
		progressDialog.setIndeterminate(false);
		// 设置ProgressDialog 是否可以按退回按键取消
		progressDialog.setCancelable(false);
		// 设置ProgressDialog 的一个Button
		// progressDialog.setButton("确定", new SureButtonListener());
		// 让ProgressDialog显示
		progressDialog.show();
	}

	public void close() {
		if (progressDialog.isShowing()) {
			progressDialog.cancel();
		}
	}

	public boolean isShow() {
		return progressDialog.isShowing();
	}
}
