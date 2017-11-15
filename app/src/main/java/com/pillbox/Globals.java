package com.pillbox;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by aschey on 11/5/2017.
 */

class Globals {
    private Globals() { }

    // Medication taken status
    enum Status {
        TAKEN("TAKEN"),
        SKIPPED("SKIPPED"),
        TIME_TO_TAKE("TIME_TO_TAKE"),
        UPCOMING("UPCOMING");

        private final String text;

        Status(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return this.text;
        }
    }

    enum DayOfWeek {
        SUNDAY("SUNDAY", 1),
        MONDAY("MONDAY", 2),
        TUESDAY("TUESDAY", 3),
        WEDNESDAY("WEDNESDAY", 4),
        THURSDAY("THURSDAY", 5),
        FRIDAY("FRIDAY", 6),
        SATURDAY("SATURDAY", 7);

        private final String text;
        private final int value;

        DayOfWeek(final String text, int value) {
            this.text = text;
            this.value = value;
        }

        int getValue() {
            return this.value;
        }

        @Override
        public String toString() {
            return this.text;
        }
    }

    static String formatDate(String pattern, Date date) {
        return new SimpleDateFormat(pattern, Locale.US).format(date);
    }

    static String formatDate(String pattern, Calendar calendar) {
        return new SimpleDateFormat(pattern, Locale.US).format(calendar.getTime());
    }

    static Date parseDate(String pattern, String date) {
        try {
            return new SimpleDateFormat(pattern, Locale.US).parse(date);
        }
        catch (ParseException ex) {
            return null;
        }
    }

    static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }

    static Date getCurrentDate() {
        return Calendar.getInstance().getTime();
    }

    static String nextDateTime(int weekOffset, DayOfWeek dow, String time) {
        final int DAYS_IN_WEEK = 7;
        Calendar date = Calendar.getInstance();
        // Choose the new starting week
        date.add(Calendar.WEEK_OF_MONTH, weekOffset);

        String[] splitTime = time.split(":");
        int hours = Integer.parseInt(splitTime[0]);
        int minutes = Integer.parseInt(splitTime[1]);

        date.set(Calendar.HOUR_OF_DAY, hours);
        date.set(Calendar.MINUTE, minutes);

        int diff = dow.getValue() - date.get(Calendar.DAY_OF_WEEK);

        // Don't add an entry for the current day if the time has already passed
        if (diff == 0 && Calendar.getInstance().compareTo(date) > 0) {
            return null;
        }
        // If the day of the week has already passed, move to the next week
        if (diff < 0) {
            diff += DAYS_IN_WEEK;
        }
        date.add(Calendar.DAY_OF_MONTH, diff);

        return formatDate("yyyy-MM-dd HH:mm", date);
    }
}
