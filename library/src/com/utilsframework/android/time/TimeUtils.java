package com.utilsframework.android.time;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * User: Tikhonenko.S
 * Date: 03.03.14
 * Time: 16:25
 */
public class TimeUtils {
    public static final int MILLISECONDS_IN_DAY = 24 * 3600 * 1000;

    private static long startTimeInMilliseconds = System.currentTimeMillis();
    private static long startNanoSeconds = System.nanoTime();

    public static final String[] SHORT_MONTHS = new String[] { "Jan", "Feb", "Mar",
            "Apr", "May", "Jun", "Jul", "Aug", "Sep",
            "Oct", "Nov", "Dec" };

    public static long getTimeInMicroSeconds(){
        long milliSeconds = System.currentTimeMillis() - startTimeInMilliseconds;
        long nanoSeconds = System.nanoTime() - startNanoSeconds;

        Log.i("yoyoyoy", "nano = " + nanoSeconds);
        Log.i("yoyoyoy", "mili = " + milliSeconds);

        return nanoSeconds / 1000 + milliSeconds * 1000;
    }

    public static String getDisplayDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static String getFormatDayOrTime(int dayOrTime) {
        if(dayOrTime < 0){
            throw new IllegalArgumentException("dayOrTime < 0");
        }

        String result = String.valueOf(dayOrTime);
        if(result.length() < 2){
            result = '0' + result;
        }

        return result;
    }

    public static String getShortMonthName(int id) {
        return SHORT_MONTHS[id];
    }

    public static String getAlternativeDisplayDate(long time) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(time);
        return getAlternativeDisplayDate(calendar);
    }

    public static String getAlternativeDisplayDate(GregorianCalendar calendar) {
        String month = TimeUtils.getShortMonthName(calendar.get(Calendar.MONTH));
        String day = TimeUtils.getFormatDayOrTime(calendar.get(Calendar.DAY_OF_MONTH));
        int year = calendar.get(Calendar.YEAR);
        return day + " " + month + " " + year;
    }

    //01 Jan 2015 at 20:23
    public static String getAlternativeDisplayDateTime(long milliseconds) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(milliseconds);

        String hours = TimeUtils.getFormatDayOrTime(calendar.get(Calendar.HOUR_OF_DAY));
        String minutes = TimeUtils.getFormatDayOrTime(calendar.get(Calendar.MINUTE));

        return getAlternativeDisplayDate(calendar) + " at " + hours + ":" + minutes;
    }

    public static long getStartOfCurrentDay() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
