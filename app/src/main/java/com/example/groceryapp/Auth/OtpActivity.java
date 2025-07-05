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
import com.example.groceryapp.R;
import com.example.groceryapp.Utils;
import com.example.groceryapp.databinding.ActivityOtpBinding;
import com.example.groceryapp.viewModels.AuthViewModel;
import com.example.groceryapp.viewModels.UserViewModel;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class OtpActivity extends AppCompatActivity {
    String number,verificationId;
    AuthViewModel viewModel;
private ActivityOtpBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setUpOtpFields();
        getNumber();
        setNumber();
        sendOtp();
        onVerifyBtnClicked();
    }

    private void setNumber() {
        binding.phoneNumber.setText(String.format("+91%s", number));
    }

    private void sendOtp(){
        Utils.showDialog(this,"OTP Sending....");
        viewModel.sendOtp(number,this);
        viewModel.isOtpSend().observe(this,code->{
             if(code){
                 Utils.hideDialog();
                 Utils.showToast(OtpActivity.this,"OTP send");

                 viewModel.getVerificationId().observe(this,it->{
                     verificationId = it;
                 });
             }
             else{
                 Utils.hideDialog();
                 Utils.showToast(OtpActivity.this,"OTP failed");
             }
        });
    }

    private void getNumber() {
        number = getIntent().getStringExtra("number");
    }

    private void onVerifyBtnClicked() {
        binding.verifyBtn.setOnClickListener(V->{
            Utils.showDialog(this,"Please wait....");
            EditText[] otpEd = new EditText[]{
                    binding.otpEd1,
                    binding.otpEd2,
                    binding.otpEd3,
                    binding.otpEd4,
                    binding.otpEd5,
                    binding.otpEd6
            };

            String otp = Arrays.stream(otpEd)
                    .map(editText -> editText.getText().toString().trim())
                    .collect(Collectors.joining());
            if(otp.length()<6){
                Utils.hideDialog();
                Utils.showToast(this,"Please enter 6 digit OTP");
            }
            else{
                Utils.hideDialog();
                Users user = new Users(number,null);
                Utils.showDialog(this,"Verifying OTP...");
                viewModel.signInWithPhoneAuthCredential(this,otp,user);
                viewModel.isValidOtp().observe(this,validOtp->{
                    if(validOtp!=null) {
                        Utils.hideDialog();
                        if (validOtp) {
                            Utils.hideDialog();
                            startActivity(new Intent(this, MainActivity.class));
                            finishAffinity();
                        }
                    }
                });
            }
        });

    }

    private void setUpOtpFields() {
        EditText[] otpEd = new EditText[]{
                binding.otpEd1,
                binding.otpEd2,
                binding.otpEd3,
                binding.otpEd4,
                binding.otpEd5,
                binding.otpEd6
        };
        for(int i=0;i<otpEd.length;i++){
            final int index = i;
            otpEd[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(s.length()==1 && index<otpEd.length-1){
                        otpEd[index+1].requestFocus();
                    }
                    else if(s.length()==0 && index>0){
                        otpEd[index-1].requestFocus();
                    }

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }

    }
}