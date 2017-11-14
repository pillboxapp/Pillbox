package com.pillbox;

import android.accessibilityservice.GestureDescription;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import java.util.Calendar;

import static com.pillbox.PillboxDB.insertMedication;
import static com.pillbox.PillboxDB.insertMedicationSchedule;

public class AddPillActivity extends AppCompatActivity implements View.OnClickListener {

    EditText pillText, descText, dateText, dosageText, timeText;
    Button createButton;
    SQLiteDatabase db;

    public AddPillActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pill);

        pillText = (EditText)findViewById(R.id.pillText);
        descText = (EditText)findViewById(R.id.descText);
        dateText = (EditText)findViewById(R.id.dateText);
        timeText = (EditText)findViewById(R.id.timeText);
        dosageText = (EditText)findViewById(R.id.dosageText);
        createButton = (Button)findViewById(R.id.createButton);
        createButton.setOnClickListener(this);
      //  db=openOrCreateDatabase("PillboxDB", Context.MODE_PRIVATE, null);
        //db.execSQL("CREATE TABLE IF NOT EXISTS pill();");


    }

    public void goToMainActivity() {
        Intent myIntent = new Intent(AddPillActivity.this, MainActivity.class);
        AddPillActivity.this.startActivity(myIntent);
    }

    public void timeClick(View view)
    {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(AddPillActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                timeText.setText(selectedHour + ":" + selectedMinute);
            }
        }, hour, minute, true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }


    public void onClick(View view)
    {

        insertMedication(pillText.getText().toString(), descText.getText().toString(), null);
        insertMedicationSchedule("Test User", pillText.getText().toString(), Integer.parseInt(dosageText.getText().toString()), Globals.DayOfWeek.TUESDAY, timeText.getText().toString());
        goToMainActivity();
        return;
    }

}
