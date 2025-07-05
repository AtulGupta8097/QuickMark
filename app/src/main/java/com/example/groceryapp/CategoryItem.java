package com.example.groceryapp;

import com.example.groceryapp.Models.CategoryModel;
import com.example.groceryapp.Models.Product;

import java.util.List;

public class CategoryItem {
    public static final int TYPE_TITLE = 0;
    public static final int TYPE_CATEGORY_GRID = 1;
    public static final int TYPE_PRODUCT_GRID = 2;

    public int type;
    public String title;
    public List<CategoryModel> categories;
    public List<Product> products;

    // Title constructor
    public CategoryItem(int type, String title) {
        this.type = type;
        this.title = title;
    }

    // Factory method for category grid
    public static CategoryItem createCategoryGrid(List<CategoryModel> categories) {
        CategoryItem item = new CategoryItem(TYPE_CATEGORY_GRID, null);
        item.categories = categories;
        return item;
    }

    public static CategoryItem createProductGrid(List<Product> products) {
        CategoryItem item = new CategoryItem(TYPE_PRODUCT_GRID, null);
        item.products = products;
        return item;
    }

}
