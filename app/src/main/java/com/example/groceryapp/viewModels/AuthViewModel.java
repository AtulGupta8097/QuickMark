package com.example.groceryapp.viewModels;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.groceryapp.Models.Users;
import com.example.groceryapp.Utils;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class AuthViewModel extends ViewModel{
    private final MutableLiveData<String> verificationId = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> codeSend = new MutableLiveData<>();
    private final MutableLiveData<Boolean> validOtp = new MutableLiveData<>(null);

    public void sendOtp(String number, Activity activity){

        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {

            }


            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                codeSend.setValue(false);
//                e.printStackTrace();
//
//                // ✅ Optional: Show a toast or log
//                Log.e("PhoneAuth", "Verification failed: " + e.getMessage());
                Utils.showToast(activity, "Verification failed: " + e.getMessage());

            }

            @Override
            public void onCodeSent(@NonNull String newVerificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                codeSend.setValue(true);
                verificationId.setValue(newVerificationId);

            }
    };

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(Utils.getInstance())
                        .setPhoneNumber("+91"+number)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(activity)                 // (optional) Activity for callback binding
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public void signInWithPhoneAuthCredential(Context context, String code, Users user) {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(Objects.requireNonNull(verificationId.getValue()),code);

        Utils.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        user.setUid(Utils.getUserId());
                        FirebaseDatabase.getInstance().getReference()
                                .child("AllUsers").child("User")
                                .child(user.getUid()).setValue(user).addOnSuccessListener(unused -> {
                                    Utils.showToast(context,"Successfully saved data");
                                }).addOnFailureListener(e -> {
                                    Utils.showToast(context,"Unable to save data");
                                });
                        validOtp.postValue(true);
                    }
                    else{
                        validOtp.postValue(false);
                    }
                });
    }

    public LiveData<Boolean> isOtpSend(){

        return codeSend;
    }

    public LiveData<String> getVerificationId(){
        return verificationId;
    }
    public LiveData<Boolean> isValidOtp(){
        return validOtp;
    }
}
