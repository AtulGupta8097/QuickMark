package com.example.groceryapp.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.groceryapp.databinding.OrderProductItemDesignBinding;
import com.example.groceryapp.roomDatabase.CartProduct;

import java.util.List;

public class OrderProductAdapter extends RecyclerView.Adapter<OrderProductAdapter.OrderProductViewHolder> {

    private final List<CartProduct> productList;

    public OrderProductAdapter(List<CartProduct> productList) {
        this.productList = productList;
    }

    public void setProductList(List<CartProduct> newProductList) {
        productList.clear();
        productList.addAll(newProductList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        OrderProductItemDesignBinding binding = OrderProductItemDesignBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new OrderProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderProductViewHolder holder, int position) {
        CartProduct product = productList.get(position);

        holder.binding.productName.setText(product.getProductTitle());
        holder.binding.productQuantity.setText(product.getProductQuantity());
        holder.binding.itemCount.setText("Qty: " + product.getItemCount());

        int totalPrice = product.getItemCount() * product.getProductPrice();
        holder.binding.totalPrice.setText("₹" + totalPrice);

        Glide.with(holder.itemView.getContext())
                .load(product.getProductImageUri())
                .into(holder.binding.productImage);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class OrderProductViewHolder extends RecyclerView.ViewHolder {
        OrderProductItemDesignBinding binding;

        public OrderProductViewHolder(@NonNull OrderProductItemDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
