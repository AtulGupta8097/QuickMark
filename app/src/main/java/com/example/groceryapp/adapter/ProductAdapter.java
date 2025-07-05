package com.example.groceryapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.groceryapp.Models.Product;
import com.example.groceryapp.ProductDiffCallback;
import com.example.groceryapp.ProductListener;
import com.example.groceryapp.databinding.ProductDesignBinding;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final AsyncListDiffer<Product> differ;
    private final ProductListener productListener;

    public ProductAdapter(ProductListener productListener) {
        this.productListener = productListener;
        differ = new AsyncListDiffer<>(this, new ProductDiffCallback());
    }

    public void submitList(List<Product> newList) {
        differ.submitList(newList);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ProductDesignBinding binding = ProductDesignBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product p = differ.getCurrentList().get(position);

        ArrayList<String> tempImage = p.getProductImageUris();
        ArrayList<SlideModel> imageList = new ArrayList<>();
        for (String imageUri : tempImage) {
            imageList.add(new SlideModel(imageUri, ScaleTypes.CENTER_CROP));
        }

        if (p.getItemCount() <= 0) {
            holder.binding.llAddMinusBtn.setVisibility(View.GONE);
            holder.binding.addProductBtn.setVisibility(View.VISIBLE);
        } else {
            holder.binding.addProductBtn.setVisibility(View.GONE);
            holder.binding.llAddMinusBtn.setVisibility(View.VISIBLE);
            holder.binding.productNumbers.setText(String.valueOf(p.getItemCount()));
        }

        String quantity = p.getProductQuantity() + " " + p.getUnit();
        holder.binding.imageSlider.setImageList(imageList, ScaleTypes.CENTER_CROP);
        holder.binding.productName.setText(p.getProductTitle());
        holder.binding.productPrice.setText("₹ " + p.getProductPrice());
        holder.binding.productQuantity.setText(quantity);

        holder.binding.addProductBtn.setOnClickListener(v -> productListener.onAddBtnClicked(p, holder.binding));
        holder.binding.plusBtn.setOnClickListener(v -> productListener.onPlusBtnClicked(1, p, holder.binding));
        holder.binding.minusBtn.setOnClickListener(v -> productListener.onMinusBtnClicked(1, p, holder.binding));
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ProductDesignBinding binding;
        public ProductViewHolder(@NonNull ProductDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
