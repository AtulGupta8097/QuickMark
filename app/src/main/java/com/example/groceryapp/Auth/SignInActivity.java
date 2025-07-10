package com.example.groceryapp.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
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
import com.example.groceryapp.SessionManager;
import com.example.groceryapp.Utils;
import com.example.groceryapp.databinding.ActivitySignInBinding;
import com.example.groceryapp.viewModels.AuthViewModel;

import java.util.Objects;

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
                Utils.setUserPhoneNumber(Objects.requireNonNull(binding.numberEd.getText()).toString().trim());
                startActivity(new Intent(this, MainActivity.class));
                finishAffinity();
            }
        });

        // Observe messages
        viewModel.getMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Utils.hideDialog();
                showError(msg);
            }
        });
    }

    private void signInUser() {
        String number = Objects.requireNonNull(binding.numberEd.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.passwordEd.getText()).toString().trim();

        if (TextUtils.isEmpty(number) || number.length() != 10) {
            showError("Please enter a valid 10-digit number");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            showError("Please enter your password");
            return;
        }

        Utils.showDialog(this, "Signing In...");
        viewModel.loginWithPhoneAndPassword(number, password);
    }

    private void showError(String message) {
        Log.d("msg",message);
        binding.errorText.setText(message);

        // Load slide in animation
        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        binding.errorText.startAnimation(slideIn);
        binding.errorText.setVisibility(View.VISIBLE);

        // Auto-hide after 2.5 seconds with slide out animation
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Animation slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
            binding.errorText.startAnimation(slideOut);
            binding.errorText.setVisibility(View.GONE);
        }, 3500);
    }
}
