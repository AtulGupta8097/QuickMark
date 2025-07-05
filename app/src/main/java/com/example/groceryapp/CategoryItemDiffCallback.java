package com.example.groceryapp;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;


public class CategoryItemDiffCallback extends DiffUtil.ItemCallback<CategoryItem> {
    @Override
    public boolean areItemsTheSame(@NonNull CategoryItem oldItem, @NonNull CategoryItem newItem) {
        // You can refine this if you have unique IDs, here I'm using title+type
        return oldItem.type == newItem.type && String.valueOf(oldItem.title).equals(String.valueOf(newItem.title));
    }

    @Override
    public boolean areContentsTheSame(@NonNull CategoryItem oldItem, @NonNull CategoryItem newItem) {
        return false;
    }
}
