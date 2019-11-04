package com.example.motor.util;

/**
 * Created by admin on 2017/4/19.
 */

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DateUtils {

    public final static String FORMAT_YYYY_MM_DD = "yyyy-MM-dd";
    public final static String FORMAT_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd hh:mm:ss";
    public final static String FORMAT_YYYYMMDD = "yyyyMMdd";
    public final static String FORMAT_YYYYMM = "yyyyMM";
    public final static String FORMAT_YYYY = "yyyy";
    public final static String FORMAT_YYYYMMDDHHMISS = "yyyyMMdd HH:mm:ss";
    public final static String FORMAT_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    public final static String FORMAT_YYYY_MM_DD_HH_MI_SS = "yyyy-MM-dd HH:mm:ss";
    public final static String FORMAT_YYYY_MM_DD_HH_MI_SS_SSS = "yyyy-MM-dd HH:mm:ss:SSS";
    public final static String FORMAT_YYYY_MM = "yyyy-MM";
    public final static String FORMAT_DD_HH_MM_SS = "dd HH:mm:ss";
    public final static String FORMAT_HH_MM_SS = "HH:mm:ss";
    public static final int USE_YEAR = 1;
    public static final int USE_MONTH = 2;
    public static final int USE_WEEK = 7;
    public static final int USE_DAY = 3;

    public static String getCurrentTime() {
        return getCurrentTime("yyyy-MM-dd  HH:mm:ss");
    }

    public static String getCurrentTime(String format) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        String currentTime = sdf.format(date);
        return currentTime;
    }


    /**
     * 获取指定日期前N月或者后N月的相同日期字符串
     *
     * @param date
     * @param tag
     * @return
     * @throws Exception
     */
    public static String getPreviousOrNextDateOfTheDay(String date, int tag) throws Exception {
        Calendar calendar = new GregorianCalendar();
        DateFormat dateFormat = getDateFormat(FORMAT_YYYY_MM_DD_HH_MI_SS);
        calendar.setTime(dateFormat.parse(date));
        if (tag == USE_WEEK) {
            calendar.add(Calendar.DAY_OF_MONTH, -7);
        } else if (tag == USE_MONTH) {
            calendar.add(Calendar.MONTH, -1);
        } else if (tag == USE_YEAR) {
            calendar.add(Calendar.YEAR, -1);
        }
        return dateFormat.format(calendar.getTime());
    }

    /**
     * 所有时间按当前2014-12-02计算
     *
     * @author alone
     */

    private String ymdhms = "yyyy-MM-dd HH:mm:ss";
    private String ymd = "yyyy-MM-dd";
    public SimpleDateFormat ymdSDF = new SimpleDateFormat(ymd);
    private String year = "yyyy";
    private String month = "MM";
    private String day = "dd";
    public SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat(ymdhms);
    public SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
    public SimpleDateFormat yyyyMMddHH_NOT_ = new SimpleDateFormat("yyyyMMdd");
    public long DATEMM = 86400L;//当前日期毫秒值

    /**
     * 根据相应的格式初始化日期格式对象
     *
     * @param pattern
     * @return
     */
    public static DateFormat getDateFormat(final String pattern) {
        return new SimpleDateFormat(pattern);
    }

    /**
     * 获取当月的第一天
     *
     * @return
     */
    public static Date getFirstDayOfThisMonth() {
        Calendar nowday = Calendar.getInstance();
        nowday.set(Calendar.DAY_OF_MONTH, 1);
        nowday.set(Calendar.HOUR_OF_DAY, 0);
        nowday.set(Calendar.MINUTE, 0);
        nowday.set(Calendar.SECOND, 0);
        nowday.set(Calendar.MILLISECOND, 0);
        return nowday.getTime();
    }

    /**
     * 获取指定日期之前或者之后的日期
     *
     * @param days Day
     * @return
     */
    public static Date getPreviousOrNextDaysOfDate(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
    }

    /**
     * 获取与指定日期相差月份数的相同日期
     *
     * @param date
     * @param month
     * @return
     */
    public static Date getPreviousOrNextMonthsOfDate(Date date, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, month);
        return calendar.getTime();
    }

    /**
     * 求某一个时间向前多少秒的时间(currentTimeToBefer)---OK
     *
     * @param givedTime        给定的时间
     * @param interval         间隔时间的毫秒数；计算方式 ：n(天)*24(小时)*60(分钟)*60(秒)(类型)
     * @param format_Date_Sign 输出日期的格式；如yyyy-MM-dd、yyyyMMdd等；
     */
    public String givedTimeToBefer(String givedTime, long interval,
                                   String format_Date_Sign) {
        String tomorrow = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format_Date_Sign);
            Date gDate = sdf.parse(givedTime);
            long current = gDate.getTime(); // 将Calendar表示的时间转换成毫秒
            long beforeOrAfter = current - interval * 1000L; // 将Calendar表示的时间转换成毫秒
            Date date = new Date(beforeOrAfter); // 用timeTwo作参数构造date2
            tomorrow = new SimpleDateFormat(format_Date_Sign).format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return tomorrow;
    }

    /**
     * 根据结束时间以及间隔差值，求符合要求的日期集合；
     *
     * @param endTime
     * @param interval
     * @param isEndTime
     * @return
     */
    public Map<String, String> getDate(String endTime, Integer interval,
                                       boolean isEndTime) {
        Map<String, String> result = new HashMap<String, String>();
        if (interval == 0 || isEndTime) {
            if (isEndTime)
                result.put(endTime, endTime);
        }
        if (interval > 0) {
            int begin = 0;
            for (int i = begin; i < interval; i++) {
                endTime = givedTimeToBefer(endTime, DATEMM, ymd);
                result.put(endTime, endTime);
            }
        }
        return result;
    }
}

