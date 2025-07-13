package com.example.groceryapp.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.groceryapp.CategoryItem;
import com.example.groceryapp.GroceryApp;
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
        userViewModel = ((GroceryApp) requireActivity().getApplication()).getUserViewModel();

        setupAdapter();
        setupListeners();

        showShimmer();
        userViewModel.fetchAllProductsRealtime();
        observeAllProduct();

        return binding.getRoot();
    }

    private void setupListeners() {
        binding.backArrow.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        binding.searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim().toLowerCase();
                if (query.isEmpty()) {
                    binding.notFound.setVisibility(View.GONE);
                    updateCategorySections();
                } else {
                    showFilteredProducts(query);
                }
            }
        });
    }

    private void setupAdapter() {
        homeAdapter = new HomeAdapter(null, userViewModel);
        binding.searchDefaultCategoryRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.searchDefaultCategoryRecycler.setAdapter(homeAdapter);
    }

    private void observeAllProduct() {
        userViewModel.getAllProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null && !products.isEmpty()) {
                hideShimmer();

                allProducts.clear();
                allProducts.addAll(products);

                beautyProduct.clear();
                beveragesProduct.clear();

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

    private void showFilteredProducts(String query) {
        List<Product> filteredList = new ArrayList<>();

        for (Product p : allProducts) {
            String title = p.getProductTitle().toLowerCase();
            if (title.contains(query) || String.valueOf(p.getProductPrice()).equals(query)) {
                filteredList.add(p);
            }
        }

        List<CategoryItem> categoryItems = new ArrayList<>();

        if (!filteredList.isEmpty()) {
            binding.notFound.setVisibility(View.GONE);
            categoryItems.add(CategoryItem.createProductGrid(filteredList));
        } else {
            binding.notFound.setVisibility(View.VISIBLE);
        }

        homeAdapter.submitList(categoryItems);
    }

    private void showShimmer() {
        binding.shimmerLayout.setVisibility(View.VISIBLE);
        binding.shimmerLayout.startShimmer();
        binding.searchDefaultCategoryRecycler.setVisibility(View.GONE);
    }

    private void hideShimmer() {
        binding.shimmerLayout.stopShimmer();
        binding.shimmerLayout.setVisibility(View.GONE);
        binding.searchDefaultCategoryRecycler.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
