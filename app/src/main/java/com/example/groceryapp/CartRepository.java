package com.example.groceryapp;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class CartRepository {
    private static CartRepository instance;
    private final MutableLiveData<Integer> numberOfCart = new MutableLiveData<>(0);

    private CartRepository() {}

    public static synchronized CartRepository getInstance() {
        if (instance == null) {
            instance = new CartRepository();
        }
        return instance;
    }

    public LiveData<Integer> getNumberOfCart() {
        return numberOfCart;
    }

    public void setNumberOfCart(int value) {
        numberOfCart.setValue(value);
    }
}

