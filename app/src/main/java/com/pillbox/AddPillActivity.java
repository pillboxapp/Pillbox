package com.pillbox;

import android.accessibilityservice.GestureDescription;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
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

    EditText pillText, descText, dosageText, editTime;
    Button createButton;
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
       editTime = (EditText)findViewById(R.id.editTime);
        //editTime.setShowSoftInputOnFocus(false);
        dosageText = (EditText)findViewById(R.id.dosageText);
        //editTime = (Button)findViewById(R.id.editTime);
        createButton = (Button)findViewById(R.id.createButton);
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
                editTime.setText(String.format("%02d", selectedHour) + ":" + String.format("%02d", selectedMinute));
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
        try {
            PillboxDB.insertMedication(pillText.getText().toString(), descText.getText().toString(), null);
        }
        catch (SQLiteConstraintException ex) {
            // TODO: Medication exists already, show error to user
        }
        if(everydayCheckBox.isChecked())
        {
            for (Globals.DayOfWeek day: Globals.DayOfWeek.values()) {
                createMedicationForDay(day);
            }
        }
        else
        {
            if (sundayCheckBox.isChecked()) {
                createMedicationForDay(Globals.DayOfWeek.SUNDAY);
            }
            if (mondayCheckBox.isChecked()) {
                createMedicationForDay(Globals.DayOfWeek.MONDAY);
            }
            if (tuesdayCheckBox.isChecked()) {
                createMedicationForDay(Globals.DayOfWeek.TUESDAY);
            }
            if (wednesdayCheckBox.isChecked()) {
                createMedicationForDay(Globals.DayOfWeek.WEDNESDAY);
            }
            if (thursdayCheckBox.isChecked()) {
                createMedicationForDay(Globals.DayOfWeek.THURSDAY);
            }
            if (fridayCheckBox.isChecked()) {
                createMedicationForDay(Globals.DayOfWeek.FRIDAY);
            }
            if (saturdayCheckBox.isChecked()) {
                createMedicationForDay(Globals.DayOfWeek.SATURDAY);
            }
        }
        //insertMedicationSchedule("Test User", pillText.getText().toString(), Integer.parseInt(dosageText.getText().toString()), Globals.DayOfWeek.FRIDAY, editTime.getText().toString());

        //PillboxDB.insertMedicationSchedule("Test User", pillText.getText().toString(), Integer.parseInt(dosageText.getText().toString()), Globals.DayOfWeek.TUESDAY, editTime.getText().toString());

        goToMainActivity();
    }

    private void createMedicationForDay(Globals.DayOfWeek day) {
        insertMedicationSchedule(pillText.getText().toString(), Double.parseDouble(dosageText.getText().toString()), day, editTime.getText().toString());
    }

}
