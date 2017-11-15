package com.pillbox;

/**
 * Created by aschey on 11/4/2017.
 */

import android.media.Image;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
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
    }

    // Represents one row on the grid
    static class DailyViewRow {
        final String pillName;
        final String pillDesc;
        final double dosage;
        final Date date;
        final String displayTime;
        final Globals.Status statusName;

        DailyViewRow(String pillName, String pillDesc, double dosage, String date, Globals.Status statusName) {
            this.pillName = pillName;
            this.pillDesc = pillDesc;
            this.dosage = dosage;
            this.date = Globals.parseDate("yyyy-MM-dd HH:mm", date);
            this.displayTime = Globals.formatDate("hh:mm a", this.date);
            this.statusName = statusName;
        }
    }
}

