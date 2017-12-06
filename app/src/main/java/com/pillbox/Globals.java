package com.pillbox;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;

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

    static int userID;
    static NotificationManager notificationManager;
    static AlarmManager alarmManager;

    static String formatDate(String pattern, Date date) {
        return new SimpleDateFormat(pattern, Locale.US).format(date);
    }

    static String formatDate(String pattern, Calendar calendar) {
        return new SimpleDateFormat(pattern, Locale.US).format(calendar.getTime());
    }

    static String reformatDate(String oldPattern, String newPattern, String date) {
        return formatDate(newPattern, parseDate(oldPattern, date));
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

    static Calendar nextDateTime(int weekOffset, DayOfWeek dow, String time) {
        final int DAYS_IN_WEEK = 7;
        Calendar date = Calendar.getInstance();
        // Choose the new starting week
        date.add(Calendar.WEEK_OF_MONTH, weekOffset);

        String[] splitTime = time.split(":");
        int hours = Integer.parseInt(splitTime[0]);
        int minutes = Integer.parseInt(splitTime[1]);

        date.set(Calendar.HOUR_OF_DAY, hours);
        date.set(Calendar.MINUTE, minutes);
        date.set(Calendar.SECOND, 0);

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

        return date;
    }

    private static int getStatusImageResource(Globals.Status status) {
        switch (status) {
            case SKIPPED: return R.drawable.red_circle;
            case TAKEN: return R.drawable.green_circle;
            case UPCOMING: return R.drawable.grey_circle;
            case TIME_TO_TAKE: return R.drawable.yellow_circle;
        }
        return R.drawable.grey_circle;
    }

    static void updateStatusImage(ImageView icon, Globals.Status status) {
        icon.setImageResource(getStatusImageResource(status));
    }

    static long getTimestamp() {
        return System.currentTimeMillis();
    }

    static void createAlarm(MainActivity context, long triggerTime, String pillName, String pillTime) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("PillName", pillName);
        intent.putExtra("PillTime", pillTime);
        int id = (int)getTimestamp();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }
}
