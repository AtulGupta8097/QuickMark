package com.example.groceryapp.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.groceryapp.R;
import com.example.groceryapp.databinding.ActivitySignUpBinding;
import com.example.groceryapp.utils.Utils;
import com.example.groceryapp.viewModels.AuthViewModel;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        observeViewModel();
        changeNextBtnBackground();
        onNextBtnClicked();
        onLoginTextClicked();
        onBackBtnClicked();
    }

    private void observeViewModel() {
        authViewModel.getUserExistsResult().observe(this, exists -> {
            Utils.hideDialog();
            if (exists == null) {
                showAnimatedError("Something went wrong. Try again.");
                return;
            }

            if (exists) {
                showAnimatedError("Number already used by someone");
            } else {
                Intent intent = new Intent(this, OtpActivity.class);
                intent.putExtra("number", Objects.requireNonNull(binding.numberEd.getText()).toString());
                intent.putExtra("firstName", Objects.requireNonNull(binding.firstNameEd.getText()).toString());
                intent.putExtra("lastName", Objects.requireNonNull(binding.lastNameEd.getText()).toString());
                startActivity(intent);
                finishAffinity();
            }
        });
    }

    private void onBackBtnClicked() {
        binding.backBtn.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, AuthenticationActivity.class));
            finishAffinity();
        });
    }

    private void changeNextBtnBackground() {
        binding.numberEd.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void afterTextChanged(Editable s) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 10) {
                    binding.signInBtn.setBackgroundResource(R.drawable.custom_green_btn);
                    binding.signInBtn.setTextColor(ContextCompat.getColor(SignUpActivity.this, R.color.white));
                } else {
                    binding.signInBtn.setBackgroundResource(R.drawable.custom_transparent_btn);
                    binding.signInBtn.setTextColor(ContextCompat.getColor(SignUpActivity.this, R.color.orange));
                }
            }
        });
    }

    private void onLoginTextClicked() {
        binding.signInText.setOnClickListener(v -> {
            startActivity(new Intent(this, SignInActivity.class));
        });
    }

    private void onNextBtnClicked() {
        binding.signInBtn.setOnClickListener(v -> {
            String number = Objects.requireNonNull(binding.numberEd.getText()).toString();

            if (number.length() != 10) {
                showAnimatedError("Please enter a valid 10 digit number.");
                return;
            }

            Utils.showDialog(this, "Checking number...");
            authViewModel.checkIfUserExists(number);
        });
    }

    private void showAnimatedError(String message) {
        Utils.vibrate(this,150);
        binding.errorText.setText(message);
        binding.errorText.setVisibility(View.VISIBLE);
        binding.errorText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left));

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Animation slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
            binding.errorText.startAnimation(slideOut);
            slideOut.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationEnd(Animation animation) {
                    binding.errorText.setVisibility(View.GONE);
                }
                @Override public void onAnimationRepeat(Animation animation) {}
            });
        }, 2000);
    }
}
