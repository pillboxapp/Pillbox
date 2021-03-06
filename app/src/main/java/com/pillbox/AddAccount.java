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

import static com.pillbox.PillboxDB.getUserID;
import static com.pillbox.PillboxDB.insertUser;
import static com.pillbox.PillboxDB.insertMedicationSchedule;

public class AddAccount extends AppCompatActivity implements View.OnClickListener{

    EditText nameText;
    Button addAccountButton, backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);

        nameText = (EditText)findViewById(R.id.nameText);
        backButton = (Button)findViewById(R.id.backButton);
        addAccountButton = (Button)findViewById(R.id.addAccountButton);
        addAccountButton.setOnClickListener(this);
    }

    public void goToMainActivity() {
        Intent myIntent = new Intent(AddAccount.this, MainActivity.class);
        AddAccount.this.startActivity(myIntent);
    }

    public void backClick(View view)
    {
        goToMainActivity();
        return;
    }

    public void onClick(View view)
    {
        if (nameText.getText().toString().length() > 1) {
            insertUser(nameText.getText().toString());
            Globals.userID = getUserID(nameText.getText().toString());
            goToMainActivity();
            return;
        }
    }
}
