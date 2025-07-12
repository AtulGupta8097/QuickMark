package com.example.groceryapp.Auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.groceryapp.Activity.MainActivity;
import com.example.groceryapp.R;
import com.example.groceryapp.databinding.ActivityPasswordBinding;
import com.example.groceryapp.utils.Utils;
import com.example.groceryapp.viewModels.AuthViewModel;

import java.util.Objects;

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

        viewModel.getPasswordSetResult().observe(this, isSuccess -> {
            Utils.hideDialog();
            if (isSuccess == null) return;

            if (isSuccess) {
                Utils.setUserPhoneNumber(phoneNumber);
                startActivity(new Intent(this, MainActivity.class));
                finishAffinity();
            } else {
                String message = viewModel.getMessage().getValue();
                if (message != null && !message.isEmpty()) {
                    binding.passwordInputLayout.setError(message);
                    animateField(binding.passwordInputLayout);
                }
            }
        });


        binding.createPasswordBtn.setOnClickListener(v -> validateAndSavePassword());
    }

    @SuppressLint("SetTextI18n")
    private void validateAndSavePassword() {
        String password = Objects.requireNonNull(binding.passwordEd.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(binding.confirmPasswordEd.getText()).toString().trim();

        password = password.replaceAll("\\s+", "");
        confirmPassword = confirmPassword.replaceAll("\\s+", "");

        // clear previous errors
        binding.passwordInputLayout.setError(null);
        binding.confirmPasswordInputLayout.setError(null);

        boolean hasError = false;

        if (password.isEmpty()) {
            binding.passwordInputLayout.setError("Please enter a password");
            animateField(binding.passwordInputLayout);
            hasError = true;
        } else if (!isValidPassword(password)) {
            binding.passwordInputLayout.setError("At least 8 characters, 1 capital, 1 number, 1 special");
            animateField(binding.passwordInputLayout);
            hasError = true;
        }

        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordInputLayout.setError("Please confirm your password");
            animateField(binding.confirmPasswordInputLayout);
            hasError = true;
        } else if (!confirmPassword.equals(password)) {
            binding.confirmPasswordInputLayout.setError("Passwords do not match");
            animateField(binding.confirmPasswordInputLayout);
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
    private void animateField(View view) {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        view.startAnimation(shake);
    }

}