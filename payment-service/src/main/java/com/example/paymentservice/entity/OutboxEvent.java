package com.example.paymentservice.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OutboxEvent {
    private Long id;
    private String eventId;
    private String aggregateType;
    private String aggregateId;
    private String eventType;
    private String topic;
    private String payload;
    /** NEW/SENT/RETRY/FAILED */
    private String status;
    private Integer retryCount;
    private LocalDateTime nextRetryAt;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}

