package com.example.groceryapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.groceryapp.databinding.ImageItemDesignBinding;

import java.util.List;

public class OrderImageAdapter extends RecyclerView.Adapter<OrderImageAdapter.ImageViewHolder> {

    private final Context context;
    private final List<String> imageUrls;

    public OrderImageAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageItemDesignBinding binding = ImageItemDesignBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ImageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Glide.with(context)
                .load(imageUrls.get(position))
                .centerCrop()
                .into(holder.binding.imageView);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageItemDesignBinding binding;

        public ImageViewHolder(ImageItemDesignBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
