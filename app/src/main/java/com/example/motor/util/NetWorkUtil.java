package com.example.motor.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 网络 工具类
 * @author WRJ
 *
 */
public class NetWorkUtil {

	private static final int NETTYPE_NO = 0;
	private static final int NETTYPE_WIFI = 1;
	private static final int NETTYPE_CMWAP = 2;
	private static final int NETTYPE_CMNET = 3;

	/**
	 * 检测网络是否可用
	 * 
	 * @return
	 */
	public static boolean isNetworkConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}
}
