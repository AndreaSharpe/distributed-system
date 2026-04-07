package com.example.paymentservice.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Payment {
    private Long id;
    private Long paymentNo;
    private Long orderNo;
    private Long userId;
    private BigDecimal amount;
    /** INIT/SUCCESS/FAILED/CANCELLED */
    private String status;
    private String channel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

