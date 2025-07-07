package com.example.groceryapp;

import android.os.Bundle;

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

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.fetchAllProducts();
        getIntentContent();
        setProductAdapter();
        observeProduct();
        setHeadingText();


    }

    private void setHeadingText() {
        binding.toolbarTitle.setText(categoryTitle);
    }

    private void observeProduct() {

        userViewModel.getAllProducts().observe(this, products -> {
            if (products != null) {
                filteredProducts.clear();
                for (Product p : products) {
                    if (p.getProductCategory().trim().equalsIgnoreCase(categoryTitle)) {
                        filteredProducts.add(p);
                    }
                }
                productAdapter.submitList(filteredProducts);
            }
        });

    }

    private void setProductAdapter() {
        productAdapter = new ProductAdapter(new BaseProductListener(userViewModel, this) {});
        binding.productRecyclerView.setAdapter(productAdapter);
    }

    private void getIntentContent() {
        categoryTitle = getIntent().getStringExtra("category");
    }
}
