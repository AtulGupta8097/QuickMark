package com.example.groceryapp.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.groceryapp.Activity.MainActivity;
import com.example.groceryapp.Models.Users;
import com.example.groceryapp.databinding.ActivityOtpBinding;
import com.example.groceryapp.utils.Utils;
import com.example.groceryapp.viewModels.AuthViewModel;

import java.util.Arrays;
import java.util.stream.Collectors;

public class OtpActivity extends AppCompatActivity {

    private String number,firstName,lastName;
    private AuthViewModel viewModel;
    private ActivityOtpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        getNumberAndName();
        setNumber();
        setUpOtpFields();
        setObservers();
        sendOtp();
        onVerifyBtnClicked();
    }

    private void getNumberAndName() {
        number = getIntent().getStringExtra("number");
        firstName = getIntent().getStringExtra("firstName");
        lastName = getIntent().getStringExtra("lastName");
    }

    private void setNumber() {
        binding.userPhone2.setText(String.format("+91%s", number));
    }

    private void sendOtp() {
        Utils.showDialog(this, "Sending OTP...");
        viewModel.sendOtp(number, this);
    }

    private void onVerifyBtnClicked() {
        binding.verifyBtn.setOnClickListener(v -> {
            String otp = Arrays.stream(new EditText[]{
                            binding.otpEd1, binding.otpEd2, binding.otpEd3,
                            binding.otpEd4, binding.otpEd5, binding.otpEd6
                    })
                    .map(editText -> editText.getText().toString().trim())
                    .collect(Collectors.joining());

            if (otp.length() < 6) {
                Utils.showToast(this, "Please enter 6 digit OTP");
                return;
            }

            Utils.showDialog(this, "Verifying OTP...");
            Users user = new Users(number,firstName+" "+lastName);
            viewModel.signInWithPhoneAuthCredential(otp, user);
        });
    }

    private void setObservers() {
        viewModel.isOtpSend().observe(this, code -> {
            Utils.hideDialog();
            if (code != null) {
                if (code) {
                    Utils.showToast(this, "OTP sent");
                } else {
                    Utils.showToast(this, "Failed to send OTP");
                }
            }
        });

        viewModel.isValidOtp().observe(this, validOtp -> {
            if (validOtp == null) return;

            if (!validOtp) {
                Utils.hideDialog();
                Utils.showToast(this, "Invalid OTP, try again");
            }
        });

        viewModel.getHasPassword().observe(this, hasPassword -> {
            if (hasPassword == null) return;

            Utils.hideDialog();

            // Save user phone number
            Utils.setUserPhoneNumber(number);
            Utils.setUserName(firstName+lastName);

            if (hasPassword) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                Intent intent = new Intent(this, PasswordActivity.class);
                intent.putExtra("phone", number);
                startActivity(intent);
            }
            finishAffinity();
        });

        viewModel.getMessage().observe(this, message -> {
            if (message != null) {
                Utils.showToast(this, message);
            }
        });
    }

    private void setUpOtpFields() {
        EditText[] otpEd = new EditText[]{
                binding.otpEd1, binding.otpEd2, binding.otpEd3,
                binding.otpEd4, binding.otpEd5, binding.otpEd6
        };

        for (int i = 0; i < otpEd.length; i++) {
            final int index = i;
            otpEd[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpEd.length - 1) {
                        otpEd[index + 1].requestFocus();
                    } else if (s.length() == 0 && index > 0) {
                        otpEd[index - 1].requestFocus();
                    }
                }
            });
        }
    }
}
