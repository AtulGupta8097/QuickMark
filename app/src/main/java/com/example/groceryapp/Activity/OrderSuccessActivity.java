package com.example.groceryapp.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.groceryapp.databinding.ActivityOrderSuccessBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

public class OrderSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityOrderSuccessBinding binding = ActivityOrderSuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkAndRequestNotificationPermission();

        // ✅ Notify all admins
        notifyAdminsOfNewOrder();

        binding.viewOrdersBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, OrderActivity.class));
            finishAffinity();
        });

        binding.continueShoppingBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finishAffinity();
        });
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // ✅ Permission granted
        }
    }

    // 🟢 Step 1: Get all admin tokens and call cloud function for each
    private void notifyAdminsOfNewOrder() {
        FirebaseDatabase.getInstance().getReference("Admins").child("AdminInfo")
                .child("AdminFCM")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DataSnapshot adminSnap : snapshot.getChildren()) {
                        String token = adminSnap.child("token").getValue(String.class);
                        if (token != null && !token.isEmpty()) {
                            sendNotificationToAdmin(token);
                        }
                    }
                });
    }

    private void sendNotificationToAdmin(String token) {
        String url = "https://us-central1-grocery-app-172b6.cloudfunctions.net/sendNotification";

        JSONObject body = new JSONObject();
        try {
            body.put("token", token);
            body.put("title", "🛒 New Order Placed");
            body.put("body", "A new order has been placed by a user.");
        } catch (Exception e) {
            Toast.makeText(this, "Notification error", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> Log.d("NOTIFY_ADMIN", "Notification sent"),
                error -> Log.e("NOTIFY_ADMIN", "Failed: " + (error.getMessage() != null ? error.getMessage() : "Unknown error"))
        ) {
            @Override
            public byte[] getBody() {
                return body.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        queue.add(request);
    }
}
