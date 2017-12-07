package com.pillbox;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pillbox.DailyViewContent.DailyViewRow;
import com.pillbox.DailyViewRowRecyclerViewAdapter.ViewHolder;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import static com.pillbox.PillboxDB.deleteMedicationSchedule;

public class MainActivity extends AppCompatActivity implements DailyViewFragment.OnListFragmentInteractionListener,
        DailyViewFragment.OnUserControlsLoadedListener {
    private Date currentDate;
    private ViewHolder selectedRow;
    private RecyclerView recyclerView;

    private static boolean initialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolBar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolBar);

        // Show the current date on the screen to start
        this.setDate(Globals.getCurrentDate());

        PillboxDB.setContext(this);

        if (!initialized) {
            Globals.userID = 1;
            SQLiteDatabase sqliteDB;

            Globals.notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            Globals.alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
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
            if (PillboxDB.getUsers().isEmpty()){
                Intent myIntent = new Intent(MainActivity.this, AddAccount.class);
                MainActivity.this.startActivityForResult(myIntent, 1);
            }
            else{
                Intent myIntent = new Intent(MainActivity.this, ChangeAccount.class);
                MainActivity.this.startActivityForResult(myIntent, 1);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        this.checkForReadyPills();
    }

    private void checkForReadyPills() {
        final Runnable update = new Runnable() {
            public void run() {
                recyclerView.post(new Runnable() {
                    public void run() {
                        updateReadyPills();
                    }
                });
            }
        };

        Timer timer = new Timer();

        // Check pills every 10 seconds
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                update.run();
            }
        }, 5000, 10000);

        update.run();
    }

    private void updateReadyPills() {
        if (recyclerView == null) {
            return;
        }
        int count = recyclerView.getAdapter().getItemCount();

        for (int i = 0; i < count; i++) {
            ViewHolder row = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
            if (row != null && row.mItem.date.compareTo(Globals.getCurrentDate()) <= 0 &&
                    row.mItem.getStatus() == Globals.Status.UPCOMING) {
                markPillAsReady(row);
            }
        }
    }

    public void onUserControlsLoaded(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
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
        BitmapDrawable drawable = (BitmapDrawable) holder.mPillPic.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        ImageView imageView = findViewById(R.id.detailed_view_image);
        imageView.setImageBitmap(bitmap);
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

    public void goToDeleteAccount(MenuItem item) {
        Intent myIntent = new Intent(MainActivity.this, DeleteAccount.class);
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
        DailyViewFragment dailyView = this.getDailyViewFragment();
        dailyView.reloadData();
        this.setRecyclerViewVisibility();
        this.resetDetailedView();
    }

    private DailyViewFragment getDailyViewFragment() {
        return (DailyViewFragment)getSupportFragmentManager().findFragmentById(R.id.daily_view_fragment);
    }

    private void updatePillStatus(Globals.Status newStatus, ViewHolder row) {
        // Don't do anything if no pill is selected
        if (row == null) {
            return;
        }

        PillboxDB.updateStatus(row.mItem.rowID, newStatus);
        row.mItem.updateStatus(newStatus);

        Globals.updateStatusImage(row.mStatusView, newStatus);
    }

    public void skipPill(View view) {
        this.updatePillStatus(Globals.Status.SKIPPED, this.selectedRow);
    }

    public void takePill(View view) {
        this.updatePillStatus(Globals.Status.TAKEN, this.selectedRow);
    }

    public void goToEditPill(View view) {
        if(selectedRow != null){
            Intent myIntent = new Intent(MainActivity.this, AddPillActivity.class);
            myIntent.putExtra("name", selectedRow.mPillNameView.getText().toString());
            myIntent.putExtra("edit", "yes");
            myIntent.putExtra("dosage", selectedRow.mItem.dosage);
            myIntent.putExtra("desc", selectedRow.mItem.pillDesc);
            myIntent.putExtra("date", selectedRow.mItem.date);
            myIntent.putExtra("time", selectedRow.mItem.displayTime);
            MainActivity.this.startActivity(myIntent);
            DailyViewFragment dailyView = this.getDailyViewFragment();
            dailyView.reloadData();
            this.setRecyclerViewVisibility();
            this.resetDetailedView();
        }else{
            Toast.makeText(this, "Must select pill first.", Toast.LENGTH_LONG).show();
        }

    }

    public void deletePill(View view) {
        if(selectedRow != null){
            deleteMedicationSchedule(selectedRow.mPillNameView.getText().toString());
            DailyViewFragment dailyView = this.getDailyViewFragment();
            dailyView.reloadData();
            this.setRecyclerViewVisibility();
            this.resetDetailedView();
        }else{
            Toast.makeText(this, "Must select pill before it can be deleted.", Toast.LENGTH_LONG).show();
        }
    }


    public void markPillAsReady(ViewHolder selectedRow) {
        this.updatePillStatus(Globals.Status.TIME_TO_TAKE, selectedRow);
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

    public void setRecyclerViewVisibility(){
        TextView textview = findViewById(R.id.recycler_empty_view);
        if(recyclerView.getAdapter().getItemCount() == 0){
            this.recyclerView.setVisibility(View.GONE);
            textview.setVisibility(View.VISIBLE);
        }else{
            this.recyclerView.setVisibility(View.VISIBLE);
            textview.setVisibility(View.GONE);
        }
    }
}
