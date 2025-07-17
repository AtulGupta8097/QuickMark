package com.example.groceryapp.Activity;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.groceryapp.GroceryApp;
import com.example.groceryapp.adapter.OrderAdapter;
import com.example.groceryapp.databinding.ActivityOrderBinding;
import com.example.groceryapp.viewModels.UserViewModel;

public class OrderActivity extends AppCompatActivity {

    private ActivityOrderBinding binding;
    private OrderAdapter adapter;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userViewModel = new ViewModelProvider(
                (GroceryApp) getApplication(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())
        ).get(UserViewModel.class);
        userViewModel.fetchOrdersFromFirebase();

        fetchOrders();
        setupRecycler();
        onBackBtnClicked();
    }

    private void onBackBtnClicked() {
        binding.backBtn.setOnClickListener(V->{
            finish();
        });
    }

    private void setupRecycler() {
        adapter = new OrderAdapter(this, order -> {

        });
        binding.orderRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.orderRecyclerView.setAdapter(adapter);
    }

    private void fetchOrders() {
        userViewModel.getUserOrdersLiveData().observe(this, orders -> {
            if (orders != null) {
                adapter.submitList(orders);
            }
        });
    }
}
