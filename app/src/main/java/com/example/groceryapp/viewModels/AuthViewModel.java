package com.example.groceryapp.viewModels;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.groceryapp.Models.Users;
import com.example.groceryapp.utils.Utils;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public final class AuthViewModel extends ViewModel {

    private final MutableLiveData<String> verificationId = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> codeSend = new MutableLiveData<>();
    private final MutableLiveData<Boolean> validOtp = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> userExists = new MutableLiveData<>();

    private final MutableLiveData<Boolean> loginResult = new MutableLiveData<>(null);
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> passwordSetResult = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> hasPassword = new MutableLiveData<>(null);

    // Expose LiveData
    public LiveData<Boolean> isOtpSend() { return codeSend;}
    public LiveData<Boolean> isValidOtp() { return validOtp; }
    public LiveData<Boolean> getLoginResult() { return loginResult; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> getPasswordSetResult() { return passwordSetResult; }
    public LiveData<Boolean> getHasPassword() { return hasPassword; }

    // Send OTP to number
    public void sendOtp(String number, Activity activity) {
        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {}

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        codeSend.setValue(false);
                        message.setValue("Verification failed: " + e.getMessage());
                    }

                    @Override
                    public void onCodeSent(@NonNull String newVerificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        codeSend.setValue(true);
                        verificationId.setValue(newVerificationId);
                    }
                };

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(Utils.getInstance())
                .setPhoneNumber("+91" + number)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(mCallbacks)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // Sign in with OTP + check/create user record
    public void signInWithPhoneAuthCredential(String code, Users user) {
        String currentVerificationId = verificationId.getValue();
        if (currentVerificationId == null) {
            message.setValue("Verification ID is null. Please request OTP again.");
            validOtp.setValue(false);
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(currentVerificationId, code);

        Utils.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserDataIfNotExists(user);
                    } else {
                        validOtp.setValue(false);
                        message.setValue("Invalid OTP or sign-in failed.");
                    }
                })
                .addOnFailureListener(e -> {
                    validOtp.setValue(false);
                    message.setValue("Sign-in Error: " + e.getMessage());
                });
    }
    public LiveData<Boolean> getUserExistsResult() { return userExists;
    }

    public void checkIfUserExists(String phone) {
        if (phone.length() != 10) {
            message.setValue("Please enter a valid 10-digit phone number.");
            userExists.setValue(null);
            return;
        }

        FirebaseDatabase.getInstance().getReference()
                .child("AllUsers")
                .child("User")
                .child(phone)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        userExists.setValue(true);
                    } else {
                        userExists.setValue(false);
                    }
                })
                .addOnFailureListener(e -> {
                    message.setValue("Check failed: " + e.getMessage());
                    userExists.setValue(null);
                });
    }

    public void fetchAndCacheUserName(String phone, Runnable onComplete) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("AllUsers").child("User").child(phone).child("userName");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.getValue(String.class);
                    if (name != null && !name.isEmpty()) {
                        Utils.setUserName(name);
                    }
                }
                onComplete.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AuthViewModel", "Failed to fetch user name: " + error.getMessage());
                onComplete.run();
            }
        });
    }



    private void saveUserDataIfNotExists(Users user) {
        FirebaseDatabase.getInstance().getReference()
                .child("AllUsers")
                .child("User")
                .child(user.getPhoneNumber())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        message.setValue("User already exists. Login successful.");
                        Boolean passwordSet = snapshot.child("isPasswordSet").getValue(Boolean.class);
                        hasPassword.setValue(passwordSet != null && passwordSet);
                    } else {
                        user.setPasswordSet(false); // <--- THIS is setting it in the Users object
                        FirebaseDatabase.getInstance().getReference()
                                .child("AllUsers")
                                .child("User")
                                .child(user.getPhoneNumber())
                                .setValue(user)   // <--- here you push entire user object, including isPasswordSet
                                .addOnSuccessListener(unused -> {
                                    message.setValue("User data saved successfully.");
                                    hasPassword.setValue(false);
                                })
                                .addOnFailureListener(e -> {
                                    message.setValue("Unable to save user data: " + e.getMessage());
                                    hasPassword.setValue(false);
                                });
                    }
                    validOtp.setValue(true);
                })
                .addOnFailureListener(e -> {
                    validOtp.setValue(false);
                    message.setValue("Failed to check user: " + e.getMessage());
                });
    }


    // Login with phone+password check
    public void loginWithPhoneAndPassword(String phone, String password) {
        if (phone.length() != 10) {
            message.setValue("Please enter a valid 10-digit phone number.");
            loginResult.setValue(false);
            return;
        }

        FirebaseDatabase.getInstance().getReference()
                .child("AllUsers")
                .child("User")
                .child(phone)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Boolean isPasswordSet = snapshot.child("isPasswordSet").getValue(Boolean.class);
                        if (isPasswordSet == null || !isPasswordSet) {
                            message.setValue("Password not set yet. Please complete sign-up.");
                            loginResult.setValue(false);
                            return;
                        }

                        String storedPassword = snapshot.child("password").getValue(String.class);
                        if (storedPassword != null && storedPassword.equals(password)) {
                            loginResult.setValue(true);
                        } else {
                            message.setValue("Incorrect password.");
                            loginResult.setValue(false);
                        }
                    } else {
                        message.setValue("Phone number not registered.");
                        loginResult.setValue(false);
                    }
                })
                .addOnFailureListener(e -> {
                    message.setValue("Login Failed: " + e.getMessage());
                    loginResult.setValue(false);
                });
    }

    // Set password for user and mark isPasswordSet=true
    public void setPasswordForUser(String phone, String password) {
        FirebaseDatabase.getInstance().getReference()
                .child("AllUsers")
                .child("User")
                .child(phone)
                .child("password")
                .setValue(password)
                .addOnSuccessListener(unused -> {
                    FirebaseDatabase.getInstance().getReference()
                            .child("AllUsers")
                            .child("User")
                            .child(phone)
                            .child("isPasswordSet")
                            .setValue(true)
                            .addOnSuccessListener(unused2 -> {
                                passwordSetResult.setValue(true);
                            })
                            .addOnFailureListener(e -> {
                                message.setValue("Failed to update status: " + e.getMessage());
                                passwordSetResult.setValue(false);
                            });
                })
                .addOnFailureListener(e -> {
                    message.setValue("Failed to set password: " + e.getMessage());
                    passwordSetResult.setValue(false);
                });
    }
}