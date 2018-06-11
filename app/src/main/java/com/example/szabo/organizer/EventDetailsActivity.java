package com.example.szabo.organizer;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class EventDetailsActivity extends AppCompatActivity {

    private Event event;
    private ArrayList<User> subscribedUsers;
    private RecyclerView mRecyclerView;
    private String eventId;
    private SubscribedUsersAdapter adapter;
    private Context context;
    private LinearLayout mLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        context = this;
        Bundle extras = getIntent().getExtras();
        eventId = extras.getString("eventId");
        mLinearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        setUpEvent();
    }

    private void setUpEvent() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("events").child(eventId);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String eventType = dataSnapshot.child("eventType").getValue().toString();
                String description = dataSnapshot.child("description").getValue().toString();
                Object object = dataSnapshot.child("startingDate").getValue();
                HashMap result = (HashMap) object;
                int year = Integer.parseInt(result.get("year").toString());
                int month = Integer.parseInt(result.get("month").toString());
                int day = Integer.parseInt(result.get("date").toString());
                int hour = Integer.parseInt(result.get("hours").toString());
                int minute = Integer.parseInt(result.get("minutes").toString());
                Date startingDate = new Date(year,month,day,hour,minute,0);
                object = dataSnapshot.child("location").getValue();
                result = (HashMap) object;
                double latitude = Double.parseDouble(result.get("latitude").toString());
                double longitude = Double.parseDouble(result.get("longitude").toString());
                LatLng location = new LatLng(latitude,longitude);
                final String userId = dataSnapshot.child("userId").getValue().toString();
                mLinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent = new Intent(context, ProfileActivity.class);
                        profileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        profileIntent.putExtra("userId", userId);
                        context.startActivity(profileIntent);
                    }
                });
                String userName = dataSnapshot.child("userName").getValue().toString();
                String userPicture = dataSnapshot.child("userPicture").getValue().toString();
                String eventPicture = dataSnapshot.child("eventPicture").getValue().toString();
                subscribedUsers = new ArrayList<User>();
                if (dataSnapshot.hasChild("subscribedUsers")) {
                    mDatabase.child("subscribedUsers").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                String userId = childSnapshot.child("userId").getValue().toString();
                                String name = childSnapshot.child("name").getValue().toString();
                                String picture = childSnapshot.child("picture").getValue().toString();
                                User subscribedUser = new User(userId, name, picture);
                                if (containsUser(subscribedUsers, subscribedUser) == -1) {
                                    subscribedUsers.add(subscribedUser);
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
                event = new Event(eventId, eventType,description,startingDate,location,userId, userName, userPicture, eventPicture);
                setText();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setText()
    {
        ImageView mProfilePicture = (ImageView) findViewById(R.id.profilePicture);
        if (!EventDetailsActivity.this.isFinishing()) {
            Glide
                    .with(this)
                    .load(event.getUserPicture())
                    .into(mProfilePicture);
        }
        TextView mUserName = (TextView) findViewById(R.id.userName);
        mUserName.setText(event.getUserName());
        ImageView mEventImage = (ImageView) findViewById(R.id.eventImage);
        if (event.getEventPicture().equals("null"))
        {
            mEventImage.setVisibility(View.INVISIBLE);
        }
        else
        {
            if (!EventDetailsActivity.this.isFinishing()) {
                Glide
                        .with(context)
                        .load(event.getEventPicture())
                        .into(mEventImage);
            }
        }
        TextView mEventType = (TextView) findViewById(R.id.eventType);
        mEventType.setText(event.getEventType());
        TextView mDescription = (TextView) findViewById(R.id.description);
        mDescription.setText(event.getDescription());
        if (!EventDetailsActivity.this.isFinishing()) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.mapView);
            mapFragment.getMapAsync(new OnMapReadyCallback()
            {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions().position(event.getLocation()));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(event.getLocation(),12.0f));
                }
            });
        }
        TextView mStartingDate = (TextView) findViewById(R.id.startingDate);
        Date startingDate = event.getStartingDate();
        String displayText = String.valueOf(startingDate.getYear());
        int month = startingDate.getMonth() + 1;
        if (month/10 == 0)
        {
            displayText = displayText + ".0" + month;
        }
        else
        {
            displayText = displayText + "." + month;
        }
        if (startingDate.getDate()/10 == 0)
        {
            displayText = displayText + ".0" + startingDate.getDate();
        }
        else
        {
            displayText = displayText + "." + startingDate.getDate();
        }
        if (startingDate.getHours()/10 == 0)
        {
            displayText = displayText + ". 0" + startingDate.getHours();
        }
        else
        {
            displayText = displayText + ". " + startingDate.getHours();
        }
        if (startingDate.getMinutes()/10 == 0)
        {
            displayText = displayText + ":0" + startingDate.getMinutes();
        }
        else
        {
            displayText = displayText + ":" + startingDate.getMinutes();
        }
        mStartingDate.setText(displayText);
        setUpRecyclerView();
        subscribe();
    }

    private void setUpRecyclerView() {
        adapter = new SubscribedUsersAdapter(this,subscribedUsers);
        mRecyclerView.setAdapter(adapter);
    }

    private void subscribe()
    {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            final String userId = mAuth.getCurrentUser().getUid();
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userPicture = dataSnapshot.child("picture").getValue().toString();
                    User user = new User(userId, userName, userPicture);
                    userSubscribe(user);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else
        {
            userSubscribe(null);
        }
    }

    private void userSubscribe(final User user) {
        Button mSubscribeButton = (Button) findViewById(R.id.subscribeButton);
        if (user == null)
        {
            mSubscribeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(EventDetailsActivity.this, getResources().getString(R.string.logInToSubscribe), Toast.LENGTH_LONG).show();
                    Intent loginIntent = new Intent(EventDetailsActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            });
        }
        else if (containsUser(subscribedUsers,user) == -1)
        {
            mSubscribeButton.setText(getResources().getString(R.string.subscribe));
            mSubscribeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("events").child(event.getEventId());
                    subscribedUsers.add(user);
                    mDatabase.child("subscribedUsers").setValue(subscribedUsers);
                }
            });
        }
        else
        {
            mSubscribeButton.setText(getResources().getString(R.string.unsubscribe));
            mSubscribeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("events").child(event.getEventId());
                    int position = containsUser(subscribedUsers,user);
                    subscribedUsers.remove(position);
                    mDatabase.child("subscribedUsers").removeValue();
                    mDatabase.child("subscribedUsers").setValue(subscribedUsers);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    private int containsUser(ArrayList<User> users, User user)
    {
        for (int i=0; i<users.size(); ++i)
        {
            if (users.get(i).getUserId().equals(user.getUserId()))
            {
                return i;
            }
        }
        return -1;
    }
}
