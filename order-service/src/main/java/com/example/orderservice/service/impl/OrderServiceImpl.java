package com.example.orderservice.service.impl;

import com.example.orderservice.entity.Order;
import com.example.orderservice.mapper.OrderMapper;
import com.example.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${services.stock.base-url}")
    private String stockServiceBaseUrl;

    @Override
    @Cacheable(value = "orders")
    public List<Order> getAllOrders() {
        return orderMapper.findAll();
    }

    @Override
    @Cacheable(value = "order", key = "#id")
    public Order getOrderById(Long id) {
        return orderMapper.findById(id);
    }

    @Override
    @Cacheable(value = "orders", key = "#userId")
    public List<Order> getOrdersByUserId(Long userId) {
        return orderMapper.findByUserId(userId);
    }

    @Override
    @CacheEvict(value = "orders", allEntries = true)
    public Order createOrder(Order order) {
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("pending");
        orderMapper.insertOrder(order);
        return order;
    }

    @Override
    @CacheEvict(value = {"orders", "order"}, key = "#order.id")
    public Order updateOrder(Order order) {
        orderMapper.updateOrder(order);
        return order;
    }

    @Override
    @CacheEvict(value = {"orders", "order"}, key = "#id")
    public void deleteOrder(Long id) {
        orderMapper.deleteById(id);
    }

    @Override
    public Order createSecKillOrder(Long userId, Long productId, int amount) {
        // Call stock service to deduct stock
        Map<String, Object> deductReq = new HashMap<>();
        deductReq.put("productId", productId);
        deductReq.put("amount", amount);

        String stockUrl = stockServiceBaseUrl + "/api/stocks/deduct";
        Map<String, Object> response = restTemplate.postForObject(stockUrl, deductReq, Map.class);

        if (response != null && (Integer) response.get("code") == 0) {
            // Deduct success, create order
            Order order = new Order();
            order.setUserId(userId);
            order.setProductId(productId);
            order.setAmount(amount);
            order.setStatus("paid");
            order.setCreatedAt(LocalDateTime.now());
            // Assume stockId is productId for simplicity
            order.setStockId(productId);
            orderMapper.insertOrder(order);
            return order;
        } else {
            throw new RuntimeException("Stock deduct failed");
        }
    }
}
