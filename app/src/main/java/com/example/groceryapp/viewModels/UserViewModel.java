package com.example.groceryapp.viewModels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.groceryapp.Models.OrdersModel;
import com.example.groceryapp.Models.Product;
import com.example.groceryapp.utils.Utils;
import com.example.groceryapp.roomDatabase.CartProduct;
import com.example.groceryapp.roomDatabase.CartProductDao;
import com.example.groceryapp.roomDatabase.CartProductDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Product>> allProducts = new MutableLiveData<>(null);
    private final Map<String, MutableLiveData<List<Product>>> categoryProductMap = new HashMap<>();
    private final Map<String, List<Product>> categoryCache = new HashMap<>();
    private final Map<String, Product> productCache = new HashMap<>();
    private List<Product> allProductsCache = null;

    private final MutableLiveData<String> userAddressFirebase = new MutableLiveData<>();
    private final MutableLiveData<Integer> badgeCartCount = new MutableLiveData<>(0);

    private final CartProductDao cartProductDao;
    private final ExecutorService executorService;
    private final SharedPreferences cartSharedPreferences, userAddressPreferences;
    private final LiveData<List<CartProduct>> cartProduct;
    private final Context appContext;

    public UserViewModel(@NonNull Application application) {
        super(application);
        appContext = application.getApplicationContext();
        executorService = Executors.newSingleThreadExecutor();
        cartProductDao = CartProductDatabase.getInstance(appContext).myDao();
        cartProduct = cartProductDao.getCartProducts();
        cartSharedPreferences = application.getSharedPreferences("cart_pref", Context.MODE_PRIVATE);
        userAddressPreferences = application.getSharedPreferences("AddressPref", Context.MODE_PRIVATE);
    }

    private String getCurrentPhone() {
        return Utils.getUserPhoneNumber();
    }

    public void fetchCategoryProductRealtime(String category) {
        if (!categoryProductMap.containsKey(category)) {
            categoryProductMap.put(category, new MutableLiveData<>(null));
        }
        MutableLiveData<List<Product>> liveData = categoryProductMap.get(category);

        String phone = getCurrentPhone();
        if (phone == null) return;

        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference()
                .child("Admins").child("AdminInfo").child("ProductCategory").child(category);
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference()
                .child("Admins").child("AdminInfo").child("userCarts").child(phone);

        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot categorySnap) {
                List<Product> productList = new ArrayList<>();

                cartRef.get().addOnSuccessListener(cartSnap -> {
                    for (DataSnapshot snap : categorySnap.getChildren()) {
                        Product p = snap.getValue(Product.class);
                        if (p != null) {
                            int count = cartSnap.hasChild(p.getProductId())
                                    ? cartSnap.child(p.getProductId()).child("itemCount").getValue(Integer.class)
                                    : 0;
                            p.setItemCount(count);
                            productCache.put(p.getProductId(), p);
                            productList.add(p);
                        }
                    }
                    categoryCache.put(category, productList);
                    assert liveData != null;
                    liveData.setValue(productList);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void setBadgeCartCount(int count) {
        badgeCartCount.setValue(count);
    }


    public LiveData<List<Product>> getCategoryProduct(String category) {
        if (!categoryProductMap.containsKey(category)) {
            categoryProductMap.put(category, new MutableLiveData<>(null));
        }
        return categoryProductMap.get(category);
    }

    public boolean isCategoryCached(String category) {
        return categoryCache.containsKey(category);
    }

    public void clearCategoryCache() {
        categoryCache.clear();
        categoryProductMap.clear();
    }
    public void setCartCountToZero() {
        cartSharedPreferences.edit().putInt("cartCount", 0).apply();
        badgeCartCount.setValue(0);
    }


    public void fetchAllProductsRealtime() {
        String phone = getCurrentPhone();
        if (phone == null) return;

        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference()
                .child("Admins").child("AdminInfo").child("AllProducts");
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference()
                .child("Admins").child("AdminInfo").child("userCarts").child(phone);

        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot productSnap) {
                List<Product> productList = new ArrayList<>();

                cartRef.get().addOnSuccessListener(cartSnap -> {
                    for (DataSnapshot snap : productSnap.getChildren()) {
                        Product p = snap.getValue(Product.class);
                        if (p != null) {
                            if(cartSnap.hasChild(p.getProductId())){
                                CartProduct product = cartSnap.child(p.getProductId()).getValue(CartProduct.class);
                                assert product != null;
                                int count = product.getItemCount();
                                p.setItemCount(count);

                            }
                            else{
                                p.setItemCount(0);
                            }
                            productCache.put(p.getProductId(), p);
                            productList.add(p);
                        }
                    }
                    allProductsCache = productList;
                    allProducts.setValue(productList);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UserViewModel", "fetchAllProductsRealtime failed: " + error.getMessage());
            }
        });
    }


    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }

    public boolean isAllProductsCached() {
        return allProductsCache != null;
    }

    public void clearAllProductsCache() {
        allProductsCache = null;
    }


    public LiveData<Integer> getBadgeCartCount() {
        return badgeCartCount;
    }

    public void resetProductsAndCartBadge() {
        clearAllProductsCache();
        clearCategoryCache();
        setCartCountToZero();
    }

    // Add this method at the end of your UserViewModel class before the closing bracket

    public void recoverUserDataFromFirebase(Runnable onComplete) {
        String phone = getCurrentPhone();
        if (phone == null) {
            onComplete.run();
            return;
        }
        deleteAllCartProductFromRoomDB();

        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference()
                .child("Admins").child("AdminInfo").child("userCarts").child(phone);
        DatabaseReference addressRef = FirebaseDatabase.getInstance().getReference()
                .child("AllUsers").child("User").child("UserAddress").child(phone);

        // Clear existing Room cart before recovery
//        deleteAllCartProductFromRoomDB();

        // Fetch cart products
        cartRef.get().addOnSuccessListener(cartSnapshot -> {
            int totalCount = 0;
            for (DataSnapshot child : cartSnapshot.getChildren()) {
                CartProduct cartProduct = child.getValue(CartProduct.class);
                if (cartProduct != null) {
                    insertCartProduct(cartProduct);
                    totalCount += cartProduct.getItemCount();
                }
            }
            // Update badge count LiveData and preference
            badgeCartCount.postValue(totalCount);
            cartSharedPreferences.edit().putInt("cartCount", totalCount).apply();

            // Now fetch address after cart recovery
            addressRef.get().addOnSuccessListener(addressSnapshot -> {
                String address = addressSnapshot.exists() ? addressSnapshot.getValue(String.class) : "Not found";
                userAddressFirebase.setValue(address);
                saveUserAddressInPref(address);
                onComplete.run(); // Done with both

            }).addOnFailureListener(e -> {
                Log.e("RecoverUserData", "Failed to fetch address: " + e.getMessage());
                userAddressFirebase.setValue("Not found");
                saveUserAddressInPref("Not found");
                onComplete.run();
            });

        }).addOnFailureListener(e -> {
            Log.e("RecoverUserData", "Failed to recover cart: " + e.getMessage());
            userAddressFirebase.setValue("Not found");
            saveUserAddressInPref("Not found");
            badgeCartCount.postValue(0);
            cartSharedPreferences.edit().putInt("cartCount", 0).apply();
            onComplete.run();
        });
    }

    public void syncCartToPreferences(String phone) {
        if (phone == null) return;
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference()
                .child("Admins").child("AdminInfo").child("userCarts").child(phone);

        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int total = 0;
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Integer count = snap.child("itemCount").getValue(Integer.class);
                    if (count != null) total += count;
                }
                cartSharedPreferences.edit().putInt("cartCount", total).apply();
                badgeCartCount.postValue(total);  // <-- update badge count LiveData directly
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    public void updateCartProductItem(CartProduct product) {
        String phone = getCurrentPhone();
        if (phone == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("Admins").child("AdminInfo").child("userCarts")
                .child(phone)
                .child(product.getProductId());
        ref.setValue(product);
    }


    public void deleteCartProductFromFirebaseDb() {
        String phone = getCurrentPhone();
        if (phone == null) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("Admins").child("AdminInfo").child("userCarts").child(phone);
        ref.removeValue();
    }

    public void saveUserAddressToFirebase(String address) {
        String phone = getCurrentPhone();
        if (phone == null) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("AllUsers").child("User").child("UserAddress").child(phone);
        ref.setValue(address);
    }

    public void saveOrder(OrdersModel order) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("Admins").child("AdminInfo").child("Orders").child(order.getOrderId());
        ref.setValue(order);
    }

    public void increaseBuyCountInFirebase(CartProduct product, int incrementBy) {
        String productId = product.getProductId();
        String category = product.getProductCategory();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        ref.child("Admins").child("AdminInfo").child("AllProducts").child(productId).child("buyCount")
                .get().addOnSuccessListener(snap -> {
                    int current = snap.exists() ? snap.getValue(Integer.class) : 0;
                    ref.child("Admins").child("AdminInfo").child("AllProducts").child(productId)
                            .child("buyCount").setValue(current + incrementBy);
                });

        ref.child("Admins").child("AdminInfo").child("ProductCategory").child(category)
                .child(productId).child("buyCount").get().addOnSuccessListener(snap -> {
                    int current = snap.exists() ? snap.getValue(Integer.class) : 0;
                    ref.child("Admins").child("AdminInfo").child("ProductCategory")
                            .child(category).child(productId)
                            .child("buyCount").setValue(current + incrementBy);
                });
    }

    public void insertCartProduct(CartProduct cartProduct) {
        executorService.execute(() -> cartProductDao.insertProduct(cartProduct));
    }

    public void updateCartProduct(CartProduct cartProduct) {
        executorService.execute(() -> cartProductDao.updateCartProduct(cartProduct));
    }

    public void deleteCartProduct(String productId) {
        executorService.execute(() -> cartProductDao.deleteCartProductById(productId));
    }

    public void deleteAllCartProductFromRoomDB() {
        executorService.execute(cartProductDao::deleteAllCartProduct);
    }

    public LiveData<List<CartProduct>> getCartProducts() {
        return cartProduct;
    }

    public void saveUserAddressInPref(String address) {
        userAddressPreferences.edit().putString("UserAddress", address).apply();
    }

    public String getUserAddressFromPref() {
        return userAddressPreferences.getString("UserAddress", "Not found");
    }

    public String getCachedUserAddress() {
        return userAddressFirebase.getValue() != null ? userAddressFirebase.getValue() : getUserAddressFromPref();
    }

}
