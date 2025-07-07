package com.example.groceryapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.groceryapp.Auth.AuthenticationActivity;
import com.example.groceryapp.Auth.PasswordActivity;
import com.example.groceryapp.R;
import com.example.groceryapp.SessionManager;
import com.example.groceryapp.databinding.ActivitySplashBinding;
import com.google.firebase.database.FirebaseDatabase;

public class SplashActivity extends AppCompatActivity {
    ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Animation code
        binding.lottieView.setAnimation(R.raw.fruit_basket_animation);
        binding.lottieView.playAnimation();

        binding.lottieView.setAlpha(0f);
        binding.lottieView.setScaleX(0.8f);
        binding.lottieView.setScaleY(0.8f);
        binding.lottieView.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setStartDelay(400)
                .start();

        binding.appName.setTranslationY(50f);
        binding.appName.setAlpha(0f);
        binding.appName.animate()
                .translationY(0f)
                .alpha(1f)
                .setStartDelay(1000)
                .setDuration(600)
                .start();

        binding.tagline.setTranslationY(50f);
        binding.tagline.setAlpha(0f);
        binding.tagline.animate()
                .translationY(0f)
                .alpha(1f)
                .setStartDelay(1600)
                .setDuration(600)
                .start();

        // Delay before routing to next screen
        new Handler().postDelayed(this::decideNextActivity, 2000);
    }

    private void decideNextActivity() {
        SessionManager sessionManager = new SessionManager(this);
        String phone = sessionManager.getUserPhone();

        if (phone != null) {
            // Check if password is set in Firebase
            FirebaseDatabase.getInstance().getReference()
                    .child("AllUsers")
                    .child("User")
                    .child(phone)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        Boolean isPasswordSet = snapshot.child("isPasswordSet").getValue(Boolean.class);

                        if (isPasswordSet != null && isPasswordSet) {
                            // Password is set → Go to MainActivity
                            startActivity(new Intent(this, MainActivity.class));
                        } else {
                            // Password not set → Go to PasswordActivity
                            Intent intent = new Intent(this, PasswordActivity.class);
                            intent.putExtra("phone", phone);
                            startActivity(intent);
                        }
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        // If any error occurs → Go to AuthenticationActivity
                        startActivity(new Intent(this, AuthenticationActivity.class));
                        finish();
                    });
        } else {
            // No session → Go to AuthenticationActivity
            startActivity(new Intent(this, AuthenticationActivity.class));
            finish();
        }
    }
}
