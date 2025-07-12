package com.example.groceryapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
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
import com.example.groceryapp.utils.Utils;
import com.example.groceryapp.databinding.ActivityMainBinding;
import com.example.groceryapp.viewModels.UserViewModel;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    UserViewModel userViewModel;
    NavController navController;
    private BadgeDrawable cartBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userViewModel = ((GroceryApp) getApplication()).getUserViewModel();

        setupBottomNav();
        getUserAddress();
        setupCartBadgeObserver();
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = binding.bottomNav;
        navController = ((NavHostFragment) Objects.requireNonNull(getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment))).getNavController();

        // Standard navigation linking
        NavigationUI.setupWithNavController(bottomNav, navController);

        // CartActivity click override
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.CartActivity) {
                startActivity(new Intent(this, CartActivity.class));
                return true;
            } else {
                return NavigationUI.onNavDestinationSelected(item, navController);
            }
        });

        // Keep ShopFragment active when on its child fragments
        Set<Integer> shopChildren = new HashSet<>(List.of(
                R.id.CategoryFragment
        ));

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (shopChildren.contains(destination.getId())) {
                bottomNav.getMenu().findItem(R.id.ShopFragment).setChecked(true);
            }
        });
    }

    private void getUserAddress() {
        String address = userViewModel.getUserAddressFromPref();
    }

    private void setupCartBadgeObserver() {
        // Initial sync with Firebase
        userViewModel.syncCartToPreferences(Utils.getUserPhoneNumber());

        // Attach badge drawable to cart menu item
        cartBadge = binding.bottomNav.getOrCreateBadge(R.id.CartActivity);
        cartBadge.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
        cartBadge.setBadgeTextColor(ContextCompat.getColor(this, R.color.white));

        // Observe LiveData and reflect instantly
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
        if (navController != null && navController.getCurrentDestination() != null) {
            int currentDestinationId = navController.getCurrentDestination().getId();
            MenuItem item = binding.bottomNav.getMenu().findItem(currentDestinationId);
            if (item != null) {
                item.setChecked(true);
            }
        }
    }
}
