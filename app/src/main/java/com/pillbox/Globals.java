package com.pillbox;

import android.content.Context;

/**
 * Created by aschey on 11/5/2017.
 */

public class Globals {
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
}
