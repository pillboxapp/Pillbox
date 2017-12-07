package com.pillbox;

import android.Manifest;
import android.accessibilityservice.GestureDescription;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.List;


public class AddPillActivity extends AppCompatActivity implements View.OnClickListener {

    EditText pillText, descText, dosageText, editTime;
    Button createButton;
    ToggleButton sundayCheckBox, mondayCheckBox, tuesdayCheckBox, wednesdayCheckBox, thursdayCheckBox, fridayCheckBox, saturdayCheckBox, everydayCheckBox;
    ImageButton imageButton;
    Boolean image_selected;
    private static final int MY_CAMERA_REQUEST_CODE = 100;

    public AddPillActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pill);
        image_selected = false;
        pillText = (EditText)findViewById(R.id.pillText);
        descText = (EditText)findViewById(R.id.descText);
        editTime = (EditText)findViewById(R.id.editTime);
        dosageText = (EditText)findViewById(R.id.dosageText);
        createButton = (Button)findViewById(R.id.createButton);
        sundayCheckBox = (ToggleButton)findViewById(R.id.sundayBox);
        sundayCheckBox.setChecked(true);
        mondayCheckBox = (ToggleButton)findViewById(R.id.mondayBox);
        mondayCheckBox.setChecked(true);
        tuesdayCheckBox = (ToggleButton)findViewById(R.id.tuesdayBox);
        tuesdayCheckBox.setChecked(true);
        wednesdayCheckBox = (ToggleButton)findViewById(R.id.wednesdayBox);
        wednesdayCheckBox.setChecked(true);
        thursdayCheckBox = (ToggleButton)findViewById(R.id.thursdayBox);
        thursdayCheckBox.setChecked(true);
        fridayCheckBox = (ToggleButton)findViewById(R.id.fridayBox);
        fridayCheckBox.setChecked(true);
        saturdayCheckBox = (ToggleButton)findViewById(R.id.saturdayBox);
        saturdayCheckBox.setChecked(true);
        everydayCheckBox = (ToggleButton)findViewById(R.id.everyOtherBox);
        everydayCheckBox.setChecked(true);

        imageButton = (ImageButton) findViewById(R.id.imgButton);
        createButton.setOnClickListener(this);
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            pillText.setText(extras.getString("name") , TextView.BufferType.EDITABLE);
            descText.setText(extras.getString("desc"), TextView.BufferType.EDITABLE);
            dosageText.setText(String.valueOf(extras.getDouble("dosage")), TextView.BufferType.EDITABLE);
            editTime.setText(extras.getString("time"), TextView.BufferType.EDITABLE);

            createButton.setText(R.string.update_pill_button_title);
        }


    }

    public void goToMainActivity() {
        Intent myIntent = new Intent(AddPillActivity.this, MainActivity.class);
        AddPillActivity.this.startActivity(myIntent);
        finish();
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
                editTime.setText(Globals.reformatDate("HH:mm", "hh:mm a",
                        String.format(Locale.ENGLISH, "%02d:%02d", selectedHour, selectedMinute)));
            }
        }, hour, minute, false);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    public void everyCheck(View view)
    {
        if(everydayCheckBox.isChecked())
        {
            sundayCheckBox.setChecked(true);
            mondayCheckBox.setChecked(true);
            tuesdayCheckBox.setChecked(true);
            wednesdayCheckBox.setChecked(true);
            thursdayCheckBox.setChecked(true);
            fridayCheckBox.setChecked(true);
            saturdayCheckBox.setChecked(true);
        }
        else if(everydayCheckBox.isChecked() == false)
        {
            sundayCheckBox.setChecked(false);
            mondayCheckBox.setChecked(false);
            tuesdayCheckBox.setChecked(false);
            wednesdayCheckBox.setChecked(false);
            thursdayCheckBox.setChecked(false);
            fridayCheckBox.setChecked(false);
            saturdayCheckBox.setChecked(false);
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onClick(View view)
    {

        Bundle extras = getIntent().getExtras();
        if(view.getId() == R.id.imgButton){
            cameraClick(view);

        }else{
            try {
                String pillName = pillText.getText().toString();
                String description = descText.getText().toString();

                if (pillName.length() == 0) {
                    Toast.makeText(this, "Pill name must be entered", Toast.LENGTH_LONG).show();
                    return;
                }
                if (dosageText.getText().toString().length() == 0) {
                    Toast.makeText(this, "Dosage must be entered", Toast.LENGTH_LONG).show();
                    return;
                }
                if (editTime.getText().toString().length() == 0) {
                    Toast.makeText(this, "Time must be entered", Toast.LENGTH_LONG).show();
                    return;
                }

                // Add new pill
                if (extras == null || extras.getString("edit") == null) {
                    PillboxDB.insertMedication(pillName, description, this.getImageArray());
                }
                // Edit pill
                else if (extras.getString("edit") != null && extras.getString("edit").equals("yes")) {
                    String previousName = extras.getString("previousName");
                    PillboxDB.deleteMedicationSchedule(previousName, getApplicationContext());
                    if (image_selected) {
                        PillboxDB.updateMedication(previousName, pillName, description, this.getImageArray());
                    }
                    else {
                        PillboxDB.updateMedication(previousName, pillName, description, null);
                    }
                }

            }
            catch (SQLiteConstraintException ex) {
                Toast.makeText(this, "Pill name already exists", Toast.LENGTH_LONG).show();
                return;
            }
            if(everydayCheckBox.isChecked())
            {
                for (Globals.DayOfWeek day: Globals.DayOfWeek.values()) {
                    createMedicationForDay(day);
                }
            }
            else
            {
                boolean dayChecked = false;
                if (sundayCheckBox.isChecked()) {
                    createMedicationForDay(Globals.DayOfWeek.SUNDAY);
                    dayChecked = true;
                }
                if (mondayCheckBox.isChecked()) {
                    createMedicationForDay(Globals.DayOfWeek.MONDAY);
                    dayChecked = true;
                }
                if (tuesdayCheckBox.isChecked()) {
                    createMedicationForDay(Globals.DayOfWeek.TUESDAY);
                    dayChecked = true;
                }
                if (wednesdayCheckBox.isChecked()) {
                    createMedicationForDay(Globals.DayOfWeek.WEDNESDAY);
                    dayChecked = true;
                }
                if (thursdayCheckBox.isChecked()) {
                    createMedicationForDay(Globals.DayOfWeek.THURSDAY);
                    dayChecked = true;
                }
                if (fridayCheckBox.isChecked()) {
                    createMedicationForDay(Globals.DayOfWeek.FRIDAY);
                    dayChecked = true;
                }
                if (saturdayCheckBox.isChecked()) {
                    createMedicationForDay(Globals.DayOfWeek.SATURDAY);
                    dayChecked = true;
                }

                if (!dayChecked) {
                    Toast.makeText(this, "At least one day must be selected", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            goToMainActivity();
        }
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private byte[] getImageArray() {
        Bitmap imageBitmap;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        if(image_selected){
            imageBitmap = ((BitmapDrawable)imageButton.getDrawable()).getBitmap();
        }
        else{
            imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_placeholder);
        }

        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        return bos.toByteArray();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void cameraClick(View view){
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    MY_CAMERA_REQUEST_CODE);
        }else{
            dispatchTakePictureIntent();
        }

    }

    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_REQUEST_CODE) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

               dispatchTakePictureIntent();

            } else {

                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();

            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageButton.setImageBitmap(imageBitmap);
            image_selected = true;

        }
    }

    private void createMedicationForDay(Globals.DayOfWeek day) {
        PillboxDB.insertMedicationSchedule(pillText.getText().toString(), Double.parseDouble(dosageText.getText().toString()), day,
                editTime.getText().toString(), getApplicationContext());
//        Bundle extras = getIntent().getExtras();
//        if (extras != null && extras.getString("edit").equals("yes")) {
//            updateMedicationSchedule(extras.getString("previousName"), pillText.getText().toString(),
//                    Double.parseDouble(dosageText.getText().toString()), day, editTime.getText().toString(), getApplicationContext());
//        } else {
//            insertMedicationSchedule(pillText.getText().toString(), Double.parseDouble(dosageText.getText().toString()), day,
//                    editTime.getText().toString(), getApplicationContext());
//
//        }
    }

}
