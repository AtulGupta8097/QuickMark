package com.example.groceryapp.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.groceryapp.Auth.AuthenticationActivity;
import com.example.groceryapp.Auth.PasswordActivity;
import com.example.groceryapp.R;
import com.example.groceryapp.databinding.ActivitySplashBinding;
import com.example.groceryapp.utils.Utils;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    ActivitySplashBinding binding;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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

        startAnimations();

        // Offload delay and database access to background thread
        executor.execute(() -> {
            try {
                Thread.sleep(1600); // delay for animation to finish
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Switch back to main thread to perform Firebase/database and UI operations
            mainHandler.post(this::decideNextActivity);
        });
    }

    private void startAnimations() {
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
    }

    private void decideNextActivity() {
        String phone = Utils.getUserPhoneNumber();

        if (phone != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("AllUsers")
                    .child("User")
                    .child(phone)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            Boolean isPasswordSet = snapshot.child("isPasswordSet").getValue(Boolean.class);

                            if (isPasswordSet != null && isPasswordSet) {
                                startActivity(new Intent(this, MainActivity.class));
                            } else {
                                Intent intent = new Intent(this, PasswordActivity.class);
                                intent.putExtra("phone", phone);
                                startActivity(intent);
                            }
                        } else {
                            startActivity(new Intent(this, AuthenticationActivity.class));
                        }
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        startActivity(new Intent(this, AuthenticationActivity.class));
                        finish();
                    });
        } else {
            startActivity(new Intent(this, AuthenticationActivity.class));
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown(); // Clean up
    }
}
