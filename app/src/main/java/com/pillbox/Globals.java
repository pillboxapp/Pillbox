package com.pillbox;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    static String formatDate(String pattern, Date date) {
        return new SimpleDateFormat(pattern, Locale.US).format(date);
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
}
