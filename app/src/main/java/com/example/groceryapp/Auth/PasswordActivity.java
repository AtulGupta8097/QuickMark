package com.example.groceryapp.Auth;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.groceryapp.databinding.ActivityPasswordBinding;
import com.example.groceryapp.viewModels.AuthViewModel;

public class PasswordActivity extends AppCompatActivity {

    private ActivityPasswordBinding binding;
    private String phoneNumber;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Init ViewModel
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        phoneNumber = getIntent().getStringExtra("phone");

        // Observe password result
        viewModel.getPasswordSetResult().observe(this, isSuccess -> {
            Utils.hideDialog();
            if (isSuccess == null) return;

            if (isSuccess) {
                // Save session
                SessionManager sessionManager = new SessionManager(this);
                sessionManager.saveUserPhone(phoneNumber);

                startActivity(new Intent(this, MainActivity.class));
                finishAffinity();
            } else {
                binding.passwordErrorText.setText(viewModel.getMessage().getValue());
                binding.passwordErrorText.setVisibility(View.VISIBLE);
            }
        });

        binding.createPasswordBtn.setOnClickListener(v -> validateAndSavePassword());
    }

    private void validateAndSavePassword() {
        String password = binding.passwordEd.getText().toString().trim();
        String confirmPassword = binding.confirmPasswordEd.getText().toString().trim();

        binding.passwordErrorText.setVisibility(View.GONE);
        binding.confirmPasswordErrorText.setVisibility(View.GONE);

        boolean hasError = false;

        if (password.isEmpty()) {
            binding.passwordErrorText.setText("Please enter a password");
            binding.passwordErrorText.setVisibility(View.VISIBLE);
            hasError = true;
        } else if (!isValidPassword(password)) {
            binding.passwordErrorText.setText("Password must be at least 8 characters with 1 capital letter, 1 number, and 1 special character.");
            binding.passwordErrorText.setVisibility(View.VISIBLE);
            hasError = true;
        }

        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordErrorText.setText("Please confirm your password");
            binding.confirmPasswordErrorText.setVisibility(View.VISIBLE);
            hasError = true;
        } else if (!confirmPassword.equals(password)) {
            binding.confirmPasswordErrorText.setText("Passwords do not match");
            binding.confirmPasswordErrorText.setVisibility(View.VISIBLE);
            hasError = true;
        }

        if (hasError) return;

        Utils.showDialog(this, "Saving Password...");
        viewModel.setPasswordForUser(phoneNumber, password);
    }

    private boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";
        return password.matches(passwordPattern);
    }
}
