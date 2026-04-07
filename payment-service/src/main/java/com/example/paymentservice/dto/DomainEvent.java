package com.example.paymentservice.dto;

import lombok.Data;

@Data
public class DomainEvent {
    private String eventId;
    private String eventType;
    private String aggregateType;
    private String aggregateId;
    private Object payload;
    private Long occurredAt;
}

