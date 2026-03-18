package com.example.orderservice.controller;

import com.example.orderservice.entity.Order;
import com.example.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping
    public Map<String, Object> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 0);
        resp.put("message", "success");
        resp.put("data", orders);
        return resp;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        Map<String, Object> resp = new HashMap<>();
        if (order == null) {
            resp.put("code", 404);
            resp.put("message", "Order not found");
            return resp;
        }
        resp.put("code", 0);
        resp.put("message", "success");
        resp.put("data", order);
        return resp;
    }

    @GetMapping("/user/{userId}")
    public Map<String, Object> getOrdersByUserId(@PathVariable Long userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 0);
        resp.put("message", "success");
        resp.put("data", orders);
        return resp;
    }

    @PostMapping
    public Map<String, Object> createOrder(@RequestBody Order order) {
        Order created = orderService.createOrder(order);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 0);
        resp.put("message", "success");
        resp.put("data", created);
        return resp;
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateOrder(@PathVariable Long id, @RequestBody Order order) {
        order.setId(id);
        Order updated = orderService.updateOrder(order);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 0);
        resp.put("message", "success");
        resp.put("data", updated);
        return resp;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 0);
        resp.put("message", "success");
        return resp;
    }

    @PostMapping("/seckill")
    public Map<String, Object> createSecKillOrder(@RequestBody Map<String, Object> req) {
        Long userId = Long.valueOf(req.get("userId").toString());
        Long productId = Long.valueOf(req.get("productId").toString());
        int amount = Integer.valueOf(req.get("amount").toString());
        try {
            Order order = orderService.createSecKillOrder(userId, productId, amount);
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", 0);
            resp.put("message", "success");
            resp.put("data", order);
            return resp;
        } catch (Exception e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("code", 1);
            resp.put("message", e.getMessage());
            return resp;
        }
    }
}
