package com.example.groceryapp.Models;

public class Users {
    private String phoneNumber;
    private boolean isPasswordSet;

    public Users() {}

    public Users(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public boolean isPasswordSet() { return isPasswordSet; }
    public void setPasswordSet(boolean passwordSet) { isPasswordSet = passwordSet; }
}
