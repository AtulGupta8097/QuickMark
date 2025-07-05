package com.example.groceryapp.Models;

import com.example.groceryapp.roomDatabase.CartProduct;

import java.util.List;

public class OrdersModel {
    List<CartProduct> orderList;
    String userAddress,orderingUserId,orderDate,orderId;
    int orderStatus;

    public List<CartProduct> getOrderList() {
        return orderList;
    }

    public void setOrderList(List<CartProduct> orderList) {
        this.orderList = orderList;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getOrderingUserId() {
        return orderingUserId;
    }

    public void setOrderingUserId(String orderingUserId) {
        this.orderingUserId = orderingUserId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(int orderStatus) {
        this.orderStatus = orderStatus;
    }

    public OrdersModel(List<CartProduct> orderList, String userAddress, String orderingUserId, String orderDate, String orderId, int orderStatus) {
        this.orderList = orderList;
        this.userAddress = userAddress;
        this.orderingUserId = orderingUserId;
        this.orderDate = orderDate;
        this.orderId = orderId;
        this.orderStatus = orderStatus;
    }
}
