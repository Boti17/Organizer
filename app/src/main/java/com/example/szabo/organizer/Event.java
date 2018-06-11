package com.example.szabo.organizer;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;

public class Event {
    private String eventId;
    private String eventType;
    private String description;
    private Date startingDate;
    private LatLng location;
    private String userId;
    private String userName;
    private String userPicture;
    private ArrayList<User> subscribedUsers;
    private String eventPicture;

    public Event(){
        this.subscribedUsers = new ArrayList<User>();
    }

    public Event(String eventType, String description, Date startingDate, LatLng location, String userId, String userName, String userPicture, String eventPicture) {
        this.eventType = eventType;
        this.description = description;
        this.startingDate = startingDate;
        this.location = location;
        this.userId = userId;
        this.userName = userName;
        this.userPicture = userPicture;
        this.subscribedUsers = new ArrayList<User>();
        this.eventPicture = eventPicture;
    }

    public Event(String eventId, String eventType, String description, Date startingDate, LatLng location, String userId, String userName, String userPicture, String eventPicture) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.description = description;
        this.startingDate = startingDate;
        this.location = location;
        this.userId = userId;
        this.userName = userName;
        this.userPicture = userPicture;
        this.subscribedUsers = new ArrayList<User>();
        this.eventPicture = eventPicture;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(Date startingDate) {
        this.startingDate = startingDate;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPicture() {
        return userPicture;
    }

    public void setUserPicture(String userPicture) {
        this.userPicture = userPicture;
    }

    public ArrayList<User> getSubscribedUsers() {
        return subscribedUsers;
    }

    public void setSubscribedUsers(ArrayList<User> subscribedUsers) {
        this.subscribedUsers = subscribedUsers;
    }

    public String getEventPicture() {
        return eventPicture;
    }

    public void setEventPicture(String eventPicture) {
        this.eventPicture = eventPicture;
    }
}
