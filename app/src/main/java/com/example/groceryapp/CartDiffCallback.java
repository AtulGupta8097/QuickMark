package com.example.groceryapp;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.example.groceryapp.roomDatabase.CartProduct;

public class CartDiffCallback extends DiffUtil.ItemCallback<CartProduct> {
    @Override
    public boolean areItemsTheSame(@NonNull CartProduct oldItem, @NonNull CartProduct newItem) {
        return oldItem.getProductId().equals(newItem.getProductId());
    }

    @Override
    public boolean areContentsTheSame(@NonNull CartProduct oldItem, @NonNull CartProduct newItem) {
        return oldItem.getItemCount()==newItem.getItemCount();
    }
}
