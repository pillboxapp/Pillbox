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
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pillbox.DailyViewContent.DailyViewRow;
import com.pillbox.DailyViewRowRecyclerViewAdapter.ViewHolder;

import java.text.MessageFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements DailyViewFragment.OnListFragmentInteractionListener {
    private Date currentDate;
    private ViewHolder selectedRow;

    private static boolean initialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolBar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolBar);

        // Show the current date on the screen to start
        this.setDate(Globals.getCurrentDate());

        if (!initialized) {
            Globals.userID = 1;
            SQLiteDatabase sqliteDB;
            try {
                // TODO: Remove the following statement when done changing the database
                //this.deleteDatabase(getResources().getString(R.string.db_name));

                sqliteDB = this.openOrCreateDatabase(getResources().getString(R.string.db_name), MODE_PRIVATE, null);

                // Set the instance of sql to be used for the duration of the app's life
                PillboxDB.setDB(sqliteDB);
                PillboxDB.createTables();
                // TODO: Remove the following statement when we insert real data
                //PillboxDB.insertDummyData();

                PillboxDB.addMissingHeaders();

                initialized = true;
            } catch (SQLiteException ex) {
                Log.e(getClass().getSimpleName(), "Could not create or open the database");
            }
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

    public void onListFragmentInteraction(ViewHolder holder) {
        this.selectedRow = holder;
        DailyViewRow item = holder.mItem;
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

    private void resetDetailedView() {
        this.updateDetailedText("", "", "");
    }

    public void goToCalendar(View view) {
        Intent myIntent = new Intent(MainActivity.this, CalendarActivity.class);
        MainActivity.this.startActivityForResult(myIntent, 1);
    }

    public void goToAddPill(View view) {
        Intent myIntent = new Intent(MainActivity.this, AddPillActivity.class);
        MainActivity.this.startActivity(myIntent);
    }

    public void goToSettings(MenuItem item) {
        Intent myIntent = new Intent(MainActivity.this, Settings.class);
        MainActivity.this.startActivity(myIntent);
    }

    public void goToAddAccount(MenuItem item) {
        Intent myIntent = new Intent(MainActivity.this, AddAccount.class);
        MainActivity.this.startActivity(myIntent);
    }

    public void goToChangeAccount(MenuItem item) {
        Intent myIntent = new Intent(MainActivity.this, ChangeAccount.class);
        MainActivity.this.startActivity(myIntent);
    }

    public void increaseDate(View view) {
        this.changeDate(1);
    }

    public void decreaseDate(View view) {
        this.changeDate(-1);
    }

    private void changeDate(int numDays) {
        this.changeDate(Globals.addDays(this.currentDate, numDays));
    }

    private void changeDate(Date date) {
        this.setDate(date);
        DailyViewFragment dailyView = (DailyViewFragment)getSupportFragmentManager().findFragmentById(R.id.daily_view_fragment);
        dailyView.reloadData();
        this.resetDetailedView();
    }

    private void updatePillStatus(Globals.Status newStatus) {
        // Don't do anything if no pill is selected
        if (selectedRow == null) {
            return;
        }

        PillboxDB.updateStatus(this.selectedRow.mItem.rowID, newStatus);
        this.selectedRow.mItem.updateStatus(newStatus);

        Globals.updateStatusImage(this.selectedRow.mStatusView, newStatus);
    }

    public void skipPill(View view) {
        this.updatePillStatus(Globals.Status.SKIPPED);
    }

    public void takePill(View view) {
        this.updatePillStatus(Globals.Status.TAKEN);
    }

    private void setDate(Date newDate) {
        this.currentDate = newDate;
        TextView dateText = findViewById(R.id.current_date);
        dateText.setText(Globals.formatDate("MMM dd, YYYY", this.currentDate));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                Date date = (Date) data.getSerializableExtra("date");
                changeDate(date);

            }
        }
    }
}
