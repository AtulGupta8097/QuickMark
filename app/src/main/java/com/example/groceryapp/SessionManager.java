package com.example.groceryapp;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_PHONE = "phone";
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveUserPhone(String phone) {
        editor.putString(KEY_PHONE, phone);
        editor.apply();
    }

    public String getUserPhone() {
        return prefs.getString(KEY_PHONE, null);
    }

    public boolean isLoggedIn() {
        return getUserPhone() != null;
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
