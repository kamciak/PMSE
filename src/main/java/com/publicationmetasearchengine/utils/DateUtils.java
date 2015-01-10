package com.publicationmetasearchengine.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat sdfDateOnly = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat sdfYearOnly = new SimpleDateFormat("yyyy");

    public static Date parseDate(String dateString) throws ParseException {
        if (dateString == null) return null;
        return sdf.parse(dateString);
    }

    public static Date parseDateOnly(String dateString) throws ParseException {
        if (dateString == null) return null;
        return sdfDateOnly.parse(dateString);
    }

    public static String formatDate(Date date) {
        if (date == null) return null;
        return sdf.format(date);
    }

    public static String formatDateOnly(Date date) {
        if (date == null) return null;
        return sdfDateOnly.format(date);
    }

    public static String formatYearOnly(Date date) {
        if (date == null) return null;
        return sdfYearOnly.format(date);
    }
}
