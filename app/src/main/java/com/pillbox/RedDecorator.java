package com.pillbox;

/**
 * Created by zach on 11/6/17.
 */

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;

/**
 * Highlight Saturdays and Sundays with a background
 */
public class RedDecorator implements DayViewDecorator {

    private final Calendar calendar = Calendar.getInstance();
    private final Drawable highlightDrawable;
    private final ShapeDrawable circleDrawable;
    private static final int color = Color.parseColor("#ff3333");
    private final HashSet<CalendarDay> dates;

    public RedDecorator() throws ParseException {
        this.dates = new HashSet<CalendarDay>(PillboxDB.getRedDates());
        highlightDrawable = new ColorDrawable(color);
        circleDrawable = new ShapeDrawable (new OvalShape ());
        circleDrawable.setIntrinsicHeight(5);
        circleDrawable.setIntrinsicWidth (5);
        circleDrawable.setAlpha(50);
        circleDrawable.getPaint ().setColor (color);

    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(circleDrawable);

    }
}