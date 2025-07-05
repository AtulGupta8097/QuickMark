package com.example.groceryapp.Auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.groceryapp.Activity.AddressActivity;
import com.example.groceryapp.R;
import com.example.groceryapp.databinding.ActivityAuthenticationBinding;

public class AuthenticationActivity extends AppCompatActivity {
    private ActivityAuthenticationBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityAuthenticationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        onSingUpBtnClicked();
        onSignInBtnClicked();
    }

    private void onSignInBtnClicked() {
        binding.signInBtn.setOnClickListener(V->{
            startActivity(new Intent(AuthenticationActivity.this, SignInActivity.class));
        });
    }

    private void onSingUpBtnClicked() {
        binding.signUpBtn.setOnClickListener(V->{
            Intent intent = new Intent(AuthenticationActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}