package com.example.orderservice.service;

import com.example.orderservice.dto.DomainEvent;

public interface OrderPaymentOrchestrator {
    void handle(DomainEvent event);
}

