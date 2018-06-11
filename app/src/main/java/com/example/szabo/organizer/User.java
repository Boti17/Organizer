package com.example.szabo.organizer;

public class User {

    private String userId;
    private String name;
    private String picture;
    private String email;
    private int registerType;

    public User(String name, String picture)
    {
        this.name = name;
        this.picture = picture;
    }

    public User(String userId, String name, String picture) {
        this.userId = userId;
        this.name = name;
        this.picture = picture;
    }

    public User(String email, String name, String picture, int registerType) {
        this.name = name;
        this.picture = picture;
        this.email = email;
        this.registerType = registerType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getRegisterType() {
        return registerType;
    }

    public void setRegisterType(int registerType) {
        this.registerType = registerType;
    }
}
