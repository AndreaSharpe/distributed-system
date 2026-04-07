package com.example.orderservice.service.impl;

import com.example.orderservice.dto.DomainEvent;
import com.example.orderservice.service.OrderPaymentOrchestrator;
import com.example.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class OrderPaymentOrchestratorImpl implements OrderPaymentOrchestrator {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${services.stock.base-url}")
    private String stockServiceBaseUrl;

    @Override
    public void handle(DomainEvent event) {
        if (event == null || event.getEventType() == null) return;
        if (!"PaymentSucceeded".equals(event.getEventType()) && !"PaymentFailed".equals(event.getEventType())) {
            return;
        }
        if (!(event.getPayload() instanceof Map)) {
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) event.getPayload();
        Object orderNoObj = payload.get("orderNo");
        if (orderNoObj == null) return;
        Long orderNo = Long.valueOf(orderNoObj.toString());

        com.example.orderservice.entity.Order o = orderService.getOrderByOrderNo(orderNo);
        if (o == null) return;

        if ("PaymentSucceeded".equals(event.getEventType())) {
            if ("paid".equalsIgnoreCase(o.getStatus())) return;
            Map<String, Object> body = new HashMap<>();
            body.put("orderNo", orderNo);
            restTemplate.postForObject(stockServiceBaseUrl + "/api/stocks/tcc/confirm", body, Map.class);
            orderService.updateStatusByOrderNo(orderNo, "paid");
        } else {
            if ("cancelled".equalsIgnoreCase(o.getStatus()) || "rejected".equalsIgnoreCase(o.getStatus())) return;
            Map<String, Object> body = new HashMap<>();
            body.put("orderNo", orderNo);
            restTemplate.postForObject(stockServiceBaseUrl + "/api/stocks/tcc/cancel", body, Map.class);
            orderService.updateStatusByOrderNo(orderNo, "cancelled");
        }
    }
}

