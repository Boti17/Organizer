package com.example.szabo.organizer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class EventListActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private RecyclerView mEventList;
    private DatabaseReference mDatabaseEvents;
    private ArrayList<Event> eventList;
    private String searchedEventType;
    private String searchedEventLocation;
    private Date searchedEventDate;
    private Context context;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        if(!isNetworkConnected()) {
            Toast.makeText(EventListActivity.this, getResources().getString(R.string.checkInternet), Toast.LENGTH_LONG).show();
            while (!isNetworkConnected());
            initData();
        }
        else
        {
            initData();
        }
    }

    private void initData()
    {
        mProgress = new ProgressDialog(this);
        mProgress.setMessage(getResources().getString(R.string.loadingEvents));
        mProgress.show();
        Bundle extras = getIntent().getExtras();
        searchedEventType = extras.getString("eventType");
        if (searchedEventType == null)
        {
            searchedEventType = "";
        }
        searchedEventLocation = extras.getString("eventLocation");
        if (searchedEventLocation == null)
        {
            searchedEventLocation = "";
        }
        searchedEventDate = (Date) extras.getSerializable("eventDate");
        Button mAllEventButton = (Button) findViewById(R.id.allEventButton);
        if (searchedEventType.equals("") && searchedEventLocation.equals("") && searchedEventDate == null)
        {
            mAllEventButton.setVisibility(View.INVISIBLE);
        }
        else
        {
            mAllEventButton.setVisibility(View.VISIBLE);
        }
        mAllEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewIntent(EventListActivity.class);
            }
        });
        mDatabaseEvents = FirebaseDatabase.getInstance().getReference().child("events");
        mAuth = FirebaseAuth.getInstance();
        context = this;
        setUpEventList();
    }

    private void startNewIntent(Class activityClass) {
        Intent intent = new Intent(EventListActivity.this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            if (mAuth.getCurrentUser() != null) {
                startNewIntent(AddEventActivity.class);
            } else {
                Toast.makeText(EventListActivity.this, getResources().getString(R.string.loggedUsersCanCreate), Toast.LENGTH_LONG).show();
                startNewIntent(LoginActivity.class);
            }
        }
        if (item.getItemId() == R.id.action_profile) {
            if (mAuth.getCurrentUser() != null) {
                Intent profileIntent = new Intent(EventListActivity.this, ProfileActivity.class);
                profileIntent.putExtra("userId", mAuth.getCurrentUser().getUid());
                profileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(profileIntent);
            } else {
                startNewIntent(LoginActivity.class);
            }
        }
        if (item.getItemId() == R.id.action_settings) {
            startNewIntent(SettingsActivity.class);
        }
        if (item.getItemId() == R.id.action_search)
        {
            startNewIntent(SearchActivity.class);
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpEventList()
    {
        mDatabaseEvents.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                eventList = new ArrayList<Event>();
                for (DataSnapshot childSnapshot:dataSnapshot.getChildren())
                {
                    String eventId = childSnapshot.getKey().toString();
                    String eventType = childSnapshot.child("eventType").getValue().toString();
                    String description = childSnapshot.child("description").getValue().toString();
                    Object object = childSnapshot.child("startingDate").getValue();
                    HashMap result = (HashMap) object;
                    int year = Integer.parseInt(result.get("year").toString());
                    int month = Integer.parseInt(result.get("month").toString());
                    int day = Integer.parseInt(result.get("date").toString());
                    int hour = Integer.parseInt(result.get("hours").toString());
                    int minute = Integer.parseInt(result.get("minutes").toString());
                    Date startingDate = new Date(year,month,day,hour,minute,0);
                    object = childSnapshot.child("location").getValue();
                    result = (HashMap) object;
                    double latitude = Double.parseDouble(result.get("latitude").toString());
                    double longitude = Double.parseDouble(result.get("longitude").toString());
                    LatLng location = new LatLng(latitude,longitude);
                    String userId = childSnapshot.child("userId").getValue().toString();
                    String userName = childSnapshot.child("userName").getValue().toString();
                    String userPicture = childSnapshot.child("userPicture").getValue().toString();
                    String eventPicture = childSnapshot.child("eventPicture").getValue().toString();
                    if (eventType.toUpperCase().contains(searchedEventType.toUpperCase()))
                    {
                        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                        List<Address> addresses  = null;
                        String fullLocation = "";
                        try {
                            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
                            if (addresses.size() > 0) {
                                if (addresses.get(0).getCountryName() != null) {
                                    String country = addresses.get(0).getCountryName();
                                    fullLocation = country;
                                }
                                if (addresses.get(0).getAdminArea() != null) {
                                    String state = addresses.get(0).getAdminArea();
                                    fullLocation = fullLocation + ", " + state;
                                }
                                if (addresses.get(0).getLocality() != null) {
                                    String city = addresses.get(0).getLocality();
                                    fullLocation = fullLocation + ", " + city;
                                }
                                if (addresses.get(0).getPostalCode() != null) {
                                    String zip = addresses.get(0).getPostalCode();
                                    fullLocation = fullLocation + ", " + zip;
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (fullLocation.toUpperCase().contains(searchedEventLocation.toUpperCase()))
                        {
                            if (searchedEventDate != null)
                            {
                                if (searchedEventDate.getYear() == startingDate.getYear() && searchedEventDate.getMonth() == startingDate.getMonth() && searchedEventDate.getDate() == startingDate.getDate()) {
                                    Event event = new Event(eventId, eventType, description, startingDate, location, userId, userName, userPicture, eventPicture);
                                    eventList.add(event);
                                }
                            }
                            else
                            {
                                Event event = new Event(eventId, eventType,description,startingDate,location,userId, userName, userPicture, eventPicture);
                                eventList.add(event);
                            }
                        }
                    }
                }
                sortEventList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setUpRecyclerView() {
        mProgress.dismiss();
        if (eventList.size() == 0)
        {
            Toast.makeText(context, getResources().getString(R.string.noEvent), Toast.LENGTH_LONG).show();
        }
        mEventList = (RecyclerView) findViewById(R.id.eventList);
        mEventList.setHasFixedSize(true);
        mEventList.setLayoutManager(new LinearLayoutManager(this));
        EventAdapter adapter = new EventAdapter(this,eventList);
        mEventList.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mEventList != null) {
            mEventList.removeAllViewsInLayout();
            setUpEventList();
        }
    }

    private void sortEventList()
    {
        Collections.sort(eventList, new Comparator<Event>() {
            @Override
            public int compare(Event o1, Event o2) {
                return Long.compare(o2.getStartingDate().getTime(), o1.getStartingDate().getTime());
            }
        });
        setUpRecyclerView();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
