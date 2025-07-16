package com.example.groceryapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.Models.OrdersModel;
import com.example.groceryapp.R;
import com.example.groceryapp.databinding.OrderItemDesignBinding;
import com.example.groceryapp.roomDatabase.CartProduct;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final Context context;
    private final OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderAgain(OrdersModel order);
    }

    public OrderAdapter(Context context, OnOrderClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    private final DiffUtil.ItemCallback<OrdersModel> diffCallback = new DiffUtil.ItemCallback<OrdersModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull OrdersModel oldItem, @NonNull OrdersModel newItem) {
            return oldItem.getOrderId().equals(newItem.getOrderId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull OrdersModel oldItem, @NonNull OrdersModel newItem) {
            return oldItem.getOrderStatus() == newItem.getOrderStatus()
                    && oldItem.getOrderDate().equals(newItem.getOrderDate())
                    && oldItem.getOrderId().equals(newItem.getOrderId()); // add more fields if needed
        }

    };

    private final AsyncListDiffer<OrdersModel> differ = new AsyncListDiffer<>(this, diffCallback);

    public void submitList(List<OrdersModel> list) {
        differ.submitList(list);
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        OrderItemDesignBinding binding = OrderItemDesignBinding.inflate(inflater, parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(differ.getCurrentList().get(position));
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {

        private final OrderItemDesignBinding binding;

        public OrderViewHolder(OrderItemDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("SetTextI18n")
        public void bind(OrdersModel order) {
            binding.orderStatusTv.setText(getStatusText(order.getOrderStatus()));
            binding.orderStatusTv.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    ContextCompat.getDrawable(context, getStatusIcon(order.getOrderStatus())),
                    null
            );

            binding.placedDateTv.setText("Placed at " + order.getOrderDate());
            binding.totalPriceTv.setText("₹" + getTotalPrice(order));
            binding.orderAgainTv.setOnClickListener(v -> listener.onOrderAgain(order));

            // Setup nested image recycler view
            List<String> imageUrls = new ArrayList<>();
            for (int i = 0; i < Math.min(order.getOrderList().size(), 4); i++) {
                imageUrls.add(order.getOrderList().get(i).getProductImageUri());
            }

            OrderImageAdapter imageAdapter = new OrderImageAdapter(context, imageUrls);
            binding.imageRecyclerView.setAdapter(imageAdapter);
        }

        private int getTotalPrice(OrdersModel order) {
            int total = 0;
            for (CartProduct item : order.getOrderList()) {
                total += item.getProductPrice() * item.getItemCount();
            }
            return total;
        }

        private String getStatusText(int status) {
            switch (status) {
                case 0: return "Order Placed";
                case 1: return "Order Shipped";
                case 2: return "Out for Delivery";
                case 3: return "Order Delivered";
                case 4: return "Order Cancelled";
                default: return "Unknown Status";
            }
        }

        private int getStatusIcon(int status) {
            switch (status) {
                case 0: return R.drawable.ic_pending;
                case 1: return R.drawable.ic_shipped;
                case 2: return R.drawable.ic_delivery;
                case 3: return R.drawable.done;
                case 4: return R.drawable.ic_cancel;
                default: return R.drawable.ic_pending;
            }
        }
    }
}
