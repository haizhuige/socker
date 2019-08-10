package com.liuhu.socket.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类
 * <p>
 * Created by zhaoliping on 2016/6/24.
 */
public class DateUtils  {
    private static final Logger logger = LogManager.getLogger(DateUtils.class);

    public enum DateFormat {
        YYYYMMDD("yyyyMMdd"),
        YYYY_MM_DD("yyyy-MM-dd"),
        Y_M_D("yyyy年MM月dd日"),
        HHMMSS("HHmmss"),
        HH_MM_SS("HH:mm:ss"),
        YYYY_MM_DD_HHMMSS("yyyy-MM-ddHH:mm:ss"),
        YYYY_MM_DD_HH_MM_SS("yyyy-MM-dd HH:mm:ss"),
        YYYY_MM_DD_HH_MM("yyyy-MM-dd HH:mm"),
        MM_DD_POINT("MM.dd"),
        YYYY_MM("yyyy-MM"),
        Y_MM("yyyy年M月"),
        MM_DD_HH_MM("MM-dd HH:mm"),
        YYYY_MM_DD_POINT("yyyy.MM.dd");
        private String format;

        DateFormat(String format) {
            this.format = format;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }
    }

    /**
     * 获取指定时间的星期几 周日为0
     */
    public static int getWeekDay(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        return w;
    }

    /**
     * 获取当日凌晨
     * @param date
     * @return
     */
    public static Date getBeginOfDate(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                0, 0, 0);
        Date beginOfDate = calendar.getTime();
        return beginOfDate;
    }

    /**
     * 获取当日23:59:59
     * @param date
     * @return
     */
    public static Date getEndOfDate(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                23, 59, 59);
        Date endOfDate = calendar.getTime();
        return endOfDate;
    }

    /**
     * 指定当前月份的某一天
     *
     * @param date
     * @param day
     * @return
     */
    public static Date getDateOfCurrcentMonth(Date date, int day) {
        if (date == null || day < 1 || day > 31) {
            throw new IllegalArgumentException("日期不能为空，天数有效范围1至31");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        if (day > 28) {
            // 判断这个月有没有28天
            int maxDay = calendar.getMaximum(Calendar.DATE);
            if (day > maxDay) {
                throw new IllegalArgumentException("当前月份，没有指定的日期");
            }
        }
        calendar.set(Calendar.DATE, day);
        return calendar.getTime();
    }

    /**
     * 获取日期的年份
     *
     * @param date 日期
     * @return 年份
     */
    public static int getYear(Date date) {
        return getInteger(date, Calendar.YEAR);
    }

    /**
     * 获取日期的月份
     *
     * @param date 日期
     * @return 月份
     */
    public static int getMonth(Date date) {
        return getInteger(date, Calendar.MONTH) + 1;
    }

    /**
     * 获取日期的天数。失败返回0。
     *
     * @param date 日期
     * @return 天
     */
    public static int getDay(Date date) {
        return getInteger(date, Calendar.DATE);
    }

    /**
     * 获取日期中的某数值。如获取月份
     *
     * @param date     日期
     * @param dateType 日期格式
     * @return 数值
     */
    private static int getInteger(Date date, int dateType) {
        if (date == null) {
            throw new IllegalArgumentException("日期不能为空!");
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(dateType);
    }

    /**
     * @param date      日期
     * @param otherDate 另一个日期
     * @return 相差天数(date与otherDate的相对值)
     */
    public static int getIntervalDays(Date date, Date otherDate) {
        return Math.abs(getIntervalDaysActual(date, otherDate));
    }

    /**
     * @param date      日期
     * @param otherDate 另一个日期
     * @return 相差天数(date与otherDate的天数)
     */
    public static int getIntervalDaysActual(Date date, Date otherDate) {
        if (date == null || otherDate == null) {
            throw new IllegalArgumentException("日期不能为空");
        }
        long day = 24L * 3600 * 1000;
        int d1 = (int) (date.getTime() / day);
        int d2 = (int) (otherDate.getTime() / day);
        return d1 - d2;
    }

    /**
     * @param date      日期
     * @param otherDate 另一个日期
     * @return 相差天数(date与otherDate的天数)
     */
    public static int getIntervalDaysForDay(Date date, Date otherDate) {
        if (date == null || otherDate == null) {
            throw new IllegalArgumentException("日期不能为空");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(otherDate);
        int day1 = cal1.get(Calendar.DAY_OF_YEAR);
        int day2 = cal2.get(Calendar.DAY_OF_YEAR);
        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);
        if (year1 != year2) //同一年
        {
            int timeDistance = 0;
            for (int i = year1; i < year2; i++) {
                if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0) //闰年 
                {
                    timeDistance += 366;
                } else //不是闰年
                {
                    timeDistance += 365;
                }
            }

            return timeDistance + (day2 - day1);
        } else //不同年
        {
            return day2 - day1;
        }
    }

    /**
     * @param date      日期1
     * @param otherDate 另一个日期2
     * @return date是否在otherDate之后
     */
    public static Boolean isAfter(Date date, Date otherDate) {
        return getIntervalMills(date, otherDate) > 0L;
    }

    /**
     * @param date      日期1
     * @param otherDate 另一个日期2
     * @return date是否在otherDate之前
     */
    public static Boolean isBefore(Date date, Date otherDate) {
        return getIntervalMills(date, otherDate) < 0L;
    }

    /**
     * 格式化日期
     *
     * @param date   日期
     * @param format 格式化类型
     * @return 格式化之后的字符串
     */
    public static String format(Date date, DateFormat format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format.getFormat());
        return sdf.format(date);
    }

    /**
     * 格式化日期
     *
     * @param date   日期
     * @param format 格式化类型
     * @return 格式化之后的字符串
     */
    public static Date parse(String date, DateFormat format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format.getFormat());
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException("日期转换异常", e);
        }
    }

    /**
     * @param date      日期1
     * @param otherDate 另一个日期2
     * @return date与otherDate相差的毫秒数
     */
    public static long getIntervalMills(Date date, Date otherDate) {
        if (date == null || otherDate == null) {
            throw new IllegalArgumentException("日期不能为空");
        }
        return date.getTime() - otherDate.getTime();
    }

    /**
     * @param date      日期1
     * @param otherDate 另一个日期2
     * @return date与otherDate相差的小时数
     */
    public static long getIntervalHours(Date date, Date otherDate) {
        if (date == null || otherDate == null) {
            throw new IllegalArgumentException("日期不能为空");
        }
        long times = date.getTime() - otherDate.getTime();
        return times/1000/3600;
    }

    public static boolean equalsDate(Date date1, Date date2) {
        if (null == date1 && null == date2) {
            return true;
        } else if (null != date1 && null != date2) {
            int date1Year = getYear(date1);
            int date1Month = getMonth(date1);
            int date1Day = getDay(date1);

            int date2Year = getYear(date2);
            int date2Month = getMonth(date2);
            int date2Day = getDay(date2);
            if (date1Year == date2Year && date1Month == date2Month && date1Day == date2Day) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取 若干小时后 的时间
     * @param date
     * @param hour
     * @return
     */
    public static Date afterDate(Date date,int hour) {
        Date afterDate = new Date(date .getTime() + 3600000*hour);
        return afterDate;
    }

    /**
     * 两个时间相差距离多少天多少小时多少分多少秒
     * @return String 返回值为：xx小时xx分xx秒
     */
    public static String getDistanceTime(Date date, Date otherDate) {
        long diff = getIntervalMills(date,otherDate);
        long hour = (diff / (60 * 60 * 1000));
        long min = ((diff / (60 * 1000)) - hour * 60);
        long sec = (diff / 1000 - hour*60*60 - min*60);
        return hour + ":" + min + ":" + sec;
    }
    /**
     * 字符串日期几天前或者几天后的日期

     */
    public static String operateDateStr(String dateStr,int day,String pattern){
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Date date = null;
        try {
            date = format.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date afterDate = new Date(date .getTime() + 24*3600000*day);
        return format.format(afterDate);
    }
    /**
     * 字符串日期几天前或者几天后的日期

     */
    public static String operateDate(Date date,int day,String pattern){
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, day);
        Date d = c.getTime();
        return format.format(d);
    }
}
