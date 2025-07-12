package com.example.groceryapp.roomDatabase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
@Database(entities = {CartProduct.class},version = 3,exportSchema = false)
public abstract class CartProductDatabase extends RoomDatabase {

    private static volatile CartProductDatabase INSTANCE;
    public abstract CartProductDao myDao();
    public static CartProductDatabase getInstance(Context context){
        if(INSTANCE==null){
            synchronized (CartProductDatabase.class){
                if(INSTANCE==null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            CartProductDatabase.class,"cart_product").fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
