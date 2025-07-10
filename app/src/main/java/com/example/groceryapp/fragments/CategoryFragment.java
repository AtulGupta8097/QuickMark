package com.example.groceryapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.groceryapp.BaseProductListener;
import com.example.groceryapp.GroceryApp;
import com.example.groceryapp.Models.Product;
import com.example.groceryapp.R;
import com.example.groceryapp.adapter.ProductAdapter;
import com.example.groceryapp.databinding.FragmentCategoryBinding;
import com.example.groceryapp.roomDatabase.CartProduct;
import com.example.groceryapp.viewModels.UserViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryFragment extends Fragment {

    private FragmentCategoryBinding binding;
    private String categoryTitle;
    private ProductAdapter productAdapter;
    private UserViewModel userViewModel;
    private final List<Product> categoryProducts = new ArrayList<>();

    public CategoryFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userViewModel = ((GroceryApp) requireActivity().getApplication()).getUserViewModel();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);

        Window window = requireActivity().getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.orange, requireContext().getTheme()));

        getBundleData();
        setupToolbar();
        setupProductAdapter();
        showCategoryProducts();
        listenCartUpdates();

        // If not cached — fetch
        if (!userViewModel.isCategoryCached(categoryTitle)) {
            binding.productShimmer.setVisibility(View.VISIBLE);
            binding.productShimmer.startShimmer();
            userViewModel.fetchCategoryProductRealtime(categoryTitle);
        }

        return binding.getRoot();
    }

    private void getBundleData() {
        Bundle args = getArguments();
        if (args != null) {
            categoryTitle = args.getString("category");
        }
    }

    private void setupToolbar() {
        binding.categoryToolbar.setTitle(categoryTitle);
        binding.categoryToolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void setupProductAdapter() {
        productAdapter = new ProductAdapter(new BaseProductListener(userViewModel, requireContext()) {});
        binding.categoryRecycler.setAdapter(productAdapter);
    }

    private void showCategoryProducts() {
        userViewModel.getCategoryProduct(categoryTitle).observe(getViewLifecycleOwner(), products -> {
            categoryProducts.clear();
            if (products != null) {
                binding.productShimmer.stopShimmer();
                binding.productShimmer.setVisibility(View.GONE);

                if (!products.isEmpty()) {
                    categoryProducts.addAll(products);
                    productAdapter.submitList(products);
                    binding.itemNotFound.setVisibility(View.GONE);
                    binding.categoryRecycler.setVisibility(View.VISIBLE);
                } else {
                    binding.itemNotFound.setVisibility(View.VISIBLE);
                    binding.categoryRecycler.setVisibility(View.GONE);
                }

            } else {
                binding.productShimmer.setVisibility(View.VISIBLE);
                binding.productShimmer.startShimmer();
                binding.categoryRecycler.setVisibility(View.GONE);
            }
        });
    }

    private void listenCartUpdates() {
        userViewModel.getCartProducts().observe(getViewLifecycleOwner(), cartProducts -> {
            if (!categoryProducts.isEmpty()) {
                Map<String, Integer> cartCountMap = new HashMap<>();
                for (CartProduct cartProduct : cartProducts) {
                    cartCountMap.put(cartProduct.getProductId(), cartProduct.getItemCount());
                }

                for (Product p : categoryProducts) {
                    p.setItemCount(cartCountMap.getOrDefault(p.getProductId(), 0));
                }

                productAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
