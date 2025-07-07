package com.example.groceryapp;

import android.app.Application;

public class GroceryApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Utils class with application context
        Utils.init(this);
    }
}
