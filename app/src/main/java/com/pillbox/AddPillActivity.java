package com.pillbox;

import android.accessibilityservice.GestureDescription;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;
import java.util.Calendar;

import static com.pillbox.PillboxDB.insertMedication;
import static com.pillbox.PillboxDB.insertMedicationSchedule;

public class AddPillActivity extends AppCompatActivity implements View.OnClickListener {

    EditText pillText, descText, dosageText, timeText;
    Button createButton, timeButton;
    CheckBox sundayCheckBox, mondayCheckBox, tuesdayCheckBox, wednesdayCheckBox, thursdayCheckBox, fridayCheckBox, saturdayCheckBox, everydayCheckBox;

    public AddPillActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pill);

        pillText = (EditText)findViewById(R.id.pillText);
        descText = (EditText)findViewById(R.id.descText);
      // dateText = (EditText)findViewById(R.id.dateText);
      //  timeText = (EditText)findViewById(R.id.timeText);
        dosageText = (EditText)findViewById(R.id.dosageText);
        timeButton = (Button)findViewById(R.id.timeButton);
        createButton = (Button)findViewById(R.id.createButton);
        timeButton = (Button)findViewById(R.id.timeButton);
        sundayCheckBox = (CheckBox)findViewById(R.id.sundayBox);
        mondayCheckBox = (CheckBox)findViewById(R.id.mondayBox);
        tuesdayCheckBox = (CheckBox)findViewById(R.id.tuesdayBox);
        wednesdayCheckBox = (CheckBox)findViewById(R.id.wednesdayBox);
        thursdayCheckBox = (CheckBox)findViewById(R.id.thursdayBox);
        fridayCheckBox = (CheckBox)findViewById(R.id.fridayBox);
        saturdayCheckBox = (CheckBox)findViewById(R.id.saturdayBox);
        everydayCheckBox = (CheckBox)findViewById(R.id.everyOtherBox);
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
        mTimePicker = new TimePickerDialog(AddPillActivity.this, AlertDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                timeButton.setText(selectedHour + ":" + selectedMinute);
            }
        }, hour, minute, false);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    public void deleteClick(View view)
    {
        goToMainActivity();
    }



    public void onClick(View view)
    {
        PillboxDB.insertMedication(pillText.getText().toString(), descText.getText().toString(), null);
        if(everydayCheckBox.isChecked())
        {
            insertMedicationSchedule("Test User", pillText.getText().toString(), Integer.parseInt(dosageText.getText().toString()), Globals.DayOfWeek.SUNDAY, timeButton.getText().toString());
            insertMedicationSchedule("Test User", pillText.getText().toString(), Integer.parseInt(dosageText.getText().toString()), Globals.DayOfWeek.MONDAY, timeButton.getText().toString());
            insertMedicationSchedule("Test User", pillText.getText().toString(), Integer.parseInt(dosageText.getText().toString()), Globals.DayOfWeek.TUESDAY, timeButton.getText().toString());
            insertMedicationSchedule("Test User", pillText.getText().toString(), Integer.parseInt(dosageText.getText().toString()), Globals.DayOfWeek.WEDNESDAY, timeButton.getText().toString());
            insertMedicationSchedule("Test User", pillText.getText().toString(), Integer.parseInt(dosageText.getText().toString()), Globals.DayOfWeek.THURSDAY, timeButton.getText().toString());
            insertMedicationSchedule("Test User", pillText.getText().toString(), Integer.parseInt(dosageText.getText().toString()), Globals.DayOfWeek.FRIDAY, timeButton.getText().toString());
            insertMedicationSchedule("Test User", pillText.getText().toString(), Integer.parseInt(dosageText.getText().toString()), Globals.DayOfWeek.SATURDAY, timeButton.getText().toString());
        }
        else
        {
            if (sundayCheckBox.isChecked()) {
                insertMedicationSchedule("Test User", pillText.getText().toString(), Integer.parseInt(dosageText.getText().toString()), Globals.DayOfWeek.SUNDAY, timeButton.getText().toString());
            }
            if (mondayCheckBox.isChecked()) {
                insertMedicationSchedule("Test User", pillText.getText().toString(), Integer.parseInt(dosageText.getText().toString()), Globals.DayOfWeek.MONDAY, timeButton.getText().toString());
            }
            if (tuesdayCheckBox.isChecked()) {
                insertMedicationSchedule("Test User", pillText.getText().toString(), Integer.parseInt(dosageText.getText().toString()), Globals.DayOfWeek.TUESDAY, timeButton.getText().toString());
            }
            if (wednesdayCheckBox.isChecked()) {
                insertMedicationSchedule("Test User", pillText.getText().toString(), Integer.parseInt(dosageText.getText().toString()), Globals.DayOfWeek.WEDNESDAY, timeButton.getText().toString());
            }
            if (thursdayCheckBox.isChecked()) {
                insertMedicationSchedule("Test User", pillText.getText().toString(), Integer.parseInt(dosageText.getText().toString()), Globals.DayOfWeek.THURSDAY, timeButton.getText().toString());
            }
            if (fridayCheckBox.isChecked()) {
                insertMedicationSchedule("Test User", pillText.getText().toString(), Integer.parseInt(dosageText.getText().toString()), Globals.DayOfWeek.FRIDAY, timeButton.getText().toString());
            }
            if (saturdayCheckBox.isChecked()) {
                insertMedicationSchedule("Test User", pillText.getText().toString(), Integer.parseInt(dosageText.getText().toString()), Globals.DayOfWeek.SATURDAY, timeButton.getText().toString());
            }
        }
        //insertMedicationSchedule("Test User", pillText.getText().toString(), Integer.parseInt(dosageText.getText().toString()), Globals.DayOfWeek.FRIDAY, timeButton.getText().toString());

        //PillboxDB.insertMedicationSchedule("Test User", pillText.getText().toString(), Integer.parseInt(dosageText.getText().toString()), Globals.DayOfWeek.TUESDAY, timeButton.getText().toString());

        goToMainActivity();
    }

}
