package com.example.szabo.organizer;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class SearchActivity extends AppCompatActivity {

    private int year;
    private int month;
    private int day;
    private Date mDate;
    private String eventType;
    private String eventLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final EditText mEventTypeField = (EditText) findViewById(R.id.eventTypeField);
        final EditText mLocationField = (EditText) findViewById(R.id.locationField);
        Button mSelectDateButton = (Button) findViewById(R.id.selectDateButton);
        mSelectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDate();
            }
        });
        Button mSearchButton = (Button) findViewById(R.id.searchButton);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventType = mEventTypeField.getText().toString();
                eventLocation = mLocationField.getText().toString();
                startNewActivity();
            }
        });
    }

    private void selectDate()
    {
        mDate = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(mDate);
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDialog(0);
    }

    @Override
    @Deprecated
    protected Dialog onCreateDialog(int id) {
        return new DatePickerDialog(this, datePickerListener, year, month, day);
    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
            day = selectedDay;
            month = selectedMonth;
            year = selectedYear;
            mDate = new Date(year, month, day);
        }
    };

    private void startNewActivity() {
        Intent listIntent = new Intent(SearchActivity.this, EventListActivity.class);
        listIntent.putExtra("eventType", eventType);
        listIntent.putExtra("eventLocation", eventLocation);
        listIntent.putExtra("eventDate", mDate);
        listIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(listIntent);
    }
}
