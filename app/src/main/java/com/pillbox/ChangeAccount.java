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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

import static com.pillbox.PillboxDB.getUserID;

public class ChangeAccount extends AppCompatActivity implements View.OnClickListener{

    Button changeAccountButton, addAccountButton;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_account);

        addAccountButton = (Button)findViewById(R.id.addAccountButton);
        changeAccountButton = (Button)findViewById(R.id.changeAccountButton);
        changeAccountButton.setOnClickListener(this);
        spinner = (Spinner) findViewById(R.id.spinner_nav);
        addItemsToSpinner();

    }

    public void goToMainActivity() {
        Intent myIntent = new Intent(ChangeAccount.this, MainActivity.class);
        ChangeAccount.this.startActivity(myIntent);
    }

    public void goToAddAccountActivity(View view){
        Intent myIntent = new Intent(ChangeAccount.this, AddAccount.class);
        ChangeAccount.this.startActivity(myIntent);
    }

    public void addItemsToSpinner(){
        final List<String> list = PillboxDB.getUsers();
        list.add(0, " Change Account");

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Context context = getApplicationContext();
                String text = list.get(position);
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                if (text.equals(" Change Account")){

                }
                else{
                    Globals.userID = getUserID(text);
                    goToMainActivity();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    public void onClick(View view)
    {
        goToMainActivity();
        return;
    }
}
