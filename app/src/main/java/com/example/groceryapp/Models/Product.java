package com.example.groceryapp.Models;

import java.util.ArrayList;


public class Product {
    String productId,productTitle,productCategory,adminUid,unit;
    int productQuantity,productPrice,productStock,itemCount,buyCount,ratingCount;
    float averageRating;

    ArrayList<String> productImageUris;

    public Product(){
    }

    public int getBuyCount() {
        return buyCount;
    }

    public void setBuyCount(int buyCount) {
        this.buyCount = buyCount;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    public Product(String productId,
                   String productTitle,
                   String productCategory,
                   String productType,
                   String adminUid,
                   String unit,
                   int productQuantity,
                   int productPrice,
                   int productStock,
                   int itemCount,
                   ArrayList<String> productImageUris,
                   int buyCount,
                   int ratingCount,
                   float averageRating) {

        this.productId = productId;
        this.productTitle = productTitle;
        this.productCategory = productCategory;
        this.adminUid = adminUid;
        this.productQuantity = productQuantity;
        this.productPrice = productPrice;
        this.productStock = productStock;
        this.itemCount = itemCount;
        this.unit = unit;
        this.productImageUris = productImageUris;
        this.buyCount = buyCount;
        this.ratingCount = ratingCount;
        this.averageRating = averageRating;

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

    public String getAdminUid() {
        return adminUid;
    }

    public void setAdminUid(String adminUid) {
        this.adminUid = adminUid;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(int productQuantity) {
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

    public ArrayList<String> getProductImageUris() {
        return productImageUris;
    }

    public void setProductImageUris(ArrayList<String> productImageUris) {
        this.productImageUris = productImageUris;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public boolean equals(Object obj){
        if(this==obj) return true;
        if(!(obj instanceof Product)) return false;
        Product p = (Product) obj;
        return productId.equals(p.getProductId()) &&
                productTitle.equals(p.getProductTitle()) &&
                productImageUris.equals(p.productImageUris) &&
                productPrice == p.getProductPrice() &&
                productQuantity == p.getProductQuantity() &&
                itemCount==p.getItemCount();
    }

}
