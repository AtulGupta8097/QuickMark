package com.example.groceryapp.roomDatabase;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CartProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProduct(CartProduct cartProduct);
    @Update
    void updateCartProduct(CartProduct cartProduct);
    @Query("SELECT * FROM cart_product")
    LiveData<List<CartProduct>> getCartProducts();

    @Query("DELETE FROM cart_product WHERE productId = :id")
    void deleteCartProductById(String id);
    @Query("DELETE FROM cart_product")
    void deleteAllCartProduct();
}
