package com.example.groceryapp.Auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.groceryapp.Activity.MainActivity;
import com.example.groceryapp.databinding.ActivityPasswordBinding;
import com.example.groceryapp.utils.Utils;
import com.example.groceryapp.viewModels.AuthViewModel;

public class PasswordActivity extends AppCompatActivity {

    private ActivityPasswordBinding binding;
    private String phoneNumber;
    private AuthViewModel viewModel;
    private static final String TAG = "PasswordActivity";

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
                // Save session via Utils
                Utils.setUserPhoneNumber(phoneNumber);

                startActivity(new Intent(this, MainActivity.class));
                finishAffinity();
            } else {
                binding.passwordErrorText.setText(viewModel.getMessage().getValue());
                binding.passwordErrorText.setVisibility(View.VISIBLE);
            }
        });

        binding.createPasswordBtn.setOnClickListener(v -> validateAndSavePassword());
    }

    @SuppressLint("SetTextI18n")
    private void validateAndSavePassword() {
        String password = binding.passwordEd.getText().toString().trim();
        String confirmPassword = binding.confirmPasswordEd.getText().toString().trim();

        Log.d(TAG, "Raw Password: [" + password + "]");
        Log.d(TAG, "Raw Confirm: [" + confirmPassword + "]");

        // Normalize (remove internal/external whitespace)
        password = password.replaceAll("\\s+", "");
        confirmPassword = confirmPassword.replaceAll("\\s+", "");

        Log.d(TAG, "Normalized Password: [" + password + "]");
        Log.d(TAG, "Normalized Confirm: [" + confirmPassword + "]");

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
        // No spaces allowed, at least 8 chars, 1 capital, 1 digit, 1 special
        String passwordPattern = "^(?=\\S+$)(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";
        return password.matches(passwordPattern);
    }
}