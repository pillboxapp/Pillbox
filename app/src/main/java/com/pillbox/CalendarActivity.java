package com.pillbox;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    MaterialCalendarView widget;
    Spinner spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        Toolbar toolBar = findViewById(R.id.calendar_toolbar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        spinner = (Spinner) findViewById(R.id.spinner_nav);
        addItemsToSpinner();

        widget = (MaterialCalendarView) findViewById(R.id.calendarView);

        try {
            widget.addDecorators(new RedDecorator(), new GreenDecorator());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        widget.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                Intent intent = new Intent();
                intent.putExtra("date", date.getDate());
                setResult(RESULT_OK, intent);
                finish();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void goToSettings(MenuItem item) {
        Intent myIntent = new Intent(CalendarActivity.this, Settings.class);
        CalendarActivity.this.startActivity(myIntent);
    }

    public void goToAddAccount(MenuItem item) {
        Intent myIntent = new Intent(CalendarActivity.this, AddAccount.class);
        CalendarActivity.this.startActivity(myIntent);
    }

    public void goToChangeAccount(MenuItem item) {
        Intent myIntent = new Intent(CalendarActivity.this, ChangeAccount.class);
        CalendarActivity.this.startActivity(myIntent);
    }

    public void addItemsToSpinner(){
        final List<String> list = PillboxDB.getMedications();
        list.add(0, "Overview");


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Reload calendar for selected medication
                //updateCalendar
                Context context = getApplicationContext();
                String text = list.get(position);
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                widget.removeDecorators();
                if(text.equals("Overview")){
                    try {
                        widget.addDecorators(new RedDecorator(), new GreenDecorator());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }else{
                    try {
                        widget.addDecorators(new RedDecorator(text), new GreenDecorator(text));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
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
    public void goToMain(View view) {
        finish();
    }
}
