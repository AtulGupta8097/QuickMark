package com.example.groceryapp.Models;

public class Users {
    String phoneNumber,uid;
    public Users(){

    }
    public Users(String phoneNumber,String uid) {
        this.phoneNumber = phoneNumber;
        this.uid = uid;
    }
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
