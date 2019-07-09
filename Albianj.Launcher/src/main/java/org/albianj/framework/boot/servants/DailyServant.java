package org.albianj.framework.boot.servants;


import org.albianj.framework.boot.tags.BundleSharingTag;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

@BundleSharingTag
public class DailyServant {
    /**
     * 一天时间的时间戳，单位MS
     */
    public final static long DailyTimestampMS = 86400000;

    public static DailyServant Instance = null;

    static {
        if(null == Instance) {
            Instance = new DailyServant();
        }
    }

    protected DailyServant() {

    }

    /**
     * 当前日期零点的时间戳
     *
     * @return
     */
    public long todayTimestampOfZero() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 当前日期的字符串，日期"-"分隔符，时间":"分隔
     */
    public String datetimeLongString() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(calendar.getTime());
    }

    /**
     * 当前日期的字符串，然后"-"分隔符
     */
    public String dateString() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(calendar.getTime());
    }

    /**
     * 当前时间的字符串，然后":"分隔符
     */
    public String timeString() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        return df.format(calendar.getTime());
    }

    /**
     * 当前日期时间的字符串，无":"分隔符
     */
    public String datetimeLongStringWithoutSep() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        return df.format(calendar.getTime());
    }

    /**
     * 当前日期的字符串，无":"分隔符
     */
    public String dateStringWithoutSep() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        return df.format(calendar.getTime());
    }

    /**
     * 当前时间的字符串，无":"分隔符
     */
    public String timeStringWithoutSep() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        DateFormat df = new SimpleDateFormat("HHmmss");
        return df.format(calendar.getTime());
    }

    /**
     * 获取完全格式的的日期格式,包括毫秒
     * @return 格式如 2015-10-31 10:33:25:012
     */

    public String datetimeLongStringWithMillis(long timeMS){
        StringBuffer sb = new StringBuffer(30);
        Calendar nowtime = Calendar.getInstance(TimeZone.getDefault());
        nowtime.setTimeInMillis(timeMS);
        int _year = nowtime.get(Calendar.YEAR); //获取年数
        int _month = nowtime.get(Calendar.MONTH) + 1; //获取月数（Java中默认为0-11）
        int _day = nowtime.get(Calendar.DAY_OF_MONTH); //获取天数
        int _hour = nowtime.get(Calendar.HOUR_OF_DAY); //获取小时
        int _minute = nowtime.get(Calendar.MINUTE); //获取分钟
        int _second = nowtime.get(Calendar.SECOND); //获取秒数
        int _millisecond = nowtime.get(Calendar.MILLISECOND); //获取毫秒数

        sb.append(_year);
        sb.append("-");
        if(_month <10){
            sb.append("0");
        }
        sb.append(_month);
        sb.append("-");
        if(_day <10){
            sb.append("0");
        }
        sb.append(_day);
        sb.append(" ");
        if(_hour <10){
            sb.append("0");
        }
        sb.append(_hour);
        sb.append(":");
        if(_minute <10){
            sb.append("0");
        }
        sb.append(_minute);
        sb.append(":");
        if(_second <10){
            sb.append("0");
        }
        sb.append(_second);
        sb.append(":");
        if(_millisecond <10){
            sb.append("00");
        }else if(_millisecond <100){
            sb.append("0");
        }
        sb.append(_millisecond);

        return sb.toString();
    }

    public String datetimeLongStringWithMillis(){
        return datetimeLongStringWithMillis(System.currentTimeMillis());
    }

}
