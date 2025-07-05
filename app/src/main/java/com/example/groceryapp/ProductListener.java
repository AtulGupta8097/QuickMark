package com.example.groceryapp;

import com.example.groceryapp.Models.Product;
import com.example.groceryapp.databinding.ProductDesignBinding;

public interface ProductListener {
    void onAddBtnClicked(Product product, ProductDesignBinding binding);
    void onPlusBtnClicked(int item,Product product,ProductDesignBinding binding);
    void onMinusBtnClicked(int item,Product product,ProductDesignBinding binding);
}
