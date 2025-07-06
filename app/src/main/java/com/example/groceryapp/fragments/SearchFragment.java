package com.example.groceryapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.groceryapp.CategoryItem;
import com.example.groceryapp.Models.Product;
import com.example.groceryapp.R;
import com.example.groceryapp.adapter.HomeAdapter;
import com.example.groceryapp.databinding.FragmentSearchBinding;
import com.example.groceryapp.viewModels.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private UserViewModel userViewModel;
    private final List<Product> allProducts = new ArrayList<>();
    private final List<Product> beautyProduct = new ArrayList<>();
    private final List<Product> beveragesProduct = new ArrayList<>();
    private HomeAdapter homeAdapter;

    public SearchFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Window window = requireActivity().getWindow();
        window.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.white));

        binding = FragmentSearchBinding.inflate(inflater, container, false);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        userViewModel.fetchAllProducts();
        setupAdapter();
        observeAllProduct();
        onBackArrowClicked();

        return binding.getRoot();
    }

    private void onBackArrowClicked() {
        binding.backArrow.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }

    private void setupAdapter() {
        homeAdapter = new HomeAdapter(null);
        binding.searchDefaultCategoryRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.searchDefaultCategoryRecycler.setAdapter(homeAdapter);
    }

    private void observeAllProduct() {
        userViewModel.getAllProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null && !products.isEmpty()) {
                allProducts.clear();
                allProducts.addAll(products);

                beveragesProduct.clear();
                beautyProduct.clear();

                for (Product p : allProducts) {
                    String category = p.getProductCategory().trim().toLowerCase();
                    if (category.contains("beverages")) {
                        beveragesProduct.add(p);
                    } else if (category.contains("personal care")) {
                        beautyProduct.add(p);
                    }
                }

                updateCategorySections();
            }
        });
    }

    private void updateCategorySections() {
        List<CategoryItem> categoryItems = new ArrayList<>();

        if (!beautyProduct.isEmpty()) {
            categoryItems.add(new CategoryItem(CategoryItem.TYPE_TITLE, "Beauty Products"));
            categoryItems.add(CategoryItem.createProductGrid(beautyProduct));
        }

        if (!beveragesProduct.isEmpty()) {
            categoryItems.add(new CategoryItem(CategoryItem.TYPE_TITLE, "Beverages"));
            categoryItems.add(CategoryItem.createProductGrid(beveragesProduct));
        }

        homeAdapter.submitList(categoryItems);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
