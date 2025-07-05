package com.example.groceryapp.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.groceryapp.Activity.CartProductListener;
import com.example.groceryapp.CartDiffCallback;
import com.example.groceryapp.databinding.CartItemDesignBinding;
import com.example.groceryapp.roomDatabase.CartProduct;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    CartProductListener cartProductListener;
    private final AsyncListDiffer<CartProduct> differ =
            new AsyncListDiffer<>(this, new CartDiffCallback());

    public CartAdapter(CartProductListener cartProductListener) {
        this.cartProductListener = cartProductListener;
    }

    @NonNull
    @Override
    public CartAdapter.CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CartItemDesignBinding binding = CartItemDesignBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);

        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.CartViewHolder holder, int position) {
        CartProduct item = differ.getCurrentList().get(position);
        Glide.with(holder.binding.getRoot()).load(item.getProductImageUri()).into(holder.binding.productImage);
        holder.binding.productName.setText(item.getProductTitle());
        holder.binding.quantity.setText(item.getProductQuantity());
        holder.binding.productNumbers.setText(String.valueOf(item.getItemCount()));
        holder.binding.price.setText(String.format("₹%d", item.getProductPrice()*item.getItemCount()));

        holder.binding.minusBtn.setOnClickListener(V->{
            cartProductListener.onMinusBtnClicked(1,item,holder.binding);
    });
        holder.binding.plusBtn.setOnClickListener(v -> {
            cartProductListener.onPlusBtnClicked(1,item,holder.binding);
        });

    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }
    public void submitList(List<CartProduct> list) {
        differ.submitList(list);
    }
    public static class  CartViewHolder extends RecyclerView.ViewHolder{
        CartItemDesignBinding binding;
        public CartViewHolder(@NonNull CartItemDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
