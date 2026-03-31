package com.example.orderservice.service.impl;

import com.example.orderservice.dto.SeckillOrderKafkaMessage;
import com.example.orderservice.entity.Order;
import com.example.orderservice.exception.DuplicateSecKillException;
import com.example.orderservice.mapper.OrderMapper;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.util.SnowflakeIdGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${services.stock.base-url}")
    private String stockServiceBaseUrl;

    @Value("${seckill.order-topic:seckill-order}")
    private String seckillOrderTopic;

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
    @Cacheable(value = "order", key = "'no:' + #orderNo")
    public Order getOrderByOrderNo(Long orderNo) {
        if (orderNo == null) {
            return null;
        }
        return orderMapper.findByOrderNo(orderNo);
    }

    @Override
    @Cacheable(value = "orders", key = "#userId")
    public List<Order> getOrdersByUserId(Long userId) {
        return orderMapper.findByUserId(userId);
    }

    @Override
    @CacheEvict(value = "orders", allEntries = true)
    public Order createOrder(Order order) {
        if (order.getOrderNo() == null) {
            order.setOrderNo(snowflakeIdGenerator.nextId());
        }
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
        String idemKey = "seckill:idem:" + userId + ":" + productId;
        Long orderNo = snowflakeIdGenerator.nextId();
        Boolean first = redisTemplate.opsForValue().setIfAbsent(idemKey, String.valueOf(orderNo), Duration.ofDays(1));
        if (Boolean.FALSE.equals(first)) {
            String prev = (String) redisTemplate.opsForValue().get(idemKey);
            throw new DuplicateSecKillException(Long.parseLong(prev));
        }

        Map<String, Object> body = new HashMap<>();
        body.put("productId", productId);
        body.put("amount", amount);

        Map<?, ?> reserveResp = restTemplate.postForObject(
                stockServiceBaseUrl + "/api/stocks/seckill/reserve", body, Map.class);
        if (reserveResp == null || !Integer.valueOf(0).equals(reserveResp.get("code"))) {
            redisTemplate.delete(idemKey);
            throw new RuntimeException("Redis 库存预扣失败");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) reserveResp.get("data");
        Long stockId = Long.valueOf(data.get("stockId").toString());

        SeckillOrderKafkaMessage msg = new SeckillOrderKafkaMessage();
        msg.setOrderNo(orderNo);
        msg.setUserId(userId);
        msg.setProductId(productId);
        msg.setAmount(amount);
        msg.setStockId(stockId);

        try {
            String json = objectMapper.writeValueAsString(msg);
            kafkaTemplate.send(seckillOrderTopic, String.valueOf(orderNo), json).get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            restTemplate.postForObject(stockServiceBaseUrl + "/api/stocks/seckill/release", body, Map.class);
            redisTemplate.delete(idemKey);
            throw new RuntimeException("Kafka 发送失败，已回滚预扣", e);
        }

        Order accepted = new Order();
        accepted.setOrderNo(orderNo);
        accepted.setUserId(userId);
        accepted.setProductId(productId);
        accepted.setStockId(stockId);
        accepted.setAmount(amount);
        accepted.setStatus("accepted");
        return accepted;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"orders", "order"}, allEntries = true)
    public void consumeSeckillOrder(SeckillOrderKafkaMessage msg) {
        if (msg == null || msg.getOrderNo() == null) {
            return;
        }
        if (orderMapper.findByOrderNo(msg.getOrderNo()) != null) {
            return;
        }

        Order pending = new Order();
        pending.setOrderNo(msg.getOrderNo());
        pending.setUserId(msg.getUserId());
        pending.setProductId(msg.getProductId());
        pending.setStockId(msg.getStockId());
        pending.setAmount(msg.getAmount());
        pending.setStatus("pending");
        pending.setCreatedAt(LocalDateTime.now());

        try {
            orderMapper.insertOrder(pending);
        } catch (DataIntegrityViolationException e) {
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("productId", msg.getProductId());
        body.put("amount", msg.getAmount());

        Map<?, ?> dbResp = restTemplate.postForObject(
                stockServiceBaseUrl + "/api/stocks/db-deduct", body, Map.class);
        boolean ok = dbResp != null && Integer.valueOf(0).equals(dbResp.get("code"));
        if (!ok) {
            orderMapper.deleteByOrderNo(msg.getOrderNo());
            restTemplate.postForObject(stockServiceBaseUrl + "/api/stocks/seckill/release", body, Map.class);
            return;
        }

        pending.setStatus("paid");
        orderMapper.updateOrder(pending);
    }
}
