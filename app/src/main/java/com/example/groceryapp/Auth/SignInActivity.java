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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.groceryapp.Activity.MainActivity;
import com.example.groceryapp.GroceryApp;
import com.example.groceryapp.R;
import com.example.groceryapp.databinding.ActivitySignInBinding;
import com.example.groceryapp.utils.Utils;
import com.example.groceryapp.viewModels.AuthViewModel;
import com.example.groceryapp.viewModels.UserViewModel;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private AuthViewModel authViewModel;
    private UserViewModel userViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.white));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setObservers();

        binding.signInBtn.setOnClickListener(v -> signInUser());
    }

    private void setObservers() {
        authViewModel.getLoginResult().observe(this, isSuccess -> {
            Utils.hideDialog();
            if (isSuccess == null) return;

            if (isSuccess) {
                String phone = Objects.requireNonNull(binding.numberEd.getText()).toString().trim();
                Utils.setUserPhoneNumber(phone);

                String fcmToken = getSharedPreferences("FCM_PREF", MODE_PRIVATE).getString("fcmToken", null);

                if (fcmToken != null && !fcmToken.isEmpty()) {
                    FirebaseDatabase.getInstance().getReference("AllUsers")
                            .child("User")
                            .child("FCMToken")
                            .child(phone)
                            .child("fcmToken")
                            .setValue(fcmToken);
                }

// Fetch & cache user name after successful login
                authViewModel.fetchAndCacheUserName(phone, () -> {

                    // Now initialize UserViewModel (after phone is available)
                    userViewModel = ((GroceryApp) getApplication()).getUserViewModel();

                    // Show fetching dialog and start cart + address recovery
                    Utils.showDialog(this, "Fetching your details...");

                    userViewModel.recoverUserDataFromFirebase(() -> {
                        Utils.hideDialog();
                        startActivity(new Intent(this, MainActivity.class));
                        finishAffinity();
                    });
                });
            }
        });

        authViewModel.getMessage().observe(this, msg -> {
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
        authViewModel.loginWithPhoneAndPassword(number, password);
    }

    private void showError(String message) {
        Log.d("SignInError", message);
        binding.errorText.setText(message);

        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        binding.errorText.startAnimation(slideIn);
        binding.errorText.setVisibility(View.VISIBLE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Animation slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
            binding.errorText.startAnimation(slideOut);
            binding.errorText.setVisibility(View.GONE);
        }, 3500);
    }
}
