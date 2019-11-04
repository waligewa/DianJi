package com.example.motor;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.Vibrator;

import com.example.motor.db.InspectionOffineItem;
import com.example.motor.db.InspectionOffineStateItem;
import com.example.motor.service.LocationServer;
import com.example.motor.service.LongRunningService;

import org.litepal.LitePalApplication;
import org.xutils.x;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;

public class MyApplication extends Application {

    public static String SDPATH;
    public Vibrator mVibrator;
    public static Typeface typeFace;
    private static Context context;
    // 这两个集合用于巡检列表无网状态数据的存储
    public static List<InspectionOffineItem> ioi = new ArrayList<>();
    public static List<InspectionOffineStateItem> iosi = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        LitePalApplication.initialize(context);
        x.Ext.init(this);
        x.Ext.setDebug(true);
        instance = this;
        //  FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/texi.ttf");
        //  FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/texi.ttf");
        //  FontsOverride.setDefaultFont(this, "SERIF", "fonts/texi.ttf");
        //  FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/texi.ttf");
        //  setTypeface();
        //百度地图初始化
        mVibrator = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        //    1.使用Environment.getExternalStorageDirectory可以得到系统的sdcard路径，不过这个一般在各个手机上都是一样的。
        //    2.使用context.getExternalFilesDir可以得到系统为程序在sdcard上分配的存储路径，据说放在这里卸载程序时目录也会被删除;
        //    3.使用context.getFileDir可以获得程序的data目录的files子目录，如果有小文件，sdcard又不存在时可以选择放这里。
        //        SDPATH = getBaseContext().getExternalFilesDir().toString() + File.separator;
        //        SDPATH = getBaseContext().getFilesDir().toString() + File.separator;
        SDPATH = getDiskCacheDir(getBaseContext()) + File.separator;
        //初始化sdk
        JPushInterface.setDebugMode(true);  //  正式版的时候设置false，关闭调试
        JPushInterface.init(this);
        // 设置保留最近通知条数
        JPushInterface.setLatestNotificationNumber(context, 30);
    }

    public static Context getContext() {
        return context;
    }

    public String getDiskCacheDir(Context context) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();//mounted 安装好的
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

    public void setTypeface() {
        //华文彩云，加载外部字体assets/front/huawen_caiyun.ttf
        typeFace = Typeface.createFromAsset(getAssets(), "fonts/xingkai.ttf");
        try {
            //  与values/styles.xml中的<item name="android:typeface">sans</item>对应
            //  Field field = Typeface.class.getDeclaredField("SERIF");
            //  field.setAccessible(true);
            //  field.set(null, typeFace);
            //  Field field_1 = Typeface.class.getDeclaredField("DEFAULT");
            //  field_1.setAccessible(true);
            //  field_1.set(null, typeFace);
            //  与monospace对应
            //  Field field_2 = Typeface.class.getDeclaredField("MONOSPACE");
            //  field_2.setAccessible(true);
            //  field_2.set(null, typeFace);

            //与values/styles.xml中的<item name="android:typeface">sans</item>对应
            Field field_3 = Typeface.class.getDeclaredField("SANS_SERIF");//  declared宣布
            field_3.setAccessible(true);  //accessible 访问
            field_3.set(null, typeFace);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开的activity
     **/
    private List<Activity> activities = new ArrayList<Activity>();
    /**
     * 应用实例
     **/
    private static MyApplication instance;

    /**
     * 获得实例
     *
     * @return
     */
    public static MyApplication getInstance() {
        return instance;
    }

    /**
     * 新建了一个activity
     *
     * @param activity
     */
    public void addActivity(Activity activity) {
        activities.add(activity);
    }

    /**
     * 结束指定的Activity
     *
     * @param activity
     */
    public void finishActivity(Activity activity) {
        if (activity != null) {
            this.activities.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    /**
     * 应用退出，结束所有的activity
     */
    public void exit() {

        for (Activity activity : activities) {
            if (activity != null) {
                activity.finish();
            }
        }
        // 18-09-28因为推送过来点击之后总是会登录过期（当然原因是guid的问题），
        // 所以只要进入登录界面就将这个服务给关闭
        stopService(new Intent(this, LongRunningService.class));
        // 停止百度鹰眼的服务（里面还有给王珂上传坐标的一个网络请求方法）
        stopService(new Intent(this, LocationServer.class));
        System.exit(0);
    }
}
