package com.pillbox;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.pillbox.DailyViewContent.DailyViewRow;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements DetailedViewFragment.OnFragmentInteractionListener,
        DailyViewFragment.OnListFragmentInteractionListener {
    private Date currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolBar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolBar);

        TextView dateText = findViewById(R.id.current_date);
        this.currentDate = Calendar.getInstance().getTime();
        dateText.setText(new SimpleDateFormat("MMM dd, YYYY", Locale.US).format(this.currentDate));

        SQLiteDatabase sqliteDB;
        try {
            // TODO: Get rid of the following statement when done changing the database
            this.deleteDatabase(getResources().getString(R.string.db_name));

            sqliteDB = this.openOrCreateDatabase(getResources().getString(R.string.db_name), MODE_PRIVATE, null);

            PillboxDB.setDB(sqliteDB);
            PillboxDB.createTables();
            PillboxDB.insertDummyData();
        }
        catch (SQLiteException ex) {
            Log.e(getClass().getSimpleName(), "Could not create or open the database");
        }
    }

    public Date getCurrentDate() {
        return this.currentDate;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onFragmentInteraction(Uri uri) {

    }

    public void onListFragmentInteraction(DailyViewRow item) {

    }

    public void goToCalendar(View view) {
        Intent myIntent = new Intent(MainActivity.this, CalendarActivity.class);
        MainActivity.this.startActivity(myIntent);

    }
}
