package com.example.orderservice.service;

import com.example.orderservice.entity.Order;

import java.util.List;

public interface OrderService {
    List<Order> getAllOrders();
    Order getOrderById(Long id);
    List<Order> getOrdersByUserId(Long userId);
    Order createOrder(Order order);
    Order updateOrder(Order order);
    void deleteOrder(Long id);
    Order createSecKillOrder(Long userId, Long productId, int amount);
}
