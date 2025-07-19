package com.example.groceryapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.groceryapp.Auth.AuthenticationActivity;
import com.example.groceryapp.GroceryApp;
import com.example.groceryapp.R;
import com.example.groceryapp.databinding.ActivityAccountBinding;
import com.example.groceryapp.utils.Utils;
import com.example.groceryapp.viewModels.UserViewModel;

public class AccountActivity extends AppCompatActivity {

    private ActivityAccountBinding binding;
    private UserViewModel userViewModel;
    private String currentAddress = "";

    private final ActivityResultLauncher<Intent> addressLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String newAddress = result.getData().getStringExtra("address");
                    if (newAddress != null && !newAddress.isEmpty()) {
                        // Update local reference
                        currentAddress = newAddress;
                        binding.userAddress.setText(newAddress);

                        // Recover fresh address from Firebase and update ViewModel cache
                        userViewModel.recoverUserDataFromFirebase(() -> {
                            runOnUiThread(this::loadUserData);
                        });
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge-to-edge layout and insets handling
        EdgeToEdge.enable(this);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        binding = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Use GroceryApp instance for ViewModel (singleton scoped)
        userViewModel = new ViewModelProvider(
                (GroceryApp) getApplication(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())
        ).get(UserViewModel.class);

        loadUserData();
        setupListeners();
        setupBackButton();
        setupOnMyOrdersClicked();
    }

    private void setupOnMyOrdersClicked() {
        binding.myOrdersLayout.setOnClickListener(V->{
        startActivity(new Intent(this, OrderActivity.class));
        });
    }


    private void setupBackButton() {
        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void loadUserData() {
        String userName = Utils.getUserName();
        String userPhone = Utils.getUserPhoneNumber();

        if (!userName.isEmpty()) {
            binding.userName.setText(userName);
            binding.userNmae2.setText(userName);
        }

        if (userPhone != null) {
            binding.userPhone.setText("+91"+userPhone);
            binding.userPhone2.setText("+91"+userPhone);
        }

        currentAddress = userViewModel.getCachedUserAddress();
        if (!currentAddress.isEmpty() && !currentAddress.equals("Not found")) {
            binding.userAddress.setText(currentAddress);
        } else {
            binding.userAddress.setText("Tap to add address");
        }
    }

    private void setupListeners() {
        // Logout button
        binding.logoutButton.setOnClickListener(v -> {
            Utils.getInstance().signOut();
            startActivity(new Intent(this, AuthenticationActivity.class));
            finishAffinity();
        });

        // Both address TextView and Edit Icon trigger the same address selection animation and launch
        binding.userAddress.setOnClickListener(v -> animateAddressAndLaunch());
        binding.editAddressIcon.setOnClickListener(v -> animateAddressAndLaunch());
    }

    private void animateAddressAndLaunch() {
        binding.userAddress.animate()
                .scaleX(0.95f).scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    binding.userAddress.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(100)
                            .start();

                    Intent intent = new Intent(this, AddressActivity.class);
                    intent.putExtra("current_address", currentAddress);
                    addressLauncher.launch(intent);
                })
                .start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();  // Ensure address and name stays fresh when resuming
    }
}
