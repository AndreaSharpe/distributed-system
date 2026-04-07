package com.example.orderservice.service;

import com.example.orderservice.dto.SeckillOrderKafkaMessage;
import com.example.orderservice.entity.Order;

import java.util.List;

public interface OrderService {
    List<Order> getAllOrders();
    Order getOrderById(Long id);
    Order getOrderByOrderNo(Long orderNo);
    List<Order> getOrdersByUserId(Long userId);
    Order createOrder(Order order);
    Order updateOrder(Order order);
    void deleteOrder(Long id);
    Order createSecKillOrder(Long userId, Long productId, int amount);

    /** Kafka 异步落单 */
    void consumeSeckillOrder(SeckillOrderKafkaMessage message);

    /** 订单状态流转（用于支付回调编排），包含缓存驱逐 */
    void updateStatusByOrderNo(Long orderNo, String status);
}
