package com.example.motor.util;

import java.text.SimpleDateFormat;

public class TimeUtils {
    public TimeUtils() { }

    public static String long2String(long time) {
        int sec = (int) time / 1000;
        int min = sec / 60;
        sec %= 60;
        return min < 10?(sec < 10?"0" + min + ":0" + sec:"0" + min + ":" + sec):(sec < 10?min + ":0" + sec:min + ":" + sec);
    }

    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(Long.valueOf(System.currentTimeMillis()));
    }
}
