package com.pillbox;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
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

    private final int NOTIFICATION_DELAY_MINS = 15;

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

            Globals.notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            Globals.alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            try {
                // TODO: Remove the following statement when done changing the database
                //this.deleteDatabase(getResources().getString(R.string.db_name));

                sqliteDB = this.openOrCreateDatabase(getResources().getString(R.string.db_name), MODE_PRIVATE, null);

                // Set the instance of sql to be used for the duration of the app's life
                PillboxDB.setDB(sqliteDB);
                PillboxDB.createTables();

                PillboxDB.addMissingHeaders(getApplicationContext());

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
        this.setRecyclerViewVisibility();
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
            if (row != null && row.mItem.getDate().compareTo(Globals.getCurrentDate()) <= 0 &&
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
        BitmapDrawable drawable = (BitmapDrawable) holder.mPillPic.getDrawable();
        this.updateDetailedText(item.pillName, item.pillDesc, item.getDisplayTime(), item.dosage, drawable);
    }

    private void updateDetailedText(String pillName, String pillDesc, String displayTime, double dosage, BitmapDrawable drawable) {
        TextView description = findViewById(R.id.detailed_view_pill_description);
        TextView name = findViewById(R.id.detailed_view_pill_name);
        TextView time = findViewById(R.id.detailed_view_pill_time);
        ImageView imageView = findViewById(R.id.detailed_view_image);

        name.setText(pillName);
        description.setText(pillDesc);
        if (dosage > 0) {
            String pillText = dosage > 1 ? "pills" : "pill";
            String pillTime = MessageFormat.format("Take {0} {1} at {2}", dosage, pillText, displayTime);
            time.setText(pillTime);
        }
        else {
            time.setText("");
        }
        if (drawable != null) {
            imageView.setImageBitmap(drawable.getBitmap());
        }
        else {
            imageView.setImageBitmap(null);
        }
    }

    private void resetDetailedView() {
        this.updateDetailedText("", "", "", 0, null);
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
        if (selectedRow == null) {
            Toast.makeText(this, "No pill selected", Toast.LENGTH_LONG).show();
            return;
        }
        this.updatePillStatus(Globals.Status.SKIPPED, this.selectedRow);
        // The pill has been skipped, so no need to show a notification
        Globals.deleteAlarm(getApplicationContext(), this.selectedRow.mItem.alarmCode);
    }

    public void takePill(View view) {
        if (selectedRow == null) {
            Toast.makeText(this, "No pill selected", Toast.LENGTH_LONG).show();
            return;
        }
        Calendar pillTime = Calendar.getInstance();
        pillTime.setTime(selectedRow.mItem.getDate());
        if (pillTime.get(Calendar.DAY_OF_YEAR) > Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) {
            Toast.makeText(this, "Cannot take pills scheduled on a future day", Toast.LENGTH_LONG).show();
            return;
        }

        this.updatePillStatus(Globals.Status.TAKEN, this.selectedRow);
        // The pill has been taken, so no need to show a notification
        Globals.deleteAlarm(getApplicationContext(), this.selectedRow.mItem.alarmCode);

        Date takenTime = Calendar.getInstance().getTime();
        selectedRow.mItem.updateDate(takenTime);
        PillboxDB.updatePillTime(selectedRow.mItem.rowID, takenTime);

        DailyViewFragment dailyViewFragment = this.getDailyViewFragment();
        dailyViewFragment.reloadData();
    }

    public void remindMe(View view) {
        if (selectedRow == null) {
            Toast.makeText(this, "No pill selected", Toast.LENGTH_LONG).show();
            return;
        }

        // Confirm user wants to change pill time
        AlertDialog alertDialog = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK).create();
        alertDialog.setTitle("Confirm Changes");
        alertDialog.setMessage(MessageFormat.format("Take {0} {1} minutes later than scheduled?",
                selectedRow.mItem.pillName, NOTIFICATION_DELAY_MINS));

        // Setting OK Button
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                updateAlarmTime();
            }
        });

        // Setting Cancel button
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        alertDialog.show();
    }

    private void updateAlarmTime() {
        int SECONDS_IN_MINUTE = 60;
        int MILLIS_IN_SECOND = 1000;
        long newAlarmTime = selectedRow.mItem.getAlarmTime() + (NOTIFICATION_DELAY_MINS * SECONDS_IN_MINUTE * MILLIS_IN_SECOND);

        int rowID = selectedRow.mItem.rowID;
        int alarmCode = selectedRow.mItem.alarmCode;

        // Calculate time + new notification time
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedRow.mItem.getDate());
        calendar.add(Calendar.MINUTE, NOTIFICATION_DELAY_MINS);
        Date newDate = calendar.getTime();
        selectedRow.mItem.updateDate(newDate);
        PillboxDB.updatePillTime(rowID, newDate);

        String pillName = selectedRow.mItem.pillName;
        String pillTime = selectedRow.mItem.getDisplayTime();
        String pillDesc = selectedRow.mItem.pillDesc;
        double dosage = selectedRow.mItem.dosage;
        BitmapDrawable drawable = (BitmapDrawable)selectedRow.mPillPic.getDrawable();

        Globals.updateAlarmTime(getApplicationContext(), newAlarmTime, pillName, pillTime, alarmCode);

        // Update UI
        this.updateDetailedText(pillName, pillDesc, pillTime, dosage, drawable);
        this.markPillAsUpcoming();

        DailyViewFragment dailyViewFragment = this.getDailyViewFragment();
        dailyViewFragment.reloadData();
    }


    public void goToEditPill(View view) {
        if (selectedRow == null) {
            Toast.makeText(this, "Must select pill first.", Toast.LENGTH_LONG).show();
            return;
        }
        if (toastIfPillInPast("Cannot edit records in the past")) {
            return;
        }

        Intent myIntent = new Intent(MainActivity.this, AddPillActivity.class);
        myIntent.putExtra("name", selectedRow.mPillNameView.getText().toString());
        myIntent.putExtra("edit", "yes");
        myIntent.putExtra("dosage", selectedRow.mItem.dosage);
        myIntent.putExtra("desc", selectedRow.mItem.pillDesc);
        myIntent.putExtra("date", selectedRow.mItem.getDate());
        myIntent.putExtra("time", selectedRow.mItem.getDisplayTime());
        myIntent.putExtra("previousName", selectedRow.mItem.pillName);
        MainActivity.this.startActivity(myIntent);
        DailyViewFragment dailyView = this.getDailyViewFragment();
        dailyView.reloadData();
        this.resetDetailedView();

    }

    public void deletePill(View view) {
        if (selectedRow == null) {
            Toast.makeText(this, "Must select pill before it can be deleted.", Toast.LENGTH_LONG).show();
            return;
        }
        if (toastIfPillInPast("Cannot delete records in the past")) {
            return;
        }
        deleteMedicationSchedule(selectedRow.mPillNameView.getText().toString(), getApplicationContext());
        DailyViewFragment dailyView = this.getDailyViewFragment();
        dailyView.reloadData();
        this.resetDetailedView();
    }


    public void markPillAsReady(ViewHolder selectedRow) {
        this.updatePillStatus(Globals.Status.TIME_TO_TAKE, selectedRow);
    }

    public void markPillAsUpcoming() {
        this.updatePillStatus(Globals.Status.UPCOMING, selectedRow);
    }

    private boolean toastIfPillInPast(String text) {
        if (Globals.getCurrentDate().compareTo(selectedRow.mItem.getDate()) >= 0) {
            this.makeToast(text);
            return true;
        }
        return false;
    }

    private void makeToast(String text, Object... formatArgs) {
        Toast toast = Toast.makeText(this, MessageFormat.format(text, formatArgs), Toast.LENGTH_SHORT);
        toast.show();
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
        if (textview == null || recyclerView == null) {
            return;
        }
        if(recyclerView.getAdapter().getItemCount() == 0){
            this.recyclerView.setVisibility(View.GONE);
            textview.setVisibility(View.VISIBLE);
        }else{
            this.recyclerView.setVisibility(View.VISIBLE);
            textview.setVisibility(View.GONE);
        }
    }
}
