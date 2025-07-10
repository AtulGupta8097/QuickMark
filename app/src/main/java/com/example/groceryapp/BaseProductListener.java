package com.example.groceryapp;

import android.content.Context;
import android.view.View;
import com.example.groceryapp.Models.Product;
import com.example.groceryapp.databinding.ProductDesignBinding;
import com.example.groceryapp.roomDatabase.CartProduct;
import com.example.groceryapp.viewModels.UserViewModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public abstract class BaseProductListener implements ProductListener {

    protected final UserViewModel userViewModel;
    protected final Context context;

    public BaseProductListener(UserViewModel userViewModel, Context context) {
        this.userViewModel = userViewModel;
        this.context = context;
    }

    @Override
    public void onAddBtnClicked(Product product, ProductDesignBinding binding) {
        Utils.vibrate(context, 40);
        binding.addProductBtn.setVisibility(View.GONE);
        binding.llAddMinusBtn.setVisibility(View.VISIBLE);
        fetchAndIncrementItemCount(product, binding, 1);
    }

    @Override
    public void onPlusBtnClicked(int item, Product product, ProductDesignBinding binding) {
        Utils.vibrate(context, 40);
        fetchAndIncrementItemCount(product, binding, item);
    }

    private void fetchAndIncrementItemCount(Product product, ProductDesignBinding binding, int increment) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("Admins").child("AdminInfo").child("userCarts")
                .child(Utils.getUserPhoneNumber())
                .child(product.getProductId()).child("itemCount");

        ref.get().addOnSuccessListener(snapshot -> {
            int currentCount = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
            int newCount = currentCount + increment;

            if (newCount <= product.getProductStock()) {
                product.setItemCount(newCount);
                binding.productNumbers.setText(String.valueOf(newCount));
                Utils.animateNumberChange(binding.productNumbers, increment > 0);

                // Update numberOfCart (for local count) and badgeCartCount (for badge live updates)

                Integer badgeCurrent = userViewModel.getBadgeCartCount().getValue();
                badgeCurrent = badgeCurrent != null ? badgeCurrent : 0;
                userViewModel.setBadgeCartCount(badgeCurrent + increment);

                saveCartProductInDB(product);
                userViewModel.updateCartProductItemCount(product.getProductId(), newCount);
            } else {
                Utils.showToast(context, "Currently we have only " + product.getProductStock() + " items available.");
            }
        });
    }

    @Override
    public void onMinusBtnClicked(int item, Product product, ProductDesignBinding binding) {
        Utils.vibrate(context, 40);
        int currentNumber = Integer.parseInt(binding.productNumbers.getText().toString());
        int cartDec = currentNumber - item;

        if (cartDec >= 0) {
            product.setItemCount(cartDec);
            binding.productNumbers.setText(String.valueOf(cartDec));
            Utils.animateNumberChange(binding.productNumbers, false);


            Integer badgeCurrent = userViewModel.getBadgeCartCount().getValue();
            badgeCurrent = badgeCurrent != null ? badgeCurrent : 0;
            userViewModel.setBadgeCartCount(Math.max(badgeCurrent - 1, 0));

            saveCartProductInDB(product);
            userViewModel.updateCartProductItemCount(product.getProductId(), cartDec);

            if (cartDec == 0) {
                binding.llAddMinusBtn.setVisibility(View.GONE);
                binding.addProductBtn.setVisibility(View.VISIBLE);
                userViewModel.deleteCartProduct(product.getProductId());
            }
        }
    }

    private void saveCartProductInDB(Product product) {
        CartProduct cartProduct = new CartProduct(
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

        if (cartProduct.getItemCount() > 1) {
            userViewModel.updateCartProduct(cartProduct);
        } else {
            userViewModel.insertCartProduct(cartProduct);
        }
    }
}
