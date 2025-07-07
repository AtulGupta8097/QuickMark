package com.example.groceryapp;

import android.content.Context;
import android.view.View;
import com.example.groceryapp.Models.Product;
import com.example.groceryapp.databinding.ProductDesignBinding;
import com.example.groceryapp.roomDatabase.CartProduct;
import com.example.groceryapp.viewModels.UserViewModel;

public abstract class BaseProductListener implements ProductListener {

    protected final UserViewModel userViewModel;
    protected final Context context;

    public BaseProductListener(UserViewModel userViewModel, Context context) {
        this.userViewModel = userViewModel;
        this.context = context;
    }

    @Override
    public void onAddBtnClicked(Product product, ProductDesignBinding binding) {
        binding.addProductBtn.setVisibility(View.GONE);
        binding.llAddMinusBtn.setVisibility(View.VISIBLE);
        onPlusBtnClicked(1, product, binding);
    }

    @Override
    public void onPlusBtnClicked(int item, Product product, ProductDesignBinding binding) {
        int currentNumber = Integer.parseInt(binding.productNumbers.getText().toString());
        int cartInc = currentNumber + item;

        if (cartInc <= product.getProductStock()) {
            product.setItemCount(cartInc);
            binding.productNumbers.setText(String.valueOf(cartInc));
            userViewModel.setNumberOfCart(1);
            saveCartProductInDB(product);
            userViewModel.updateCartProductItemCount(product.getProductId(), cartInc);
        } else {
            Utils.showToast(context, "Currently we have only " + product.getProductStock() + " items available.");
        }
    }

    @Override
    public void onMinusBtnClicked(int item, Product product, ProductDesignBinding binding) {
        int currentNumber = Integer.parseInt(binding.productNumbers.getText().toString());
        int cartDec = currentNumber - item;
        product.setItemCount(cartDec);
        binding.productNumbers.setText(String.valueOf(cartDec));
        userViewModel.setNumberOfCart(-1);
        saveCartProductInDB(product);
        userViewModel.updateCartProductItemCount(product.getProductId(), cartDec);

        if (cartDec == 0) {
            binding.llAddMinusBtn.setVisibility(View.GONE);
            binding.addProductBtn.setVisibility(View.VISIBLE);
            userViewModel.deleteCartProduct(product.getProductId());
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
