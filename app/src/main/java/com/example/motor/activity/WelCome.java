package com.example.motor.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.motor.MyApplication;

public class WelCome extends AppCompatActivity {

	private SharedPreferences prefs1;
	private Intent intent = new Intent();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MyApplication.getInstance().addActivity(this);
		prefs1 = getSharedPreferences("isFirst", Context.MODE_PRIVATE);
		boolean isFirstIn = prefs1.getBoolean("isFirstIn", false);
		if (isFirstIn) {
			intent.setClass(getApplicationContext(), SplashActivity.class);
			startActivity(intent);
			finish();
		} else {
			intent.setClass(getApplicationContext(), WelcomeActivity.class);
			startActivity(intent);
			finish();
		}
	}
}
