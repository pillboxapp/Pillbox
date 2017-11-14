package com.pillbox;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.pillbox.DailyViewContent.DailyViewRow;

import java.text.MessageFormat;
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

        // Show the current date on the screen to start
        this.setDate(Globals.getCurrentDate());

        SQLiteDatabase sqliteDB;
        try {
            // TODO: Remove the following statement when done changing the database
            this.deleteDatabase(getResources().getString(R.string.db_name));

            sqliteDB = this.openOrCreateDatabase(getResources().getString(R.string.db_name), MODE_PRIVATE, null);

            // Set the instance of sql to be used for the duration of the app's life
            PillboxDB.setDB(sqliteDB);
            PillboxDB.createTables();
            // TODO: Remove the following statement when we insert real data
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
        String pillText = item.dosage > 1 ? "pills": "pill";
        String pillTime = MessageFormat.format("Take {0} {1} at {2}", item.dosage, pillText, item.displayTime);
        this.updateDetailedText(item.pillName, item.pillDesc, pillTime);
    }

    private void updateDetailedText(String pillName, String pillDesc, String pillTime) {
        TextView description = findViewById(R.id.detailed_view_pill_description);
        TextView name = findViewById(R.id.detailed_view_pill_name);
        TextView time = findViewById(R.id.detailed_view_pill_time);

        name.setText(pillName);
        description.setText(pillDesc);
        time.setText(pillTime);
    }

    private void resetDetailedText() {
        this.updateDetailedText("", "", "");
    }

    public void goToCalendar(View view) {
        Intent myIntent = new Intent(MainActivity.this, CalendarActivity.class);
        MainActivity.this.startActivity(myIntent);
    }

    public void goToAddPill(View view) {
        Intent myIntent = new Intent(MainActivity.this, AddPillActivity.class);
        MainActivity.this.startActivity(myIntent);
    }

    public void increaseDate(View view) {
        this.changeDate(1);
    }

    public void decreaseDate(View view) {
        this.changeDate(-1);
    }

    private void changeDate(int numDays) {
        this.setDate(Globals.addDays(this.currentDate, numDays));
        DailyViewFragment dailyView = (DailyViewFragment)getSupportFragmentManager().findFragmentById(R.id.daily_view_fragment);
        dailyView.reloadData();
        this.resetDetailedText();
    }

    private void setDate(Date newDate) {
        this.currentDate = newDate;
        TextView dateText = findViewById(R.id.current_date);
        dateText.setText(Globals.formatDate("MMM dd, YYYY", this.currentDate));
    }
}
