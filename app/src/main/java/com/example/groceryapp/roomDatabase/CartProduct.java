package com.example.groceryapp.roomDatabase;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Objects;

@Entity(tableName = "cart_product")
public class CartProduct {
    @PrimaryKey(autoGenerate = false)
    @NonNull
    String productId;
    String productTitle,productCategory,productImageUri,productQuantity;
    int productPrice,productStock,itemCount,buyCount,ratingCount;
    float averageRating;

    public int getBuyCount() {
        return buyCount;
    }

    public void setBuyCount(int buyCount) {
        this.buyCount = buyCount;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    public CartProduct(@NonNull String productId, String productTitle, String productCategory, String productImageUri,
                       String productQuantity, int productPrice, int productStock, int itemCount, int buyCount,
                       int ratingCount,
                       float averageRating) {

        this.productId = productId;
        this.productTitle = productTitle;
        this.productCategory = productCategory;
        this.productImageUri = productImageUri;
        this.productQuantity = productQuantity;
        this.productPrice = productPrice;
        this.productStock = productStock;
        this.itemCount = itemCount;
        this.buyCount = buyCount;
        this.ratingCount = ratingCount;
        this.averageRating = averageRating;
    }
    public CartProduct(){

    }

    @NonNull
    public String getProductId() {
        return productId;
    }

    public void setProductId(@NonNull String productId) {
        this.productId = productId;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getProductImageUri() {
        return productImageUri;
    }

    public void setProductImageUri(String productImageUri) {
        this.productImageUri = productImageUri;
    }

    public String getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(String productQuantity) {
        this.productQuantity = productQuantity;
    }

    public int getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(int productPrice) {
        this.productPrice = productPrice;
    }

    public int getProductStock() {
        return productStock;
    }

    public void setProductStock(int productStock) {
        this.productStock = productStock;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    @Override
    public boolean equals(Object obj){
        if(this==obj) return true;
        if(!(obj instanceof CartProduct)) return false;
        CartProduct p = (CartProduct) obj;
        return this.productId.equals(p.getProductId()) &&
                this.productCategory.equals(p.getProductCategory()) &&
                this.productImageUri.equals(p.getProductImageUri()) &&
                this.productPrice == p.getProductPrice() &&
                this.itemCount==p.getItemCount();
    }

}
