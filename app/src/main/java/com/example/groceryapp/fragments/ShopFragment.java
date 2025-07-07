package com.example.groceryapp.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.groceryapp.Activity.AddressActivity;
import com.example.groceryapp.CategoryItem;
import com.example.groceryapp.Constants;
import com.example.groceryapp.Models.CategoryModel;
import com.example.groceryapp.R;
import com.example.groceryapp.adapter.HomeAdapter;
import com.example.groceryapp.databinding.FragmentShopBinding;
import com.example.groceryapp.viewModels.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class ShopFragment extends Fragment{
FragmentShopBinding binding;
UserViewModel userViewModel;
int currentIndex = 0;
NavController navController;

int[] lottiFiles;
List<CategoryItem> categoryItem = new ArrayList<>();
private ActivityResultLauncher<Intent> addressLauncher;

    public ShopFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lottiFiles = new int[]{
                R.raw.dilevery_animation,
                R.raw.dilevered_animation,
                R.raw.celebration_animation
        };
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        navController = NavHostFragment.findNavController(this);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentShopBinding.inflate(inflater, container, false);
        Window window = requireActivity().getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.orange, requireContext().getTheme()));

        loadUserAddressFrom();
        showCategoryRecycler();

        addressLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String selectedAddress = result.getData().getStringExtra("selected_address");

                        if (selectedAddress != null) {
                            binding.userAddress.setText(selectedAddress);
                        }
                    }
                });
        onSearchBarClicked();
        onUserAddressClicked();
        loadLottieAnimation();
        return binding.getRoot() ;
    }
    private void onSearchBarClicked() {
        binding.searchBar.setOnClickListener(V->{
            navController.navigate(R.id.action_ShopFragment_to_SearchFragment);

        });
    }

    private void loadUserAddressFrom() {
        String address = userViewModel.getCachedUserAddress();
        if (!address.isEmpty() && !address.equals("Not found") && address!=null) {
            binding.userAddress.setText(address);
        }
        else{
            binding.userAddress.setText("Tap to add address");
        }
    }



    private void loadLottieAnimation() {
        LottieAnimationView lottieView = binding.lottieView;

        // Start with the first animation
        lottieView.setAnimation(lottiFiles[currentIndex]);
        lottieView.playAnimation();

        lottieView.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                lottieView.removeAnimatorListener(this);

                // Animate the content "going out" (slide left + fade)
                lottieView.animate()
                        .alpha(0f)
                        .translationX(-50f)
                        .setDuration(200)
                        .withEndAction(() -> {
                            // Change the animation
                            currentIndex = (currentIndex + 1) % lottiFiles.length;
                            lottieView.setAnimation(lottiFiles[currentIndex]);

                            // Reset position off to right side (content feels new)
                            lottieView.setTranslationX(50f);

                            // Animate the content "coming in"
                            lottieView.animate()
                                    .alpha(1f)
                                    .translationX(0f)
                                    .setDuration(200)
                                    .withEndAction(() -> {
                                        lottieView.playAnimation(); // Play next
                                        loadLottieAnimation(); // Loop again
                                    })
                                    .start();
                        })
                        .start();
            }
        });
    }



    private void onUserAddressClicked() {
        binding.userAddress.setOnClickListener(v -> {
            int originalColor = ((TextView)v).getCurrentTextColor();
            ((TextView)v).setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_grey)); // Highlight color

            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                ((TextView)v).setTextColor(originalColor); // Revert after feedback
                Intent intent = new Intent(getActivity(), AddressActivity.class);
                addressLauncher.launch(intent);
            }).start();
        });
    }
    private void showCategoryRecycler() {
        categoryItem.clear();

        ArrayList<CategoryModel> categories = new ArrayList<>();
        for (int i = 0; i < Constants.categoryImages.length; i++) {
            categories.add(new CategoryModel(Constants.categoryImages[i], Constants.productCategories[i]));
        }

        categoryItem.add(new CategoryItem(CategoryItem.TYPE_TITLE, "Grocery & Kitchen"));
        categoryItem.add(CategoryItem.createCategoryGrid(categories));

        categoryItem.add(new CategoryItem(CategoryItem.TYPE_TITLE, "Snacks & Drinks"));
        categoryItem.add(CategoryItem.createCategoryGrid(categories));

        categoryItem.add(new CategoryItem(CategoryItem.TYPE_TITLE, "Beauty & Personal Care"));
        categoryItem.add(CategoryItem.createCategoryGrid(categories));

        categoryItem.add(new CategoryItem(CategoryItem.TYPE_TITLE, "HouseHold Essentials"));
        categoryItem.add(CategoryItem.createCategoryGrid(categories));

        HomeAdapter homeAdapter = new HomeAdapter(category -> {
            Bundle bundle = new Bundle();
            bundle.putString("category", category);
            navController.navigate(R.id.action_ShopFragment_to_CategoryFragment, bundle);
        }, userViewModel);

        binding.categoryRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.categoryRecycler.setAdapter(homeAdapter);
        homeAdapter.submitList(categoryItem);
    }



}