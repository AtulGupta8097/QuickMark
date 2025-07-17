package com.example.groceryapp.Models;

import com.example.groceryapp.roomDatabase.CartProduct;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class OrdersModel{

    List<CartProduct> orderList;
    String userAddress, orderingUserId, orderDate, orderId, paymentMode;
    int orderStatus;

    // Required no-arg constructor for Firebase
    public OrdersModel() {}

    public OrdersModel(List<CartProduct> orderList, String userAddress, String orderingUserId,
                       String orderDate, String orderId, int orderStatus, String paymentMode) {
        this.orderList = orderList;
        this.userAddress = userAddress;
        this.orderingUserId = orderingUserId;
        this.orderDate = orderDate;
        this.orderId = orderId;
        this.orderStatus = orderStatus;
        this.paymentMode = paymentMode;
    }

    public List<CartProduct> getOrderList() { return orderList; }
    public void setOrderList(List<CartProduct> orderList) { this.orderList = orderList; }

    public String getUserAddress() { return userAddress; }
    public void setUserAddress(String userAddress) { this.userAddress = userAddress; }

    public String getOrderingUserId() { return orderingUserId; }
    public void setOrderingUserId(String orderingUserId) { this.orderingUserId = orderingUserId; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public int getOrderStatus() { return orderStatus; }
    public void setOrderStatus(int orderStatus) { this.orderStatus = orderStatus; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrdersModel that = (OrdersModel) o;

        // Assuming orderId is unique and reliable to compare
        return Objects.equals(orderId, that.orderId);
    }

    @Override
    public int hashCode() {
        return orderId != null ? orderId.hashCode() : 0;
    }

}
