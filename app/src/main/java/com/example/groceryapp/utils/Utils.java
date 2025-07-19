package com.example.groceryapp.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.Toast;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.groceryapp.Models.Product;
import com.example.groceryapp.databinding.ProgressDialogBinding;
import com.example.groceryapp.roomDatabase.CartProduct;
import com.google.firebase.auth.FirebaseAuth;

import java.util.UUID;

public final class Utils {

    private static AlertDialog dialog;
    private static FirebaseAuth auth;
    private static Context appContext;

    // Must be called once in Application class
    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
    public static CartProduct productToCartProduct(Product product) {
        return new CartProduct(
                product.getProductId(),
                product.getProductTitle(),
                product.getProductCategory(),
                product.getProductImageUris().get(0),
                product.getProductQuantity() + product.getUnit(),
                product.getProductPrice(),
                product.getProductStock(),
                product.getItemCount(),
                product.getBuyCount(),
                product.getRatingCount(),
                product.getAverageRating()
        );
    }

    public static void vibrate(Context context, int durationMillis) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            VibrationEffect effect = VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE);
            vibrator.vibrate(effect);
        }
    }

    public static void animateNumberChange(View view, boolean isIncrement) {
        float fromYDelta = isIncrement ? -0.35f : 0.35f;
        float toYDelta = 0f;

        // Scale animation for a slight pop effect
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1f, 1.15f, 1f, 1.15f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(120);
        scaleAnimation.setRepeatCount(1);
        scaleAnimation.setRepeatMode(ScaleAnimation.REVERSE);
        view.startAnimation(scaleAnimation);

        // Fade in during translation for smoothness
        view.setAlpha(0.8f);
        view.animate()
                .translationYBy(fromYDelta * view.getHeight())
                .alpha(1f)
                .setDuration(90)
                .withEndAction(() -> view.animate()
                        .translationY(toYDelta)
                        .setDuration(90)
                        .start())
                .start();
    }



    public static void showDialog(Context context, String message) {
        ProgressDialogBinding progress = ProgressDialogBinding.inflate(LayoutInflater.from(context));
        progress.progressMsg.setText(message);

        dialog = new AlertDialog.Builder(context).setView(progress.getRoot()).setCancelable(false).create();
        dialog.show();
    }
    public static void setUserPhoneNumber(String phoneNumber) {
        if (appContext == null) {
            throw new IllegalStateException("Utils.init() not called in Application class.");
        }
        appContext.getSharedPreferences("user_session", Context.MODE_PRIVATE)
                .edit()
                .putString("user_phone", phoneNumber)
                .apply();
    }

    // Save user name
    public static void setUserName(String name) {
        if (appContext == null) {
            throw new IllegalStateException("Utils.init() not called in Application class.");
        }
        appContext.getSharedPreferences("user_session", Context.MODE_PRIVATE)
                .edit()
                .putString("user_name", name)
                .apply();
    }

    // Retrieve user name
    public static String getUserName() {
        if (appContext == null) {
            throw new IllegalStateException("Utils.init() not called in Application class.");
        }
        return appContext.getSharedPreferences("user_session", Context.MODE_PRIVATE)
                .getString("user_name", "");
    }



    public static void hideDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    public static FirebaseAuth getInstance() {
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

    public static String getUserPhoneNumber() {
        if (appContext == null) {
            throw new IllegalStateException("Utils.init() not called in Application class.");
        }
        return appContext.getSharedPreferences("user_session", Context.MODE_PRIVATE)
                .getString("user_phone", null);
    }

    public static String getUniqueId() {
        return "ORD-" + UUID.randomUUID().toString();
    }

    public static <T> void observeOnce(LiveData<T> liveData, LifecycleOwner owner, Observer<T> observer) {
        liveData.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(T t) {
                observer.onChanged(t);
                liveData.removeObserver(this);
            }
        });
    }
}
