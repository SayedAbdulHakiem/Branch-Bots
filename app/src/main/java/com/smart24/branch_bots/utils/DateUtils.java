package com.smart24.branch_bots.utils;


import android.annotation.SuppressLint;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    private static final String DATE_ONLY_FORMAT = "yyyy-MM-dd";
    private static final String DATE_AND_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static DateUtils instance;

    public DateUtils() {

    }

    public static long getCurrentTimeStamp() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        return date.getTime();
    }

    public String getDateFromDayNumber(int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, day);
        Date date = calendar.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_ONLY_FORMAT);
        String dateString = formatter.format(date);

        return dateString;
    }


    public String getDateAndTimeLineSeparated(long timestamp) {
        String date;
        Timestamp ts = new Timestamp(timestamp);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat(DATE_AND_TIME_FORMAT);
        date = convertToEnglishNumbers(formatter.format(ts));
        String[] stringArr = date.split(" ");
        date = stringArr[0] + "\n" + stringArr[1];


        return date;
    }

    public static String getDateAndTime(long timestamp) {
        String date;
        Timestamp ts = new Timestamp(timestamp);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat(DATE_AND_TIME_FORMAT);
        date = convertToEnglishNumbers(formatter.format(ts));

        return date;
    }

    public static String getDateAndTime() {
        Date date = new Date();
        String dateString;
        Timestamp ts = new Timestamp(date.getTime());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat(DATE_AND_TIME_FORMAT);
        dateString = convertToEnglishNumbers(formatter.format(ts));

        return dateString;
    }


    public int getDayNumberOfMonth() {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public String getCurrentDayName() {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day_of_week = calendar.get(Calendar.DAY_OF_WEEK);

        if (day_of_week == 1)
            return "الاحد";
        else if (day_of_week == 2)
            return "الاثنين";
        else if (day_of_week == 3)
            return "الثلاثاء";
        else if (day_of_week == 4)
            return "الاربعاء";
        else if (day_of_week == 5)
            return "الخميس";
        else if (day_of_week == 6)
            return "الجمعة";
        else if (day_of_week == 7)
            return "السبت";
        else
            return "";


    }

    public String getMonthName() {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month_num = calendar.get(Calendar.MONTH) + 1;

        String month = "";
        if (month_num == 1)
            month = "يناير";
        else if (month_num == 2)
            month = "فبراير";
        else if (month_num == 3)
            month = "مارس";
        else if (month_num == 4)
            month = "ابريل";
        else if (month_num == 5)
            month = "مايو";
        else if (month_num == 6)
            month = "يونيه";
        else if (month_num == 7)
            month = "يوليو";
        else if (month_num == 8)
            month = "اغسطس";
        else if (month_num == 9)
            month = "سبتمبر";
        else if (month_num == 10)
            month = "اكتوبر";
        else if (month_num == 11)
            month = "نوفمبر";
        else if (month_num == 12)
            month = "ديسمبر";
        else
            month = "null";

        return month;
    }

    public static String getDateAndTimeString(Long timeStamp) {
        String dateString = "";
        Date date;
        if (timeStamp != null) {
            date = new Date(timeStamp);
        } else {
            date = new Date();
        }
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_ONLY_FORMAT);
        dateString = formatter.format(date);

        dateString = convertToEnglishNumbers(dateString);

        return dateString;
    }

    public String getDayAndMonth(long timestamp) {
        return getDateString(timestamp).substring(5);
    }

    public static String getDateString(long timeStamp) {

        String dateAndTimeString = getDateAndTimeString(timeStamp);

        return dateAndTimeString.split(" ")[0];
    }

    public static String convertToEnglishNumbers(String ar) {
        String en = "";
        //{'٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩'};
        char[] english_chars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        char[] arabic_chars = ar.toCharArray();
        char temp;

        for (int i = 0; i < ar.length(); ++i) {
            temp = arabic_chars[i];
            if (temp == '٠') {
                en += english_chars[0];
            } else if (temp == '١') {
                en += english_chars[1];
            } else if (temp == '٢') {
                en += english_chars[2];
            } else if (temp == '٣') {
                en += english_chars[3];
            } else if (temp == '٤') {
                en += english_chars[4];
            } else if (temp == '٥') {
                en += english_chars[5];
            } else if (temp == '٦') {
                en += english_chars[6];
            } else if (temp == '٧') {
                en += english_chars[7];
            } else if (temp == '٨') {
                en += english_chars[8];
            } else if (temp == '٩') {
                en += english_chars[9];
            } else {
                en += temp;
            }
        }
        return en;
    }


}
