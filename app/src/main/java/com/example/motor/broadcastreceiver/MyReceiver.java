package com.example.motor.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.motor.activity.ChronicleActivity3;
import com.example.motor.activity.MainActivity;
import com.example.motor.util.ExampleUtil;
import com.example.motor.workorderlist.inspection.InspectionWorkOrderActivity;
import com.example.motor.workorderlist.repair.RepairWorkOrderActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import cn.jpush.android.api.JPushInterface;

/**
 * 自定义接收器
 * 
 * 如果不定义这个 Receiver，则：
 * 1) 默认用户会打开主界面
 * 2) 接收不到自定义消息
 *
 */
public class MyReceiver extends BroadcastReceiver {

	private static final String TAG = "zbb_MyReciver";
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Bundle bundle = intent.getExtras();
			Log.e(TAG, "[MyReceiver] onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));
			if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
				String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
				Log.e(TAG, "[MyReceiver] 接收Registration Id : " + regId);
				//send the Registration Id to your server...
			} else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
				Log.e(TAG, "[MyReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
				processCustomMessage(context, bundle);
			} else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
				Log.e(TAG, "[MyReceiver] 接收到推送下来的通知");
				int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
				Log.e(TAG, "[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);
			} else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
				Log.e(TAG, "[MyReceiver] 用户点击打开了通知");
                String type = bundle.getString(JPushInterface.EXTRA_EXTRA);
				String content = bundle.getString(JPushInterface.EXTRA_ALERT);
                Log.e(TAG, "[MyReceiver]" + type);
                Intent i = new Intent();
                if(type.contains("维修")){
                    i.setClass(context, RepairWorkOrderActivity.class);
                } else if(type.contains("巡检")){
                    i.setClass(context, InspectionWorkOrderActivity.class);
                } else if(type.contains("报警")){
                    i.setClass(context, ChronicleActivity3.class);
                }
                //i.putExtras(bundle);
                i.putExtra("workorder_number", content);  // 内容指的那一堆报警内容
				i.putExtra("workorder_type", type);  // type指的是那个json字符串
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(i);
				// 打开自定义的Activity
			} else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
				Log.e(TAG, "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
				// 在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..
			} else if(JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
				boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
				Log.e(TAG,"[MyReceiver]" + intent.getAction() + " connected state change to " + connected);
			} else {
				Log.e(TAG,"[MyReceiver] Unhandled intent - " + intent.getAction());
			}
		} catch (Exception e){}
	}

	// 打印所有的intent extra数据   就用了一次，我感觉可有可无，对我来说
	private static String printBundle(Bundle bundle) {
		StringBuilder stringBuilder = new StringBuilder();
		for (String key : bundle.keySet()) {
			if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
				stringBuilder.append("\nkey:" + key + ", value:" + bundle.getInt(key));
			}else if(key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)){
				stringBuilder.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
			} else if (key.equals(JPushInterface.EXTRA_EXTRA)) {
				if (TextUtils.isEmpty(bundle.getString(JPushInterface.EXTRA_EXTRA))) {
					Log.e(TAG, "This message has no Extra data");
					continue;
				}
				try {
					JSONObject json = new JSONObject(bundle.getString(JPushInterface.EXTRA_EXTRA));
					Iterator<String> it =  json.keys();
					while (it.hasNext()) {
						String myKey = it.next();
						stringBuilder.append("\nkey:" + key + ", value: [" +
								myKey + " - " +json.optString(myKey) + "]");
					}
				} catch (JSONException e) {
					Log.e(TAG, "Get message extra JSON error!");
				}
			} else {
				stringBuilder.append("\nkey:" + key + ", value:" + bundle.getString(key));
			}
		}
		return stringBuilder.toString();
	}
	
	//  send msg to MainActivity
	private void processCustomMessage(Context context, Bundle bundle) {
		if (MainActivity.isForeground) {
			String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
			String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
			Intent intent = new Intent(MainActivity.MESSAGE_RECEIVED_ACTION);
			intent.putExtra(MainActivity.KEY_MESSAGE, message);
			if (!ExampleUtil.isEmpty(extras)) {
				try {
					JSONObject jsonObject = new JSONObject(extras);
					if (jsonObject.length() > 0) {
						intent.putExtra(MainActivity.KEY_EXTRAS, extras);
					}
				} catch (JSONException e) {}
			}
			LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		}
	}
}
