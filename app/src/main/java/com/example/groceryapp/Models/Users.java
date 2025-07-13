package com.example.groceryapp.Models;

public class Users {
    private String phoneNumber,userName;
    private boolean isPasswordSet;

    public Users() {}

    public Users(String phoneNumber,String userName) {
        this.phoneNumber = phoneNumber;
        this.userName = userName;
    }

    public String getPhoneNumber() { return phoneNumber;}

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public boolean isPasswordSet() { return isPasswordSet; }
    public void setPasswordSet(boolean passwordSet) { isPasswordSet = passwordSet; }
}
