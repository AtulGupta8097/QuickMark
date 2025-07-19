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

                    // Subtotal
                    int subtotal = calculateSubTotal(order);
                    binding.subtotalText.setText("₹" + subtotal);

                    // Delivery
                    int delivery = subtotal > 250 ? 0 : 40;
                    binding.deliveryChargeText.setText("₹" + delivery);

                    // Total
                    int total = subtotal + delivery;
                    binding.grandTotalText.setText("₹" + total);

                    // Order Details
                    binding.orderId.setText(orderId);
                    binding.deliveryAddrress.setText(order.getUserAddress());
                    binding.OrderDateAndTime.setText(order.getOrderDate());

                    // StepView and Status Message
                    int status = order.getOrderStatus();
                    handleOrderStatusDisplay(status, order);

                    // Cancel Button Visibility
                    if (status == 0) {
                        binding.cancelBtn.setVisibility(View.VISIBLE);
                        binding.cancelBtn.setText("Cancel Order");
                        binding.cancelBtn.setOnClickListener(v -> showCancelConfirmation(orderId));
                    } else if (status == -1 || status == 3) {
                        binding.cancelBtn.setVisibility(View.VISIBLE);
                        binding.cancelBtn.setText("Order Again");
                        binding.cancelBtn.setOnClickListener(v -> {
                            for (CartProduct p : order.getOrderList()) {
                                addToCart(p);
                            }
                            Toast.makeText(OrderDetailActivity.this, "Products added to cart", Toast.LENGTH_SHORT).show();
                        });
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
    private void handleOrderStatusDisplay(int status, OrdersModel order) {
        if (status == -1) {
            // Cancelled
            binding.stepView.setVisibility(View.GONE);
            binding.deliveredAndCanceledStatus.setVisibility(View.VISIBLE);
            binding.deliveredAndCanceledStatus.setText("This order was cancelled");
            binding.deliveredAndCanceledStatus.setTextColor(getResources().getColor(R.color.red));
            binding.deliveredAndCanceledStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cancel, 0, 0, 0);
        } else if (status == 3) {
            // Delivered
            binding.stepView.setVisibility(View.VISIBLE);
            binding.deliveredAndCanceledStatus.setVisibility(View.VISIBLE);
            binding.deliveredAndCanceledStatus.setText("Order delivered successfully");
            binding.deliveredAndCanceledStatus.setTextColor(getResources().getColor(R.color.black)); // or green if preferred
            binding.deliveredAndCanceledStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.done, 0, 0, 0);
            animateStepView(3);
        } else {
            // In progress
            binding.stepView.setVisibility(View.VISIBLE);
            binding.deliveredAndCanceledStatus.setVisibility(View.GONE);
            animateStepView(status);
        }
    }


    private void addToCart(CartProduct product) {
    }

    private int calculateSubTotal(OrdersModel order) {
        int total = 0;
        for (CartProduct p : order.getOrderList()) {
            total += p.getProductPrice() * p.getItemCount();
        }
        return total;
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

                if (step == status) {
                    binding.stepView.done(true);
                }
            }, (long) step * delay);
        }
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
