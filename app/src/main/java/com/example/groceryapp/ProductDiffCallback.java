package com.example.groceryapp;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.example.groceryapp.Models.Product;

public class ProductDiffCallback extends DiffUtil.ItemCallback<Product> {
    @Override
    public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
        return oldItem.getProductId().equals(newItem.getProductId());
    }

    @Override
    public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
        return oldItem.equals(newItem);
    }
}
