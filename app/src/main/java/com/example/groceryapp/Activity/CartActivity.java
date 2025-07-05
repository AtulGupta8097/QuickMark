package com.example.groceryapp.Activity;

import static java.lang.String.valueOf;

import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.groceryapp.Models.OrdersModel;
import com.example.groceryapp.R;
import com.example.groceryapp.Utils;
import com.example.groceryapp.adapter.CartAdapter;
import com.example.groceryapp.databinding.ActivityCartBinding;
import com.example.groceryapp.databinding.CartItemDesignBinding;
import com.example.groceryapp.roomDatabase.CartProduct;
import com.example.groceryapp.viewModels.UserViewModel;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements PaymentResultListener {
    ActivityCartBinding binding;
    UserViewModel userViewModel;
    CartAdapter cartAdapter;
    int toPay = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        Window window = this.getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.white, getApplicationContext().getTheme()));

        setCartRecycler();
        getCartProducts();
        onBackBtnClicked();
        onPayBtnClicked();

    }

    private void onBackBtnClicked() {
        binding.backBtn.setOnClickListener(v -> {
            finish();
        });
    }

    private void onPayBtnClicked() {
        binding.payBtn.setOnClickListener(V->{
            String address = userViewModel.getCachedUserAddress();
            if (address == null || address.isEmpty() || address.equals("Not found")) {
                Utils.showToast(this,"Address not selected");
            }
            else {
                Checkout checkout = new Checkout();
                checkout.setKeyID("rzp_test_y8ZHMFKiAeVWpw");

                try {
                    JSONObject options = getJsonObject();
                    checkout.open(this, options);
                } catch(Exception e) {
                    Log.e("Payment Error", "Error in starting Razorpay Checkout", e);
                }
            }
        });

    }

    @NonNull
    private JSONObject getJsonObject() throws JSONException {
        JSONObject options = new JSONObject();
        options.put("name", "QuickMart");
        options.put("description", "Test Transaction");
        options.put("currency", "INR");
        options.put("amount", toPay*100); // amount in paise (₹500.00)

        JSONObject preFill = new JSONObject();
        preFill.put("email", "atulgupta8070@gmail.com");
        preFill.put("contact", "+917718094564");
        options.put("prefill", preFill);
        return options;
    }

    private void getCartProducts() {
        userViewModel.getCartProducts().observe(this, cartProductList -> {
            cartAdapter.submitList(cartProductList);
            updateCartTotal(cartProductList);
        });
    }

    private void setCartRecycler() {
        cartAdapter = new CartAdapter(new CartProductListener() {
            @Override
            public void onPlusBtnClicked(int item, CartProduct cartProduct, CartItemDesignBinding cartBinding) {
                int currentNumber = Integer.parseInt(cartBinding.productNumbers.getText().toString());
                int cartInc = currentNumber + item;

                if (cartInc <= cartProduct.getProductStock()) {
                    CartProduct updatedProduct = new CartProduct(
                            cartProduct.getProductId(),
                            cartProduct.getProductTitle(),
                            cartProduct.getProductCategory(),
                            cartProduct.getProductImageUri(),
                            cartProduct.getProductQuantity(),
                            cartProduct.getProductPrice(),
                            cartProduct.getProductStock(),
                            cartInc,
                            cartProduct.getBuyCount(),
                            cartProduct.getRatingCount(),
                            cartProduct.getAverageRating()
                    );
                    cartBinding.productNumbers.setText(valueOf(cartInc));
                    userViewModel.setNumberOfCart(1);
                    saveCartProductInDB(updatedProduct);
                    userViewModel.updateCartProductItemCount(cartProduct.getProductId(), cartInc);
                } else {
                    Utils.showToast(CartActivity.this, "Currently we have only " + currentNumber + " item(s) available");
                }
            }

            @Override
            public void onMinusBtnClicked(int item, CartProduct cartProduct, CartItemDesignBinding cartBinding) {
                int currentNumber = Integer.parseInt(cartBinding.productNumbers.getText().toString());
                int cartDec = currentNumber - item;

                if (cartDec >= 0) {
                    CartProduct updatedProduct = new CartProduct(
                            cartProduct.getProductId(),
                            cartProduct.getProductTitle(),
                            cartProduct.getProductCategory(),
                            cartProduct.getProductImageUri(),
                            cartProduct.getProductQuantity(),
                            cartProduct.getProductPrice(),
                            cartProduct.getProductStock(),
                            cartDec,
                            cartProduct.getBuyCount(),
                            cartProduct.getRatingCount(),
                            cartProduct.getAverageRating()
                    );
                    cartBinding.productNumbers.setText(valueOf(cartDec));
                    userViewModel.setNumberOfCart(-1);
                    saveCartProductInDB(updatedProduct);
                    userViewModel.updateCartProductItemCount(cartProduct.getProductId(), cartDec);

                    if (cartDec == 0) {
                        userViewModel.deleteCartProduct(cartProduct.getProductId());
                    }
                }
            }

            private void saveCartProductInDB(CartProduct cartProduct) {
                userViewModel.updateCartProduct(cartProduct);
            }
        });

        binding.cartRecycler.setAdapter(cartAdapter);
    }

    private void updateCartTotal(List<CartProduct> cartProducts) {
        if(!cartProducts.isEmpty()){
            binding.noCartCv.setVisibility(View.GONE);
            binding.addMoreLl.setVisibility(View.VISIBLE);
            binding.billCv.setVisibility(View.VISIBLE);
            binding.payBtn.setVisibility(View.VISIBLE);

        }
        else{
            binding.noCartCv.setVisibility(View.VISIBLE);
            binding.addMoreLl.setVisibility(View.GONE);
            binding.billCv.setVisibility(View.GONE);
            binding.payBtn.setVisibility(View.GONE);

        }
        int total = 0;
        int deliveryCharge = 0;

        for (CartProduct product : cartProducts) {
            total += product.getProductPrice() * product.getItemCount();
        }
        if(total<250){
            deliveryCharge = 45;
            toPay = total+deliveryCharge;
            binding.cancelDileveryFee.setText(String.format("₹%d",deliveryCharge));
            binding.dileveryFee.setVisibility(View.GONE);
        }
        else{
            toPay = total;
            binding.dileveryFee.setVisibility(View.VISIBLE);
            binding.itemTotal.setText(String.valueOf(toPay));
            binding.cancelDileveryFee.setPaintFlags(binding.cancelDileveryFee.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            binding.cancelDileveryFee.setTextColor(getResources().getColor(R.color.dark_grey, this.getTheme()));
            binding.dileveryFee.setText(String.format("₹%d", deliveryCharge));
        }
        binding.overallPrice.setText(String.format("₹%d", toPay));
    }


        @Override
        public void onPaymentSuccess(String s) {
            Utils.observeOnce(userViewModel.getCartProducts(), this, cartProducts -> {
                for (CartProduct product : cartProducts) {
                    userViewModel.increaseBuyCountInFirebase(product,product.getItemCount());
                }

                saveOrder();
                userViewModel.deleteAllCartProductFromRoomDB();
                userViewModel.deleteCartProductFromFirebaseDb();
                Utils.showToast(this, "Payment Success");
            });
        }


    private void saveOrder() {
        Utils.observeOnce(userViewModel.getCartProducts(),this,cartProducts -> {
            String address = userViewModel.getCachedUserAddress();
            OrdersModel order = new OrdersModel(cartProducts,
                    address,
                    Utils.getUserId(),
                    getCurrentDate(),
                    Utils.getUniqueId(),
                    0
            );
            userViewModel.saveOrder(order);
        });
    }

    @Override
    public void onPaymentError(int i, String s) {
        Utils.showToast(this,"Payment failed");
    }
    public String getCurrentDate() {
        // Define the desired date format
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Get the current date
        Date date = new Date();

        // Format and return as string
        return sdf.format(date);
    }

}
