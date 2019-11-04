package com.example.motor.util;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 赵彬彬 on 2017-10-07.
 * 这个是防止500毫秒内连续点击使用的一个工具类
 */

public class DoubleClickUtils {

    private static long lastClickTime;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < 500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    /**
     * 提取字符串中包含的日期格式
     *
     * @param title 传入的字符串
     * @return List<String> 返回日期的List
     */
    public static List<String> getDatime(String title) {
        List<String> dateStrList = new ArrayList<>();
        String dateStr = "";
        if (!TextUtils.isEmpty(title)) {
            Pattern p = Pattern.compile("(\\d{4})/(\\d{1,2})/(\\d{1,2})");
            Matcher m = p.matcher(title);
            while (m.find()) {
                dateStr += m.group() + "到";
            }
        }
        if (!TextUtils.isEmpty(dateStr)) {
            String str[] = dateStr.split("到");
            if (!TextUtils.isEmpty(str[0])) {
                dateStrList.add(str[0]);
            }
            /*if (!TextUtils.isEmpty(str[1])) {
                dateStrList.add(str[1]);
            }*/
        }
        return dateStrList;
    }
}
