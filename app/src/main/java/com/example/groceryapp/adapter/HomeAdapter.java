package com.example.groceryapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.CategoryItem;
import com.example.groceryapp.Models.CategoryModel;
import com.example.groceryapp.Models.Product;
import com.example.groceryapp.OnCategoryClickedListener;
import com.example.groceryapp.ProductListener;
import com.example.groceryapp.R;
import com.example.groceryapp.SeeAllCategoryActivity;
import com.example.groceryapp.databinding.ProductDesignBinding;

import java.util.ArrayList;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<CategoryItem> items = new ArrayList<>();
    private final OnCategoryClickedListener categoryListener;

    public HomeAdapter(OnCategoryClickedListener listener) {
        this.categoryListener = listener;
    }

    public void submitList(List<CategoryItem> updatedItems) {
        this.items.clear();
        this.items.addAll(updatedItems);
        notifyDataSetChanged();
    }


    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == CategoryItem.TYPE_TITLE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_section_title, parent, false);
            return new TitleViewHolder(view);
        } else if (viewType == CategoryItem.TYPE_CATEGORY_GRID) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item_grid, parent, false);
            return new CategoryGridViewHolder(view);
        } else if (viewType == CategoryItem.TYPE_PRODUCT_GRID) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item_grid, parent, false);
            return new ProductGridViewHolder(view);
        }
        throw new IllegalArgumentException("Invalid view type");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CategoryItem item = items.get(position);

        if (holder instanceof TitleViewHolder) {
            ((TitleViewHolder) holder).title.setText(item.title);

        } else if (holder instanceof CategoryGridViewHolder) {
            ((CategoryGridViewHolder) holder).setCategoryData(holder.itemView.getContext(), item.categories, categoryListener);

        } else if (holder instanceof ProductGridViewHolder) {
            ((ProductGridViewHolder) holder).setProductData(holder.itemView.getContext(), item.products);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // Title ViewHolder
    static class TitleViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        public TitleViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.sectionTitle);
        }
    }

    // Category Grid ViewHolder
    static class CategoryGridViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;
        public CategoryGridViewHolder(View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.categoryRecyclerView);
        }

        public void setCategoryData(Context context, List<CategoryModel> categories, OnCategoryClickedListener listener) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 4);

            // Important: Set SpanSizeLookup
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    // First item takes 2 spans, others take 1
                    return (position == 0) ? 2 : 1;
                }
            });

            CategoriesAdapter adapter = new CategoriesAdapter(categories, listener);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setAdapter(adapter);
        }

    }

    static class ProductGridViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;
        AppCompatButton seeAllBtn;

        public ProductGridViewHolder(View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.categoryRecyclerView);
            seeAllBtn = itemView.findViewById(R.id.seeAllBtn);
        }

        public void setProductData(Context context, List<Product> products) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 3);
            recyclerView.setLayoutManager(gridLayoutManager);

            ProductAdapter adapter = new ProductAdapter(new ProductListener() {
                @Override
                public void onAddBtnClicked(Product product, ProductDesignBinding binding) {}
                @Override
                public void onPlusBtnClicked(int count, Product product, ProductDesignBinding binding) {}
                @Override
                public void onMinusBtnClicked(int count, Product product, ProductDesignBinding binding) {}
            });

            // Show only 6 items
            List<Product> displayedProducts = products.size() > 6 ? products.subList(0, 6) : products;
            adapter.submitList(displayedProducts);
            recyclerView.setAdapter(adapter);

            // Show/hide See All button
            if (products.size() > 6) {
                seeAllBtn.setVisibility(View.VISIBLE);
                seeAllBtn.setOnClickListener(v -> {
                    Intent intent = new Intent(context, SeeAllCategoryActivity.class);
                    intent.putExtra("category", products.get(0).getProductCategory());
                    context.startActivity(intent);
                });

            } else {
                seeAllBtn.setVisibility(View.GONE);
            }
        }
    }

}
