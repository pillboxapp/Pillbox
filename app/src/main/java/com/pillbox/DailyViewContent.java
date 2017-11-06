package com.pillbox;

/**
 * Created by aschey on 11/4/2017.
 */

import android.media.Image;

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
    static List<DailyViewRow> Items = new ArrayList<>();

    static void loadItems(Date currentDate) {
        List<DailyViewRow> returnedItems = PillboxDB.getHeadersForDay(currentDate);
        Items.addAll(returnedItems);
    }

    // Represents one row on the grid
    static class DailyViewRow {
        final String pillName;
        final String date;
        final Globals.Status statusName;

        DailyViewRow(String pillName, String date, Globals.Status statusName) {
            this.pillName = pillName;
            this.date = date;
            this.statusName = statusName;
        }
    }
}

