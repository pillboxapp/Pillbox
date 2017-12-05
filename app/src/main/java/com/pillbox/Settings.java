package com.pillbox;

/**
 * Created by James on 11/15/2017.
 */

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

public class Settings extends AppCompatActivity implements View.OnClickListener{

    Button returnButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        returnButton = (Button)findViewById(R.id.returnButton);
        returnButton.setOnClickListener(this);
    }

    public void goToMainActivity() {
        Intent myIntent = new Intent(Settings.this, MainActivity.class);
        Settings.this.startActivity(myIntent);
    }

    public void onClick(View view)
    {
        goToMainActivity();
        return;
    }
}
