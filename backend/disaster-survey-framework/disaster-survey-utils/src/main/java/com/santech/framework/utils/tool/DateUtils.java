package com.santech.framework.utils.tool;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 日期工具类
 */
@Slf4j
public class DateUtils {


    /**
     * 判断date早于now返回true，否则返回false
     * @param date
     * @return
     */
    public static boolean isBeforeNow(Date date){
        boolean b = false;
        if(ObjectUtil.isEmpty(date)){
            b = true;
            return b;
        }
        Date now = new Date();
        LocalDateTime dateL = dateToLocalDateTime(date);
        LocalDateTime nowL = dateToLocalDateTime(now);
        if(dateL.isBefore(nowL)){
            b = true;
        }
        return b;
    }

    /**
     * 判断date晚于now返回true，否则返回false
     * @param date
     * @return
     */
    public static boolean isAfterNow(Date date){
        boolean b = false;
        if(ObjectUtil.isEmpty(date)){
            b = true;
            return b;
        }
        Date now = new Date();
        LocalDateTime dateL = dateToLocalDateTime(date);
        LocalDateTime nowL = dateToLocalDateTime(now);
        if(dateL.isAfter(nowL)){
            b = true;
        }
        return b;
    }

    /**
      * @Description: 早于开始时间，返回true
      * @param startDateStr
      * @param startDateFormatter
      * @param dateStr
      * @param dateFormatter
      * @return: boolean
      **/
    public static boolean isAfterStartDate(String startDateStr, String startDateFormatter, String dateStr, String dateFormatter) {
        boolean b = false;
        DateTimeFormatter startDateDF = DateTimeFormatter.ofPattern(startDateFormatter);
        LocalDateTime startDateDT = LocalDateTime.parse(startDateStr , startDateDF);
        DateTimeFormatter dateDF = DateTimeFormatter.ofPattern(dateFormatter);
        LocalDateTime dateDT = LocalDateTime.parse(dateStr , dateDF);
        if(startDateDT.isBefore(dateDT)||startDateDT.isEqual(dateDT)){
            b = true;
        }
        return b;
    }

    /**
      * @Description: 早于结束时间，返回true
      * @param endDateStr
      * @param endDateFormatter
      * @param dateStr
      * @param dateFormatter
      * @return: boolean
      **/
    public static boolean isBeforeEndDate(String endDateStr, String endDateFormatter, String dateStr, String dateFormatter) {
        boolean b = false;
        DateTimeFormatter endDateDF = DateTimeFormatter.ofPattern(endDateFormatter);
        LocalDateTime endDateDT = LocalDateTime.parse(endDateStr , endDateDF);
        DateTimeFormatter dateDF = DateTimeFormatter.ofPattern(dateFormatter);
        LocalDateTime dateDT = LocalDateTime.parse(dateStr , dateDF);
        if(endDateDT.isAfter(dateDT)||endDateDT.isEqual(dateDT)){
            b = true;
        }
        return b;
    }

    /**
     * Date 转 LocalDateTime
     * @param date
     * @return
     */
    public static  LocalDateTime dateToLocalDateTime(Date date){
        LocalDateTime ldt = date.toInstant()
                .atZone( ZoneId.systemDefault() )
                .toLocalDateTime();
        return ldt;
    }

    /**
     * 获取当前时间字符串
     * @return
     */
    public static String getCurrentDateStr(){
        LocalDateTime nowDateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTimeFormatter.format(nowDateTime);

    }

    /**
     * 获取当前年份字符串
     * @return
     */
    public static String getCurrentYearStr(){
        LocalDateTime nowDateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy");
        return dateTimeFormatter.format(nowDateTime);
    }

    /**
     * 获取当前时间字符串
     * @return
     */
    public static String getCurrentDateSampleStr(){
        LocalDateTime nowDateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return dateTimeFormatter.format(nowDateTime);

    }

    /**
     * 获取时间字符串("yyyy-MM-dd")
     * @return
     */
    public static String getDateSampleStr(Date date){
        if(date == null){
            return "";
        }
        LocalDateTime dateTime = dateToLocalDateTime(date);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return dateTimeFormatter.format(dateTime);
    }

    /**
     * 获取时间字符串("yyyy年MM月dd日")
     * @return
     */
    public static String getDateSampleStrCn(Date date){
        if(date == null){
            return "";
        }
        LocalDateTime dateTime = dateToLocalDateTime(date);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        return dateTimeFormatter.format(dateTime);
    }

    /**
     * 获取时间字符串("yyyy-MM")
     * @return
     */
    public static String getDateMonthStr(Date date){
        if(date == null){
            return "";
        }
        LocalDateTime dateTime = dateToLocalDateTime(date);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        return dateTimeFormatter.format(dateTime);
    }

    /**
     * 获取时间字符串("yyyy年MM月")
     * @return
     */
    public static String getDateMonthStrCn(Date date){
        if(date == null){
            return "";
        }
        LocalDateTime dateTime = dateToLocalDateTime(date);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月");
        return dateTimeFormatter.format(dateTime);
    }

    /**
     * 获取时间字符串("yyyy")
     * @return
     */
    public static String getDateYearStr(Date date){
        if(date == null){
            return "";
        }
        LocalDateTime dateTime = dateToLocalDateTime(date);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy");
        return dateTimeFormatter.format(dateTime);
    }

    /**
     * 指定日期加上天数后的日期
     * @param num 为增加的天数，如果为负即为减去的天数
     * @param date 创建时间
     * @return
     */
    public static Date plusDay(Date date, int num) {
        long day = num;
        // 得到指定日期的毫秒数
        long time = date.getTime();
        // 要加上的天数转换成毫秒数
        day = day*24*60*60*1000;
        // 相加得到新的毫秒数
        time+=day;
        // 将毫秒数转换成日期
        return new Date(time);
    }

    /**
     * 本季度开始时间
     * @return
     */
    public static String currentDateStartSeasonStr(){
        String dateStr = getCurrentDateSampleStr();
        String monthStr = dateStr.substring(5,7);
        String startMonthStr = "";
        if("01".equals(monthStr) || "02".equals(monthStr) || "03".equals(monthStr)){
            startMonthStr = "01";
        }else if("04".equals(monthStr) || "05".equals(monthStr) || "06".equals(monthStr)){
            startMonthStr = "04";
        }else if("07".equals(monthStr) || "08".equals(monthStr) || "09".equals(monthStr)){
            startMonthStr = "07";
        }else if("10".equals(monthStr) || "11".equals(monthStr) || "12".equals(monthStr)){
            startMonthStr = "10";
        }
        return dateStr.substring(0,5) + startMonthStr + "-01" + " 00:00:00";
    }

    /**
     * 获取n天字符串，为正即后，为负即前
     * @param num
     * @return
     */
    public static String getCurrentDateNdayStr(int num){
        return getDateSampleStr(plusDay(new Date(), num)) + " 00:00:00";
    }

    /**
      * @Description: 前端传日期时，传过来的null,得重新赋为 ""
      * @param o
      * @return: java.lang.String
      **/
    public static Object checkDateNull(Object o){
        if("null".equals(o)){
            o = "";
        }
        return o;
    }

    /***
      * @Description: 判断传入日期与当前日期是否在n天内
      * @param dateStr
      * @param formatter 日期格式
      * @param numDay n天内
      * @return: boolean
      **/
    public static boolean durationNumday(String dateStr, String formatter, int numDay) {
        boolean b = false;
        DateTimeFormatter df = DateTimeFormatter.ofPattern(formatter);
        LocalDateTime ldt = LocalDateTime.parse(dateStr , df);
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(ldt, now);
        //相差的天数
        long days = duration.toDays();
        if (days < numDay) {
            b = true;
        }
        return b;
    }

    public static long timeStamp = getNowTime();
    public static int reqCount = 0;
    /**
     * 计数器，适用于简单场景
     * 注意：有一个十分致命的问题，那就是临界问题
     * @param limit 时间窗口内最大请求数
     * @param interval 时间窗口ms
     */
    public static boolean counter(int limit,long interval) {
        long now = getNowTime();
        if (now < timeStamp + interval) {
            // 在时间窗口内
            reqCount++;
            // 判断当前时间窗口内是否超过最大请求控制数
            return reqCount <= limit;
        } else {
            timeStamp = now;
            // 超时后重置
            reqCount = 1;
            return true;
        }
    }


    public static long getNowTime() {
        return System.currentTimeMillis();
    }

    /**
      * @Description: 获取当前年前一年字符串
      * @param
      * @return: java.lang.String
      **/
    public static String getPreYearStr(){
        String year = getDateYearStr(new Date());
        year = Integer.parseInt(year) - 1 + "";
        return year;
    }



}
