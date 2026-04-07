package com.example.orderservice.service.impl;

import com.example.orderservice.dto.SeckillOrderKafkaMessage;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OutboxEvent;
import com.example.orderservice.exception.DuplicateSecKillException;
import com.example.orderservice.mapper.OutboxEventMapper;
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
import java.util.UUID;

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

    @Autowired
    private OutboxEventMapper outboxEventMapper;

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
    @CacheEvict(value = {"orders", "order"}, allEntries = true)
    public void updateStatusByOrderNo(Long orderNo, String status) {
        if (orderNo == null || status == null) return;
        orderMapper.updateStatusByOrderNo(orderNo, status);
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

        SeckillOrderKafkaMessage msg = new SeckillOrderKafkaMessage();
        msg.setEventId(UUID.randomUUID().toString().replace("-", ""));
        msg.setOrderNo(orderNo);
        msg.setUserId(userId);
        msg.setProductId(productId);
        msg.setAmount(amount);

        try {
            String json = objectMapper.writeValueAsString(msg);
            OutboxEvent event = new OutboxEvent();
            event.setEventId(msg.getEventId());
            event.setAggregateType("Order");
            event.setAggregateId(String.valueOf(orderNo));
            event.setEventType("SeckillOrderRequested");
            event.setTopic(seckillOrderTopic);
            event.setPayload(json);
            event.setStatus("NEW");
            event.setRetryCount(0);
            outboxEventMapper.insert(event);
        } catch (Exception e) {
            redisTemplate.delete(idemKey);
            throw new RuntimeException("写入 Outbox 失败", e);
        }

        Order accepted = new Order();
        accepted.setOrderNo(orderNo);
        accepted.setUserId(userId);
        accepted.setProductId(productId);
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

        Map<String, Object> tryBody = new HashMap<>();
        tryBody.put("orderNo", msg.getOrderNo());
        tryBody.put("productId", msg.getProductId());
        tryBody.put("amount", msg.getAmount());
        tryBody.put("ttlSeconds", 120);

        Map<?, ?> tryResp = restTemplate.postForObject(
                stockServiceBaseUrl + "/api/stocks/tcc/try", tryBody, Map.class);
        boolean reserved = tryResp != null && Integer.valueOf(0).equals(tryResp.get("code"));
        Long stockId = null;
        if (reserved) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) tryResp.get("data");
            if (data != null && data.get("stockId") != null) {
                stockId = Long.valueOf(data.get("stockId").toString());
            }
        }

        Order pending = new Order();
        pending.setOrderNo(msg.getOrderNo());
        pending.setUserId(msg.getUserId());
        pending.setProductId(msg.getProductId());
        pending.setStockId(stockId);
        pending.setAmount(msg.getAmount());
        pending.setStatus(reserved ? "reserved" : "rejected");
        pending.setCreatedAt(LocalDateTime.now());

        try {
            orderMapper.insertOrder(pending);
        } catch (DataIntegrityViolationException e) {
            return;
        }

        if (!reserved) {
            return;
        }
    }
}
