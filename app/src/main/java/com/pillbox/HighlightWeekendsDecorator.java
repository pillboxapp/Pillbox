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
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Calendar;

/**
 * Highlight Saturdays and Sundays with a background
 */
public class HighlightWeekendsDecorator implements DayViewDecorator {

    private final Calendar calendar = Calendar.getInstance();
    private final Drawable highlightDrawable;
    private final ShapeDrawable circleDrawable;
    private static final int color = Color.parseColor("#ff3333");

    public HighlightWeekendsDecorator() {
        highlightDrawable = new ColorDrawable(color);
        circleDrawable = new ShapeDrawable (new OvalShape ());
        circleDrawable.setIntrinsicHeight(5);
        circleDrawable.setIntrinsicWidth (5);
        circleDrawable.setAlpha(50);
        circleDrawable.getPaint ().setColor (color);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        day.copyTo(calendar);
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        return weekDay == Calendar.SATURDAY || weekDay == Calendar.SUNDAY;
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(circleDrawable);

    }
}