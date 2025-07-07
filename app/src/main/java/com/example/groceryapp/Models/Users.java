package com.example.groceryapp.Models;

public class Users {
    private String uid;
    private String phoneNumber;
    private boolean isPasswordSet;


    public Users() {}

    public Users(String phoneNumber, String uid) {
        this.phoneNumber = phoneNumber;
        this.uid = uid;
    }

    public Users(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public boolean isPasswordSet() { return isPasswordSet; }
    public void setPasswordSet(boolean passwordSet) { isPasswordSet = passwordSet; }
}
