package com.example.groceryapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.groceryapp.R;
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

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        getUserAddress();
        setOnBottomNavItemClicked();
        setBadgeInBottomNav();
    }

    private void getUserAddress() {
        String address = userViewModel.getUserAddressFromPref();
        if (address.equals("Not found")) {
            userViewModel.getUserAddressFromFirebase().observe(this, userAddress -> {
                if (userAddress != null && !userAddress.equals("Not found")) {
                    userViewModel.saveUserAddressInPref(userAddress);
                }
            });
        }
    }


    private void setBadgeInBottomNav() {
        userViewModel.getNumberOfCart().observe(this,count->{
            BadgeDrawable badge = binding.bottomNav.getOrCreateBadge(R.id.CartActivity);
            badge.setNumber(count);
            badge.setBackgroundColor(ContextCompat.getColor(this,R.color.red));
            badge.setBadgeTextColor(ContextCompat.getColor(this,R.color.white));
            badge.setVisible(count>0);
        });
    }

    private void setOnBottomNavItemClicked() {
//        BottomNavigationView bottomNav = binding.bottomNav;
//        NavController navController = ((NavHostFragment) Objects.requireNonNull(getSupportFragmentManager()
//                .findFragmentById(R.id.nav_host_fragment)))
//                .getNavController();
//        NavigationUI.setupWithNavController(bottomNav, navController);
//
//        Set<Integer> shopChildren = new HashSet<>(List.of(
//                R.id.CategoryFragment
//        ));
//
//        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
//            if (shopChildren.contains(destination.getId())) {
//                bottomNav.getMenu().findItem(R.id.ShopFragment).setChecked(true);
//            }
//        });

        BottomNavigationView bottomNav = binding.bottomNav;
        navController = ((NavHostFragment) Objects.requireNonNull(getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment)))
                .getNavController();

        // Setup navigation for other fragments (Shop, Account)
        NavigationUI.setupWithNavController(bottomNav, navController);

        // Manually handle Cart click
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.CartActivity) {
                // Open CartActivity
                startActivity(new Intent(this, CartActivity.class));
                return true;
            } else {
                // Let NavigationUI handle other destinations
                return NavigationUI.onNavDestinationSelected(item, navController);
            }
        });

        Set<Integer> shopChildren = new HashSet<>(List.of(
                R.id.CategoryFragment
        ));

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (shopChildren.contains(destination.getId())) {
                bottomNav.getMenu().findItem(R.id.ShopFragment).setChecked(true);
            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();

        int currentDestinationId = Objects.requireNonNull(navController.getCurrentDestination()).getId();
        MenuItem item = binding.bottomNav.getMenu().findItem(currentDestinationId);

        if (item != null) {
            item.setChecked(true);
        }
    }


}