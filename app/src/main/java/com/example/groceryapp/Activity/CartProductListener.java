package com.example.groceryapp.Activity;

import com.example.groceryapp.databinding.CartItemDesignBinding;
import com.example.groceryapp.roomDatabase.CartProduct;

public interface CartProductListener {
    void onPlusBtnClicked(int item, CartProduct cartProduct, CartItemDesignBinding cartBinding);
    void onMinusBtnClicked(int item,CartProduct cartProduct,CartItemDesignBinding cartBinding);
}
