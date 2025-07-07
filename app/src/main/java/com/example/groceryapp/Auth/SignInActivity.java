package com.example.groceryapp.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.groceryapp.Activity.MainActivity;
import com.example.groceryapp.SessionManager;
import com.example.groceryapp.Utils;
import com.example.groceryapp.databinding.ActivitySignInBinding;
import com.example.groceryapp.viewModels.AuthViewModel;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setObservers();

        binding.signInBtn.setOnClickListener(v -> signInUser());
    }

    private void setObservers() {
        // Observe login result
        viewModel.getLoginResult().observe(this, isSuccess -> {
            Utils.hideDialog();
            if (isSuccess == null) return;

            if (isSuccess) {
                SessionManager sessionManager = new SessionManager(this);
                sessionManager.saveUserPhone(binding.numberEd.getText().toString().trim());

                startActivity(new Intent(this, MainActivity.class));
                finishAffinity();
            }
        });


        // Observe messages
        viewModel.getMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Utils.hideDialog();
                binding.errorText.setText(msg);
                binding.errorText.setVisibility(View.VISIBLE);
            }
        });
    }

    private void signInUser() {
        String number = binding.numberEd.getText().toString().trim();
        String password = binding.passwordEd.getText().toString().trim();

        binding.errorText.setVisibility(View.GONE);

        if (TextUtils.isEmpty(number) || number.length() != 10) {
            binding.errorText.setText("Please enter a valid 10-digit number");
            binding.errorText.setVisibility(View.VISIBLE);
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.errorText.setText("Please enter your password");
            binding.errorText.setVisibility(View.VISIBLE);
            return;
        }

        Utils.showDialog(this, "Signing In...");
        viewModel.loginWithPhoneAndPassword(number, password);
    }
}
