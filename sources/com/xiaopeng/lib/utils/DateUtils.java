package com.xiaopeng.lib.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
/* loaded from: classes.dex */
public class DateUtils {
    private static SimpleDateFormat sDailySimpleFormat = new SimpleDateFormat("MM.dd");
    private static SimpleDateFormat sMonthlySimpleFormat = new SimpleDateFormat("yyyy.MM");
    private static SimpleDateFormat sSimpleFormat1 = new SimpleDateFormat("MM.dd");
    private static SimpleDateFormat sSimpleFormat2 = new SimpleDateFormat("MM月dd日");
    private static SimpleDateFormat sSimpleFormat3 = new SimpleDateFormat("yyyy年MM月dd日");
    private static SimpleDateFormat sSimpleFormat4 = new SimpleDateFormat("yyyy.MM.dd");
    private static SimpleDateFormat sSimpleFormat5 = new SimpleDateFormat("HH:mm:ss");
    private static SimpleDateFormat sTimeSimpleFormat = new SimpleDateFormat("HH:mm");
    private static SimpleDateFormat sBirthdaySimpleFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat sSimpleFormat6 = new SimpleDateFormat("yyMMddHHmmss");
    private static SimpleDateFormat sSimpleFormat7 = new SimpleDateFormat("yyyy/MM/dd");
    private static SimpleDateFormat sSimpleFormat8 = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
    private static SimpleDateFormat sSimpleFormat9 = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
    private static SimpleDateFormat sSimpleFormat10 = new SimpleDateFormat("yyyyMMdd_HHmmss");

    public static synchronized String formatDate1(long date) {
        String format;
        synchronized (DateUtils.class) {
            format = sSimpleFormat1.format(Long.valueOf(date));
        }
        return format;
    }

    public static synchronized String formatDate2(long date) {
        synchronized (DateUtils.class) {
            if (isToday(date)) {
                return "今天";
            }
            if (isYesterday(date)) {
                return "昨天";
            }
            return sSimpleFormat2.format(Long.valueOf(date));
        }
    }

    public static synchronized String formatDate3(long date) {
        synchronized (DateUtils.class) {
            if (isToday(date)) {
                return "今天";
            }
            if (isYesterday(date)) {
                return "昨天";
            }
            return sSimpleFormat3.format(Long.valueOf(date));
        }
    }

    public static synchronized String formatDate4(long date) {
        String format;
        synchronized (DateUtils.class) {
            format = sBirthdaySimpleFormat.format(Long.valueOf(date));
        }
        return format;
    }

    public static synchronized String formatDate5(long date) {
        String format;
        synchronized (DateUtils.class) {
            format = sSimpleFormat5.format(Long.valueOf(date));
        }
        return format;
    }

    public static synchronized String formatDate6(long date) {
        String format;
        synchronized (DateUtils.class) {
            format = sSimpleFormat6.format(Long.valueOf(date));
        }
        return format;
    }

    public static synchronized String formatDate7(long date) {
        String format;
        synchronized (DateUtils.class) {
            format = sSimpleFormat7.format(Long.valueOf(date));
        }
        return format;
    }

    public static synchronized String formatDate8(long date) {
        String format;
        synchronized (DateUtils.class) {
            format = sSimpleFormat8.format(Long.valueOf(date));
        }
        return format;
    }

    public static synchronized String formatDate9(long date) {
        String format;
        synchronized (DateUtils.class) {
            format = sSimpleFormat9.format(Long.valueOf(date));
        }
        return format;
    }

    public static synchronized String formatDate10(long date) {
        String format;
        synchronized (DateUtils.class) {
            format = sSimpleFormat10.format(Long.valueOf(date));
        }
        return format;
    }

    public static synchronized long dateToStamp(String dateStr) {
        long stamp;
        synchronized (DateUtils.class) {
            stamp = 0;
            try {
                Date date = sSimpleFormat10.parse(dateStr);
                stamp = date.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return stamp;
    }

    public static long getDayBeginTimeInMillis(long timeInMillis) {
        Calendar calendar = (Calendar) Calendar.getInstance().clone();
        calendar.setTimeInMillis(timeInMillis);
        calendar.set(11, Calendar.getInstance().getActualMinimum(11));
        calendar.set(12, Calendar.getInstance().getActualMinimum(12));
        calendar.set(13, Calendar.getInstance().getActualMinimum(13));
        calendar.set(14, Calendar.getInstance().getActualMinimum(14));
        return calendar.getTimeInMillis();
    }

    public static Calendar getStartOfDay(Calendar calendar) {
        Calendar start = (Calendar) calendar.clone();
        start.set(11, calendar.getActualMinimum(11));
        start.set(12, calendar.getActualMinimum(12));
        start.set(13, calendar.getActualMinimum(13));
        start.set(14, calendar.getActualMinimum(14));
        return start;
    }

    public static Calendar getEndOfDay(Calendar calendar) {
        Calendar end = (Calendar) calendar.clone();
        end.set(11, calendar.getActualMaximum(11));
        end.set(12, calendar.getActualMaximum(12));
        end.set(13, calendar.getActualMaximum(13));
        end.set(14, calendar.getActualMaximum(14));
        return end;
    }

    public static synchronized String getDailyFormatString(long calendar) {
        String format;
        synchronized (DateUtils.class) {
            format = sDailySimpleFormat.format(Long.valueOf(calendar));
        }
        return format;
    }

    public static synchronized String getDailyFormatString(Calendar calendar) {
        String format;
        synchronized (DateUtils.class) {
            format = sDailySimpleFormat.format(Long.valueOf(calendar.getTimeInMillis()));
        }
        return format;
    }

    public static synchronized String getBirthdayFormatString(long calendar) {
        String format;
        synchronized (DateUtils.class) {
            format = sBirthdaySimpleFormat.format(Long.valueOf(calendar));
        }
        return format;
    }

    public static synchronized Date getBirthdayParseString(String calendar) {
        Date parse;
        synchronized (DateUtils.class) {
            try {
                parse = sBirthdaySimpleFormat.parse(calendar);
            } catch (ParseException e) {
                e.printStackTrace();
                return new Date();
            }
        }
        return parse;
    }

    public static boolean isToday(Calendar calendar) {
        return calendar.get(1) == Calendar.getInstance().get(1) && calendar.get(6) == Calendar.getInstance().get(6);
    }

    public static boolean isToday(long timeInMillis) {
        Calendar calendar = (Calendar) Calendar.getInstance().clone();
        calendar.setTimeInMillis(timeInMillis);
        return calendar.get(1) == Calendar.getInstance().get(1) && calendar.get(6) == Calendar.getInstance().get(6);
    }

    public static boolean isYesterday(long timeInMillis) {
        Calendar calendar = (Calendar) Calendar.getInstance().clone();
        calendar.setTimeInMillis(timeInMillis);
        return calendar.get(1) == Calendar.getInstance().get(1) && calendar.get(6) == Calendar.getInstance().get(6) - 1;
    }

    public static Calendar getMondayOfWeek(Calendar calendar) {
        Calendar monday = (Calendar) calendar.clone();
        int day_of_week = monday.get(7) - 1;
        if (day_of_week == 0) {
            day_of_week = 7;
        }
        monday.add(5, (-day_of_week) + 1);
        monday.set(11, monday.getActualMinimum(11));
        monday.set(12, monday.getActualMinimum(12));
        monday.set(13, monday.getActualMinimum(13));
        monday.set(14, monday.getActualMinimum(14));
        return monday;
    }

    public static Calendar getSundayOfWeek(Calendar calendar) {
        Calendar sunday = (Calendar) calendar.clone();
        int day_of_week = sunday.get(7) - 1;
        if (day_of_week == 0) {
            day_of_week = 7;
        }
        sunday.add(5, (-day_of_week) + 7);
        sunday.set(11, sunday.getActualMaximum(11));
        sunday.set(12, sunday.getActualMaximum(12));
        sunday.set(13, sunday.getActualMaximum(13));
        sunday.set(14, sunday.getActualMaximum(14));
        return sunday;
    }

    public static synchronized String getWeeklyFormatString(Calendar calendar) {
        String str;
        synchronized (DateUtils.class) {
            Calendar monday = getMondayOfWeek(calendar);
            Calendar sunday = getSundayOfWeek(calendar);
            str = sDailySimpleFormat.format(Long.valueOf(monday.getTimeInMillis())) + " - " + sDailySimpleFormat.format(Long.valueOf(sunday.getTimeInMillis()));
        }
        return str;
    }

    public static boolean isThisWeek(Calendar calendar) {
        return calendar.get(3) == Calendar.getInstance().get(3) && calendar.get(1) == Calendar.getInstance().get(1);
    }

    public static Calendar getStartOfMonth(Calendar calendar) {
        Calendar begin = (Calendar) calendar.clone();
        begin.set(5, begin.getActualMinimum(5));
        begin.set(11, begin.getActualMinimum(11));
        begin.set(12, begin.getActualMinimum(12));
        begin.set(13, begin.getActualMinimum(13));
        begin.set(14, begin.getActualMinimum(14));
        return begin;
    }

    public static Calendar getEndOfMonth(Calendar calendar) {
        Calendar end = (Calendar) calendar.clone();
        end.set(5, end.getActualMaximum(5));
        end.set(11, end.getActualMaximum(11));
        end.set(12, end.getActualMaximum(12));
        end.set(13, end.getActualMaximum(13));
        end.set(14, end.getActualMaximum(14));
        return end;
    }

    public static synchronized String getMonthlyFormatString(Calendar calendar) {
        String format;
        synchronized (DateUtils.class) {
            format = sMonthlySimpleFormat.format(Long.valueOf(calendar.getTimeInMillis()));
        }
        return format;
    }

    public static boolean isThisMonth(Calendar calendar) {
        return calendar.get(2) == Calendar.getInstance().get(2) && calendar.get(1) == Calendar.getInstance().get(1);
    }

    public static long getAccuracyToHour(long collectTime) {
        Calendar calendar = (Calendar) Calendar.getInstance().clone();
        calendar.setTimeInMillis(collectTime);
        calendar.set(14, calendar.getActualMinimum(14));
        calendar.set(13, calendar.getActualMinimum(13));
        calendar.set(12, calendar.getActualMinimum(12));
        return calendar.getTimeInMillis();
    }

    public static long getAccuracyToDay(long collectTime) {
        Calendar calendar = (Calendar) Calendar.getInstance().clone();
        calendar.setTimeInMillis(collectTime);
        calendar.set(14, calendar.getActualMinimum(14));
        calendar.set(13, calendar.getActualMinimum(13));
        calendar.set(12, calendar.getActualMinimum(12));
        calendar.set(11, calendar.getActualMinimum(11));
        return calendar.getTimeInMillis();
    }

    public static synchronized String getTimeFormatString(long time) {
        String format;
        synchronized (DateUtils.class) {
            format = sTimeSimpleFormat.format(Long.valueOf(time));
        }
        return format;
    }

    public static long getSomeMinuteAgo(Calendar calendar, int minute) {
        Calendar end = (Calendar) calendar.clone();
        end.add(12, minute * (-1));
        return end.getTime().getTime();
    }

    public static Date getSomeMinuteAfter(Calendar calendar, int minute) {
        Calendar end = (Calendar) calendar.clone();
        end.add(12, minute);
        return end.getTime();
    }

    public static List<Date> getBeforeAndAfterHalfHour(Date date) {
        List<Date> dateList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(12, -30);
        Date startDate = calendar.getTime();
        calendar.add(12, 60);
        Date endDate = calendar.getTime();
        dateList.add(startDate);
        dateList.add(endDate);
        return dateList;
    }

    public static int dateInterval(long date1, long date2) {
        long date12;
        long date22;
        if (date2 <= date1) {
            date12 = date1;
            date22 = date2;
        } else {
            long date23 = date2 + date1;
            date12 = date23 - date1;
            date22 = date23 - date12;
        }
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(date12);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(date22);
        int y1 = calendar1.get(1);
        int y2 = calendar2.get(1);
        int d1 = calendar1.get(6);
        int d2 = calendar2.get(6);
        if (y1 - y2 > 0) {
            int day = numerical(0, d1, d2, y1, y2, calendar2);
            return day;
        }
        int day2 = d1 - d2;
        return day2;
    }

    public static int numerical(int maxDays, int d1, int d2, int y1, int y2, Calendar calendar) {
        int day = d1 - d2;
        int betweenYears = y1 - y2;
        List<Integer> d366 = new ArrayList<>();
        if (calendar.getActualMaximum(6) == 366) {
            day++;
        }
        for (int i = 0; i < betweenYears; i++) {
            calendar.set(1, calendar.get(1) + 1);
            int maxDays2 = calendar.getActualMaximum(6);
            if (maxDays2 != 366) {
                day += maxDays2;
            } else {
                d366.add(Integer.valueOf(maxDays2));
            }
            if (i == betweenYears - 1 && betweenYears > 1 && maxDays2 == 366) {
                day--;
            }
        }
        for (int i2 = 0; i2 < d366.size(); i2++) {
            if (d366.size() >= 1) {
                day += d366.get(i2).intValue();
            }
        }
        return day;
    }
}
