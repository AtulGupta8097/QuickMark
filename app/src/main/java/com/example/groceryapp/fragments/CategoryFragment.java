package com.example.groceryapp.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.example.groceryapp.BaseProductListener;
import com.example.groceryapp.Models.Product;
import com.example.groceryapp.ProductListener;
import com.example.groceryapp.R;
import com.example.groceryapp.Utils;
import com.example.groceryapp.adapter.ProductAdapter;
import com.example.groceryapp.databinding.FragmentCategoryBinding;
import com.example.groceryapp.databinding.ProductDesignBinding;
import com.example.groceryapp.roomDatabase.CartProduct;
import com.example.groceryapp.viewModels.UserViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryFragment extends Fragment {
    FragmentCategoryBinding binding;
    String title;
    ProductAdapter productAdapter;
    UserViewModel userViewModel;
    ArrayList<Product> categoryProducts = new ArrayList<>();

    public CategoryFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);

        Window window = requireActivity().getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.orange, requireContext().getTheme()));

        getBundleData();
        setTitleToToolbar();
        retrieveCategoryProduct();
        setProductAdapter();
        showCategoryProduct();
        observeCartChanges();
        onCategoryToolbarItemClicked();

        return binding.getRoot();
    }

    private void onCategoryToolbarItemClicked() {
        binding.categoryToolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void setProductAdapter() {
        productAdapter = productAdapter = new ProductAdapter(new BaseProductListener(userViewModel, requireContext()) {});

        binding.categoryRecycler.setAdapter(productAdapter);
    }

    private void showCategoryProduct() {
        userViewModel.getCategoryProduct().observe(getViewLifecycleOwner(), list -> {
            categoryProducts.clear();
            if (list != null) {
                binding.productShimmer.stopShimmer();
                binding.productShimmer.setVisibility(View.GONE);

                if (!list.isEmpty()) {
                    productAdapter.submitList(list);
                    categoryProducts.addAll(list);
                    binding.itemNotFound.setVisibility(View.GONE);
                    binding.categoryRecycler.setVisibility(View.VISIBLE);
                } else {
                    binding.categoryRecycler.setVisibility(View.GONE);
                    binding.itemNotFound.setVisibility(View.VISIBLE);
                }
            } else {
                binding.productShimmer.setVisibility(View.VISIBLE);
                binding.productShimmer.startShimmer();
                binding.categoryRecycler.setVisibility(View.GONE);
            }
        });
    }

    private void retrieveCategoryProduct() {
        userViewModel.fetchCategoryProduct(title);
    }

    private void setTitleToToolbar() {
        binding.categoryToolbar.setTitle(title);
    }

    private void getBundleData() {
        Bundle arg = getArguments();
        if (arg != null) {
            title = arg.getString("category");
        }
    }

    // 🔥🔥 Listen to cart product changes and sync with categoryProducts list
    private void observeCartChanges() {
        userViewModel.getCartProducts().observe(getViewLifecycleOwner(), cartProducts -> {
            if (categoryProducts != null && !categoryProducts.isEmpty()) {
                // Build a quick map for faster lookup
                Map<String, Integer> cartCountMap = new HashMap<>();
                for (CartProduct cartProduct : cartProducts) {
                    cartCountMap.put(cartProduct.getProductId(), cartProduct.getItemCount());
                }

                for (Product product : categoryProducts) {
                    product.setItemCount(cartCountMap.getOrDefault(product.getProductId(), 0));
                }

                productAdapter.notifyDataSetChanged();
            }
        });
    }
}
