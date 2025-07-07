package com.example.groceryapp.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.groceryapp.R;
import com.example.groceryapp.Utils;
import com.example.groceryapp.databinding.ActivitySignUpBinding;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        changeNextBtnBackground();
        onNextBtnClicked();
        onLoginTextClicked();
        onBackBtnClicked();
    }

    private void onBackBtnClicked() {
        binding.backBtn.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, AuthenticationActivity.class));
            finishAffinity();
        });
    }

    private void changeNextBtnBackground() {
        binding.numberEd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 10) {
                    binding.nextBtn.setBackgroundResource(R.drawable.custom_green_btn);
                    binding.nextBtn.setTextColor(ContextCompat.getColor(SignUpActivity.this, R.color.white));
                } else {
                    binding.nextBtn.setBackgroundResource(R.drawable.custom_transparent_btn);
                    binding.nextBtn.setTextColor(ContextCompat.getColor(SignUpActivity.this, R.color.orange));
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void onLoginTextClicked() {
        binding.loginText.setOnClickListener(v -> {
            startActivity(new Intent(this, SignInActivity.class));
        });
    }

    private void onNextBtnClicked() {
        binding.nextBtn.setOnClickListener(v -> {
            String number = binding.numberEd.getText().toString();

            if (number.length() != 10) {
                Utils.showToast(this, "Please enter a valid 10 digit number");
            } else {
                Intent intent = new Intent(this, OtpActivity.class);
                intent.putExtra("number", number);
                startActivity(intent);
            }
        });
    }
}
