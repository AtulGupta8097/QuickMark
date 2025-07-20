package com.example.groceryapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.groceryapp.GroceryApp;
import com.example.groceryapp.R;
import com.example.groceryapp.databinding.ActivityMainBinding;
import com.example.groceryapp.utils.Utils;
import com.example.groceryapp.viewModels.UserViewModel;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private UserViewModel userViewModel;
    private NavController navController;
    private BadgeDrawable cartBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        handleIntentNavigation(getIntent());

        userViewModel = ((GroceryApp) getApplication()).getUserViewModel();

        setupBottomNav();
        setupCartBadgeObserver();
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = binding.bottomNav;

        // Get NavController
        navController = ((NavHostFragment) Objects.requireNonNull(
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment)))
                .getNavController();

        // Connect nav controller to bottom nav
        NavigationUI.setupWithNavController(bottomNav, navController);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.CartActivity) {
                startActivity(new Intent(this, CartActivity.class));
                return true;
            } else if (itemId == R.id.AccountActivity) {
                startActivity(new Intent(this, AccountActivity.class));
                return true;
            } else if (itemId == R.id.ShopFragment) {
                // If we're not already at ShopFragment, navigate to it
                if (navController.getCurrentDestination() == null ||
                        navController.getCurrentDestination().getId() != R.id.ShopFragment) {
                    navController.popBackStack(R.id.ShopFragment, false); // go back to ShopFragment
                }
                return true;
            } else {
                return NavigationUI.onNavDestinationSelected(item, navController);
            }
        });


        // Maintain ShopFragment checked state when inside its children
        Set<Integer> shopChildren = new HashSet<>(List.of(
                R.id.CategoryFragment
        ));

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (shopChildren.contains(destination.getId())) {
                bottomNav.getMenu().findItem(R.id.ShopFragment).setChecked(true);
            }
        });
    }

    private void setupCartBadgeObserver() {
        // Sync with Firebase on launch
        userViewModel.syncCartToPreferences(Utils.getUserPhoneNumber());

        // Attach badge drawable
        cartBadge = binding.bottomNav.getOrCreateBadge(R.id.CartActivity);
        cartBadge.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
        cartBadge.setBadgeTextColor(ContextCompat.getColor(this, R.color.white));

        // Observe LiveData badge updates
        userViewModel.getBadgeCartCount().observe(this, this::updateCartBadge);
    }

    private void updateCartBadge(int count) {
        if (cartBadge != null) {
            cartBadge.setNumber(count);
            cartBadge.setVisible(count > 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure current fragment’s bottom nav item stays checked
        if (navController != null && navController.getCurrentDestination() != null) {
            int currentDestinationId = navController.getCurrentDestination().getId();
            MenuItem item = binding.bottomNav.getMenu().findItem(currentDestinationId);
            if (item != null) {
                item.setChecked(true);
            }
        }
    }
    private void handleIntentNavigation(Intent intent) {
        if (intent != null && navController != null) {
            if(intent.getBooleanExtra("navigate_to_search", false)) {
                navController.popBackStack(R.id.ShopFragment, false);
                navController.navigate(R.id.action_ShopFragment_to_SearchFragment);
                binding.bottomNav.getMenu().findItem(R.id.ShopFragment).setChecked(true);
            }
        }
    }


    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntentNavigation(intent);
    }
}