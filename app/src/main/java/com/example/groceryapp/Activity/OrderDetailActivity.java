package com.example.groceryapp.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.groceryapp.Models.OrdersModel;
import com.example.groceryapp.R;
import com.example.groceryapp.adapter.OrderProductAdapter;
import com.example.groceryapp.databinding.ActivityOrderDetailBinding;
import com.example.groceryapp.roomDatabase.CartProduct;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Arrays;

public class OrderDetailActivity extends AppCompatActivity {

    private ActivityOrderDetailBinding binding;
    private OrderProductAdapter adapter;
    private int lastAnimatedStep = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.stepView.setSteps(Arrays.asList("Ordered", "Shipped", "Out for Delivery", "Delivered"));

        adapter = new OrderProductAdapter(new ArrayList<>());
        binding.orderProductRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.orderProductRecyclerView.setAdapter(adapter);

        String orderId = getIntent().getStringExtra("orderId");
        if (orderId != null) {
            fetchOrderDetails(orderId);
        } else {
            Toast.makeText(this, "Order ID not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchOrderDetails(String orderId) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance()
                .getReference("Admins").child("AdminInfo").child("Orders").child(orderId);

        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                OrdersModel order = snapshot.getValue(OrdersModel.class);
                if (order != null) {
                    adapter.setProductList(order.getOrderList());
                    binding.totalPriceText.setText("Total: ₹" + calculateTotal(order));
                    binding.orderIdText.setText("Order ID: " + orderId);
                    binding.orderDateText.setText("Order Date: " + order.getOrderDate());

                    animateStepView(order.getOrderStatus());

                    if (order.getOrderStatus() == 0) {
                        binding.cancelBtn.setVisibility(View.VISIBLE);
                        binding.cancelBtn.setOnClickListener(v -> showCancelConfirmation(orderId));
                    } else {
                        binding.cancelBtn.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(OrderDetailActivity.this, "Order not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderDetailActivity.this, "Failed to fetch order", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void animateStepView(int status) {
        if (status == -1) {
            binding.stepView.go(0, true);
            Toast.makeText(this, "Order was cancelled", Toast.LENGTH_SHORT).show();
            return;
        }

        int delay = 400;

        for (int i = 0; i <= status && i <= 3; i++) {
            int step = i;
            binding.stepView.postDelayed(() -> {
                binding.stepView.go(step, true);

                // ✅ Tick the current step
                if (step == status) {
                    binding.stepView.done(true);
                }
            }, (long) step * delay);
        }
    }


    private int calculateTotal(OrdersModel order) {
        int total = 0;
        for (CartProduct p : order.getOrderList()) {
            total += p.getProductPrice() * p.getItemCount();
        }
        return total;
    }

    private void showCancelConfirmation(String orderId) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Order")
                .setMessage("Are you sure you want to cancel this order?")
                .setPositiveButton("Yes", (dialog, which) -> cancelOrder(orderId))
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelOrder(String orderId) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance()
                .getReference("Admins").child("AdminInfo").child("Orders").child(orderId);

        orderRef.child("orderStatus").setValue(-1)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Order cancelled", Toast.LENGTH_SHORT).show();
                    binding.stepView.go(0, true);
                    binding.cancelBtn.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to cancel order", Toast.LENGTH_SHORT).show();
                });
    }
}
