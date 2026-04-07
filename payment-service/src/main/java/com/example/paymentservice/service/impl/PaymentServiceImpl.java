package com.example.paymentservice.service.impl;

import com.example.paymentservice.dto.DomainEvent;
import com.example.paymentservice.entity.OutboxEvent;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.mapper.OutboxEventMapper;
import com.example.paymentservice.mapper.PaymentMapper;
import com.example.paymentservice.service.PaymentService;
import com.example.paymentservice.util.SnowflakeIdGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private OutboxEventMapper outboxEventMapper;

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${events.topic:domain-events}")
    private String eventsTopic;

    @Override
    @Transactional
    public Payment createPayment(Long orderNo, Long userId, BigDecimal amount, String channel) {
        if (orderNo == null || userId == null) {
            throw new IllegalArgumentException("orderNo/userId required");
        }
        if (amount == null) amount = BigDecimal.ZERO;

        Payment p = new Payment();
        p.setPaymentNo(snowflakeIdGenerator.nextId());
        p.setOrderNo(orderNo);
        p.setUserId(userId);
        p.setAmount(amount);
        p.setStatus("INIT");
        p.setChannel(channel);
        paymentMapper.insert(p);
        return p;
    }

    @Override
    public Payment getByPaymentNo(Long paymentNo) {
        return paymentMapper.findByPaymentNo(paymentNo);
    }

    @Override
    @Transactional
    public Payment callback(Long paymentNo, boolean success) {
        Payment p = paymentMapper.findByPaymentNo(paymentNo);
        if (p == null) {
            throw new IllegalArgumentException("payment not found");
        }
        String to = success ? "SUCCESS" : "FAILED";
        if (to.equals(p.getStatus())) {
            return p;
        }

        paymentMapper.updateStatusAny(paymentNo, to);
        Payment updated = paymentMapper.findByPaymentNo(paymentNo);

        DomainEvent evt = new DomainEvent();
        evt.setEventId(UUID.randomUUID().toString().replace("-", ""));
        evt.setEventType(success ? "PaymentSucceeded" : "PaymentFailed");
        evt.setAggregateType("Payment");
        evt.setAggregateId(String.valueOf(paymentNo));
        evt.setOccurredAt(System.currentTimeMillis());
        Map<String, Object> payload = new HashMap<>();
        payload.put("paymentNo", paymentNo);
        payload.put("orderNo", updated.getOrderNo());
        payload.put("userId", updated.getUserId());
        payload.put("amount", updated.getAmount());
        payload.put("status", updated.getStatus());
        evt.setPayload(payload);

        try {
            String json = objectMapper.writeValueAsString(evt);
            OutboxEvent out = new OutboxEvent();
            out.setEventId(evt.getEventId());
            out.setAggregateType(evt.getAggregateType());
            out.setAggregateId(evt.getAggregateId());
            out.setEventType(evt.getEventType());
            out.setTopic(eventsTopic);
            out.setPayload(json);
            out.setStatus("NEW");
            out.setRetryCount(0);
            outboxEventMapper.insert(out);
        } catch (DuplicateKeyException ignore) {
            // ignore
        } catch (Exception e) {
            throw new IllegalStateException("write outbox failed", e);
        }

        return updated;
    }
}

