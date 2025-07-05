package com.example.groceryapp.Models;

public class CategoryModel {
    int CategoryImage;
    String CategoryName;

    public int getCategoryImage() {
        return CategoryImage;
    }

    public void setCategoryImage(int categoryImage) {
        CategoryImage = categoryImage;
    }

    public String getCategoryName() {
        return CategoryName;
    }

    public void setCategoryName(String categoryName) {
        CategoryName = categoryName;
    }

    public CategoryModel(int categoryImage, String categoryName) {
        CategoryImage = categoryImage;
        CategoryName = categoryName;
    }
}
