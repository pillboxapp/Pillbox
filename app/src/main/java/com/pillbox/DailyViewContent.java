package com.pillbox;

/**
 * Created by aschey on 11/4/2017.
 */

import android.media.Image;
import android.support.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
class DailyViewContent {

    // List of items to be displayed on the grid
    static ArrayList<DailyViewRow> Items = new ArrayList<>();

    static void loadItems(Date currentDate) {
        List<DailyViewRow> returnedItems = PillboxDB.getHeadersForDay(currentDate);

        // Reload data
        Items.clear();
        Items.addAll(returnedItems);

        // Sort items from earliest to latest
        Collections.sort(Items);
    }

    // Represents one row on the grid
    static class DailyViewRow implements Comparable<DailyViewRow> {
        final int rowID;
        final String pillName;
        final String pillDesc;
        final double dosage;
        final byte[] pillPic;
        final int alarmCode;

        private Date date;
        private String displayTime;
        private Globals.Status statusName;
        private long alarmTime;

        DailyViewRow(int rowID, String pillName, String pillDesc, double dosage, String date, Globals.Status statusName, byte[] pillPic,
                     int alarmCode) {
            this.rowID = rowID;
            this.pillName = pillName;
            this.pillDesc = pillDesc;
            this.dosage = dosage;
            this.statusName = statusName;
            this.pillPic = pillPic;
            this.alarmCode = alarmCode;

            this.updateDate(date);
        }

        Date getDate() {
            return this.date;
        }

        void updateDate(Date newDate) {
            this.date = newDate;
            this.displayTime = Globals.formatDate("hh:mm a", this.date);
            this.alarmTime = this.date.getTime();
        }

        void updateDate(String newDate) {
            this.updateDate(Globals.parseDate("yyyy-MM-dd HH:mm", newDate));
        }

        String getDisplayTime() {
            return this.displayTime;
        }

        void updateStatus(Globals.Status newStatus) {
            this.statusName = newStatus;
        }

        Globals.Status getStatus() {
            return this.statusName;
        }

        long getAlarmTime() {
            return this.alarmTime;
        }

        @Override
        public int compareTo(@NonNull DailyViewRow other) {
            return this.date.compareTo(other.date);
        }
    }
}

