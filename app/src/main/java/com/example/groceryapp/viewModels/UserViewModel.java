package com.example.groceryapp.viewModels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.groceryapp.Models.OrdersModel;
import com.example.groceryapp.Models.Product;
import com.example.groceryapp.Utils;
import com.example.groceryapp.CartRepository;
import com.example.groceryapp.roomDatabase.CartProduct;
import com.example.groceryapp.roomDatabase.CartProductDao;
import com.example.groceryapp.roomDatabase.CartProductDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Product>> categoryProduct = new MutableLiveData<>(null);
    private final MutableLiveData<List<Product>> allProducts = new MutableLiveData<>(null);
    private final CartProductDao cartProductDao;
    private final ExecutorService executorService;
    private final SharedPreferences cartSharedPreferences, userAddressPreferences;
    private final LiveData<List<CartProduct>> cartProduct;
    private final MutableLiveData<String> userAddressFirebase = new MutableLiveData<>();

    private final CartRepository cartRepository;

    public UserViewModel(@NonNull Application application) {
        super(application);

        executorService = Executors.newSingleThreadExecutor();
        cartProductDao = CartProductDatabase.getInstance(application.getApplicationContext()).myDao();
        cartProduct = cartProductDao.getCartProducts();

        cartSharedPreferences = application.getSharedPreferences("cart_pref", Context.MODE_PRIVATE);
        userAddressPreferences = application.getSharedPreferences("AddressPref", Context.MODE_PRIVATE);

        cartRepository = CartRepository.getInstance();
    }

    // CATEGORY PRODUCTS FETCH
    public void fetchCategoryProduct(String category) {
        categoryProduct.setValue(null);

        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference()
                .child("Admins").child("AdminInfo").child("ProductCategory").child(category);
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference()
                .child("Admins").child("AdminInfo").child("userCarts").child(Utils.getUserId());

        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot categorySnap) {
                List<Product> temp = new ArrayList<>();
                cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot cartSnap) {
                        for (DataSnapshot snap : categorySnap.getChildren()) {
                            Product p = snap.getValue(Product.class);
                            if (p != null) {
                                if (cartSnap.hasChild(p.getProductId())) {
                                    Integer count = cartSnap.child(p.getProductId())
                                            .child("itemCount").getValue(Integer.class);
                                    p.setItemCount(count != null ? count : 0);
                                } else {
                                    p.setItemCount(0);
                                }
                                temp.add(p);
                            }
                        }
                        categoryProduct.setValue(temp);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }



    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }

    public void increaseBuyCountInFirebase(CartProduct product, int incrementBy) {
        String productId = product.getProductId();
        String category = product.getProductCategory();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

        // Update in AllProducts
        dbRef.child("Admins")
                .child("AdminInfo")
                .child("AllProducts")
                .child(productId)
                .child("buyCount")
                .get()
                .addOnSuccessListener(snapshot -> {
                    int current = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                    dbRef.child("Admins")
                            .child("AdminInfo")
                            .child("AllProducts")
                            .child(productId)
                            .child("buyCount")
                            .setValue(current + incrementBy);
                });

        // Update in ProductCategory
        dbRef.child("Admins")
                .child("AdminInfo")
                .child("ProductCategory")
                .child(category)
                .child(productId)
                .child("buyCount")
                .get()
                .addOnSuccessListener(snapshot -> {
                    int current = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                    dbRef.child("Admins")
                            .child("AdminInfo")
                            .child("ProductCategory")
                            .child(category)
                            .child(productId)
                            .child("buyCount")
                            .setValue(current + incrementBy);
                });
    }


    public void fetchAllProducts() {
        DatabaseReference allProductsRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("Admins").child("AdminInfo").child("AllProducts");

        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("Admins").child("AdminInfo").child("userCarts").child(Utils.getUserId());

        allProductsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot productSnap) {
                List<Product> productList = new ArrayList<>();

                cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot cartSnap) {
                        for (DataSnapshot snap : productSnap.getChildren()) {
                            Product p = snap.getValue(Product.class);
                            if (p != null) {
                                if (cartSnap.hasChild(p.getProductId())) {
                                    Integer count = cartSnap.child(p.getProductId())
                                            .child("itemCount").getValue(Integer.class);
                                    p.setItemCount(count != null ? count : 0);
                                } else {
                                    p.setItemCount(0);
                                }
                                productList.add(p);
                            }
                        }

                        allProducts.setValue(productList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // handle error if needed
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // handle error if needed
            }
        });
    }




    public MutableLiveData<List<Product>> getCategoryProduct() {
        return categoryProduct;
    }

    // CART COUNT MANAGEMENT
    public void setNumberOfCart(int numberOfItem) {
        Integer current = cartRepository.getNumberOfCart().getValue();
        if (current == null) current = 0;
        int updatedCount = current + numberOfItem;
        cartRepository.setNumberOfCart(updatedCount);
        cartSharedPreferences.edit().putInt("cartCount", updatedCount).apply();
    }

    public LiveData<Integer> getNumberOfCart() {
        int count = cartSharedPreferences.getInt("cartCount", 0);
        if (cartRepository.getNumberOfCart().getValue() == null || cartRepository.getNumberOfCart().getValue() == 0) {
            syncCartToPreferences(Utils.getUserId());
        }
        cartRepository.setNumberOfCart(count);
        return cartRepository.getNumberOfCart();
    }

    public void syncCartToPreferences(String userId) {
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference()
                .child("Admins").child("AdminInfo").child("userCarts").child(userId);

        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalCount = 0;
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Integer count = snap.child("itemCount").getValue(Integer.class);
                    if (count != null) {
                        totalCount += count;
                    }
                }
                cartSharedPreferences.edit().putInt("cartCount", totalCount).apply();
                cartRepository.setNumberOfCart(totalCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void updateCartProductItemCount(String productId, int count) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Admins").child("AdminInfo");
        databaseReference.child("userCarts").child(Utils.getUserId())
                .child(productId).child("itemCount").setValue(count);
    }
    public void deleteCartProductFromFirebaseDb(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Admins").child("AdminInfo");
        databaseReference.child("userCarts").removeValue();
    }
    public void saveUserAddressToFirebase(String address){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Admins").child("AdminInfo").child("UserAddress").child(Utils.getUserId());
        databaseReference.child("Address").setValue(address);
    }
    public LiveData<String> getUserAddressFromFirebase() {
        // Only fetch from Firebase if value not already loaded
        if (userAddressFirebase.getValue() == null) {
            fetchUserAddressFromFirebase();
        }
        return userAddressFirebase;
    }

    private void fetchUserAddressFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Admins").child("AdminInfo").child("UserAddress").child(Utils.getUserId()).child("Address");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userAddressFirebase.setValue(snapshot.getValue(String.class));
                } else {
                    userAddressFirebase.setValue("Not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                userAddressFirebase.setValue("Not found");
            }
        });
    }
    public void saveOrder(OrdersModel order){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Admins")
                .child("AdminInfo")
                .child("Orders")
                .child(order.getOrderId());
        databaseReference.setValue(order);

    }


    // ROOM DATABASE ACCESS
    public void insertCartProduct(CartProduct cartProduct) {
        executorService.execute(() -> cartProductDao.insertProduct(cartProduct));
    }

    public LiveData<List<CartProduct>> getCartProducts() {
        return cartProduct;
    }

    public void updateCartProduct(CartProduct cartProduct) {
        executorService.execute(() -> cartProductDao.updateCartProduct(cartProduct));
    }

    public void deleteCartProduct(String cartProductId) {
        executorService.execute(() -> cartProductDao.deleteCartProductById(cartProductId));
    }

    public void deleteAllCartProductFromRoomDB(){
        executorService.execute(cartProductDao::deleteAllCartProduct);

    }

    // USER ADDRESS PREFS
    public void saveUserAddressInPref(String address) {
        userAddressPreferences.edit().putString("UserAddress", address).apply();
    }



    public String getUserAddressFromPref() {
        return userAddressPreferences.getString("UserAddress","Not found");
    }

    public String getCachedUserAddress() {
        if (userAddressFirebase.getValue() != null) {
            return userAddressFirebase.getValue();
        } else {
            return getUserAddressFromPref();
        }
    }

}
