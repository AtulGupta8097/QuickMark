package com.example.groceryapp.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.Models.CategoryModel;
import com.example.groceryapp.OnCategoryClickedListener;
import com.example.groceryapp.databinding.CategoryItemBinding;

import java.util.ArrayList;
import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder> {
    List<CategoryModel> categories;
    OnCategoryClickedListener listener;
    public CategoriesAdapter(List<CategoryModel> categories, OnCategoryClickedListener listener){
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CategoryItemBinding binding = CategoryItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryModel category = categories.get(position);
        holder.binding.categoryImage.setImageResource(category.getCategoryImage());
        holder.binding.categoryText.setText(category.getCategoryName());
        holder.itemView.setOnClickListener(v -> {
            if(listener!=null){
                listener.onCategoryClicked(category.getCategoryName());
            }
        });


    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
    public static class CategoryViewHolder extends RecyclerView.ViewHolder{
        CategoryItemBinding binding;
        public CategoryViewHolder(@NonNull CategoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
