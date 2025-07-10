package com.example.groceryapp;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.groceryapp.viewModels.UserViewModel;

public class UserViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;

    public UserViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(UserViewModel.class)) {
            return (T) new UserViewModel(application);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
