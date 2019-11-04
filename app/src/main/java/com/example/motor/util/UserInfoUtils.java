package com.example.motor.util;

import android.content.Context;

import com.example.motor.constant.ConstantsField;
import com.example.motor.db.User;
import com.google.gson.Gson;

/**
 * Author : yanftch
 * Date   : 2018/3/10
 * Time   : 20:33
 * Desc   : 登录信息保存工具类
 */


public class UserInfoUtils {
    private volatile static UserInfoUtils instance;  //  volatile  不稳定的
    private User user;

    public static UserInfoUtils getInstance() {
        if (null == instance) {
            synchronized (UserInfoUtils.class) {
                if (null == instance) {
                    instance = new UserInfoUtils();
                }
            }
        }
        return instance;
    }

    public void saveUserInfoString(Context context, String string) {
        SPUtils.saveStringData(context, ConstantsField.USER, string);
    }

    public User getUserInfo(Context context) {
        // 在SplashActivity里面可以得到key为user的value的值，如果点击登录之后，这个value就为data的值，
        // 不为空字符串，然后通过Gson解析为User对象，并返回一个User对象。
        String s = SPUtils.getStringData(context, ConstantsField.USER);
        Gson gson = new Gson();
        User user = gson.fromJson(s, User.class);
        return user;
    }
    // 承接上面，在点击登录界面之后，因为getUserInfro方法返回的是一个非null的User对象，所以这个的
    // 返回值为true，所以在SplashActivity里面的判断前后都为true，走第一个if
    public boolean isLogin(Context context) {
        if ((getUserInfo(context) != null)) {
            return true;
        }
        return false;
    }

    public String getUserName(Context context) {
        return null == getUserInfo(context) ? "" : getUserInfo(context).getUserName();
    }

    public int getUserId(Context context) {
        return null == getUserInfo(context) ? -1 : getUserInfo(context).getUserID();
    }

    public int getUserRole(Context context) {
        return null == getUserInfo(context) ? -1 : getUserInfo(context).getRole();
    }

    public void removeUserCache(Context context) {
        SPUtils.removeData(context, ConstantsField.USER);
        instance.user = null;
    }
//
//    /**
//     * 缓存userinfo下次免登录
//     *
//     * @param context
//     * @param user
//     */
//    public void saveUserInfo(Context context, User user) {
//        SPUtils.saveObject(context, ConstantsField.USER, user);
//        instance = getInstance();
//        setUser(user);
//    }
//
//    /**
//     * 只在当前状态下缓存，如果推出则清空，下次继续登录
//     *
//     * @param user
//     */
//    public void setUser(User user) {
//        this.user = user;
//    }
//
//    public User getUserInfo(Context context) {
//        getInstance();
//        if (user == null) {
//            User user = SPUtils.getObject(context, ConstantsField.USER, User.class);
//            this.user = user;
//        }
//        return instance.user;
//    }
//
//    public boolean isSavaInfo(Context context) {
//        User user = SPUtils.getObject(context, ConstantsField.USER, User.class);
//        return user != null;
//    }
//
//    public boolean getUserState(Context context) {
//        return SPUtils.getBooleanData(context, ConstantsField.ISSAVA);
//    }
//
//    public boolean isLogin(Context context) {
//        return getUserInfo(context) != null;
//    }
//
//
//    public void clearCache(Context context) {
//        SPUtils.removeData(context, ConstantsField.USER);
//        instance.user = null;
//    }
}
