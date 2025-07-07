package com.example.groceryapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.BaseProductListener;
import com.example.groceryapp.CategoryItem;
import com.example.groceryapp.Models.CategoryModel;
import com.example.groceryapp.Models.Product;
import com.example.groceryapp.OnCategoryClickedListener;
import com.example.groceryapp.R;
import com.example.groceryapp.SeeAllCategoryActivity;
import com.example.groceryapp.viewModels.UserViewModel;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<CategoryItem> items = new ArrayList<>();
    private final OnCategoryClickedListener categoryListener;
    private final UserViewModel userViewModel;

    public HomeAdapter(OnCategoryClickedListener listener, UserViewModel userViewModel) {
        this.categoryListener = listener;
        this.userViewModel = userViewModel;
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
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return (position == 0) ? 2 : 1;
                }
            });
            CategoriesAdapter adapter = new CategoriesAdapter(categories, listener);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setAdapter(adapter);
        }
    }

    // Product Grid ViewHolder — made NON-STATIC to access userViewModel directly
    class ProductGridViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;
        AppCompatButton seeAllBtn;
        ShimmerFrameLayout shimmer;

        public ProductGridViewHolder(View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.categoryRecyclerView);
            seeAllBtn = itemView.findViewById(R.id.seeAllBtn);
            shimmer = itemView.findViewById(R.id.shimmerEffect);
        }

        public void setProductData(Context context, List<Product> products) {
            // Show shimmer while loading
            shimmer.setVisibility(View.VISIBLE);
            shimmer.startShimmer();

            // Simulate loading delay — ideally you'd stop shimmer when actual data arrives from ViewModel
            recyclerView.postDelayed(() -> {
                GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 3);
                recyclerView.setLayoutManager(gridLayoutManager);

                ProductAdapter adapter = new ProductAdapter(new BaseProductListener(userViewModel, context) {});
                List<Product> displayedProducts = products.size() > 6 ? products.subList(0, 6) : products;
                adapter.submitList(displayedProducts);
                recyclerView.setAdapter(adapter);

                // Hide shimmer after data is set
                shimmer.stopShimmer();
                shimmer.setVisibility(View.GONE);

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

            }, 500); // Delay to simulate loading, or trigger this when your data is fetched
        }

    }
}
