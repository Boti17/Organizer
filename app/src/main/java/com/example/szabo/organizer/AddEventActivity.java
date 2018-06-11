package com.example.szabo.organizer;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class AddEventActivity extends AppCompatActivity {

    private int year;
    private int month;
    private int day;
    private Date mDate;
    private Time mTime;
    private static final int RC_MAP = 1;
    private LatLng mLocation;
    private FirebaseAuth mAuth;
    private ImageButton mSelectImageButton;
    private static final int GALLERY_REQUEST = 2;
    private Uri eventImageUri = null;
    private StorageReference mStorage;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        mStorage = FirebaseStorage.getInstance().getReference().child("pictures");
        mProgress = new ProgressDialog(this);
        Button mDatePickerButton = (Button) findViewById(R.id.datePickerButton);
        mDatePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDate();
            }
        });
        Button mLocationButton = (Button) findViewById(R.id.locationButton);
        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(AddEventActivity.this, MapsActivity.class);
                startActivityForResult(mapIntent, RC_MAP);
            }
        });
        Button mAddEventButton = (Button) findViewById(R.id.addEventButton);
        mAddEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEventToFireBase();
            }
        });
        mSelectImageButton = (ImageButton) findViewById(R.id.selectImageButton);
        mSelectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });
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
            new TimePickerDialog(AddEventActivity.this,timePickerDialog,0,0, true).show();
        }
    };

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

    TimePickerDialog.OnTimeSetListener timePickerDialog = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int i, int i1) {
            mTime = new Time();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mTime.set(0,timePicker.getMinute(),timePicker.getHour(), 0, 0, 0);
            }
            else
            {
                mTime.set(0, timePicker.getCurrentMinute(),timePicker.getCurrentHour(), 0,0,0);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){
            case RC_MAP:
                if(resultCode == Activity.RESULT_OK){
                    Bundle result = data.getExtras();
                    mLocation = (LatLng) result.get("result");
                }
                break;
            case GALLERY_REQUEST:
                if (resultCode == RESULT_OK)
                {
                    Uri imageUri = data.getData();
                    eventImageUri = imageUri;
                    mSelectImageButton.setImageURI(eventImageUri);
                }
                break;
            default:
                break;
        }
    }

    private void addEventToFireBase() {
        mProgress.setMessage(getResources().getString(R.string.uploadEvent));
        mProgress.show();
        EditText mEventTypeField = (EditText) findViewById(R.id.eventTypeField);
        EditText mDescriptionField = (EditText) findViewById(R.id.descriptionField);
        final String eventType = mEventTypeField.getText().toString();
        final String eventDescription = mDescriptionField.getText().toString();
        if (eventType.length() > 0 && eventType.length() <= 30)
        {
            if (eventDescription.length()>0)
            {
                if (mDate != null)
                {
                    if (mTime != null)
                    {
                        if (mLocation != null)
                        {
                            mAuth = FirebaseAuth.getInstance();
                            mDate.setHours(mTime.hour);
                            mDate.setMinutes(mTime.minute);
                            mDate.setYear(mDate.getYear()-1900);
                            if ((new Date()).getTime() > mDate.getTime()) {
                                Toast.makeText(AddEventActivity.this, getResources().getString(R.string.pastStartingDate), Toast.LENGTH_LONG).show();
                                mDate = null;
                                mTime = null;
                            }
                            else {
                                mDate.setYear(mDate.getYear()+1900);
                                DatabaseReference mDatabaseEvents = FirebaseDatabase.getInstance().getReference().child("events");
                                final DatabaseReference eventReference = mDatabaseEvents.push();
                                final DatabaseReference mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
                                mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(final DataSnapshot dataSnapshot) {
                                        if (eventImageUri == null) {
                                            Event event = new Event(eventType, eventDescription, mDate, mLocation, mAuth.getCurrentUser().getUid(), dataSnapshot.child("name").getValue().toString(), dataSnapshot.child("picture").getValue().toString(),"null");
                                            addEvent(event, eventReference);
                                        } else
                                        {
                                            StorageReference filePath;
                                            filePath = mStorage.child(eventImageUri.getLastPathSegment());
                                            filePath.putFile(eventImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    String downloadUri = taskSnapshot.getDownloadUrl().toString();
                                                    Event event = new Event(eventType, eventDescription, mDate, mLocation, mAuth.getCurrentUser().getUid(), dataSnapshot.child("name").getValue().toString(), dataSnapshot.child("picture").getValue().toString(), downloadUri);
                                                    addEvent(event, eventReference);
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        } else {Toast.makeText(AddEventActivity.this, getResources().getString(R.string.selectALocation), Toast.LENGTH_LONG).show();}
                    } else {Toast.makeText(AddEventActivity.this, getResources().getString(R.string.startingTime), Toast.LENGTH_LONG).show();}
                } else {Toast.makeText(AddEventActivity.this, getResources().getString(R.string.startingDate), Toast.LENGTH_LONG).show();}
            } else {Toast.makeText(AddEventActivity.this, getResources().getString(R.string.emptyDescription), Toast.LENGTH_LONG).show();}
        } else {Toast.makeText(AddEventActivity.this, getResources().getString(R.string.eventTypeError), Toast.LENGTH_LONG).show();}
    }

    public void addEvent(Event event, DatabaseReference eventReference)
    {
        eventReference.setValue(event).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Intent listIntent = new Intent(AddEventActivity.this, EventListActivity.class);
                    listIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(listIntent);
                    mProgress.dismiss();
                }
            }
        });
    }
}
