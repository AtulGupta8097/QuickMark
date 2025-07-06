package com.example.groceryapp;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.groceryapp.Models.Product;
import com.example.groceryapp.ProductListener;
import com.example.groceryapp.adapter.ProductAdapter;
import com.example.groceryapp.databinding.ActivitySeeAllCategoryBinding;
import com.example.groceryapp.viewModels.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class SeeAllCategoryActivity extends AppCompatActivity {

    private ActivitySeeAllCategoryBinding binding;
    private ProductAdapter productAdapter;
    private UserViewModel userViewModel;
    private List<Product> filteredProducts = new ArrayList<>();
    private String categoryTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivitySeeAllCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Apply edge insets padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        categoryTitle = getIntent().getStringExtra("category");

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        productAdapter = new ProductAdapter(new ProductListener() {
            @Override
            public void onAddBtnClicked(Product product, com.example.groceryapp.databinding.ProductDesignBinding binding) {}
            @Override
            public void onPlusBtnClicked(int count, Product product, com.example.groceryapp.databinding.ProductDesignBinding binding) {}
            @Override
            public void onMinusBtnClicked(int count, Product product, com.example.groceryapp.databinding.ProductDesignBinding binding) {}
        });

        binding.productRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        binding.productRecyclerView.setAdapter(productAdapter);
        userViewModel.fetchAllProducts();

        userViewModel.getAllProducts().observe(this, products -> {
            if (products != null) {
                filteredProducts.clear();
                for (Product p : products) {
                    if (p.getProductCategory().trim().equalsIgnoreCase(categoryTitle)) {
                        filteredProducts.add(p);
                    }
                }
                for(int i=0;i<filteredProducts.size();i++){
                    Log.d("item",filteredProducts.get(i).getProductTitle());
                }
                productAdapter.submitList(filteredProducts);
            }
        });

        binding.toolbarTitle.setText(categoryTitle);
    }
}
