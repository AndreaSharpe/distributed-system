package com.example.stockservice.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StockReservation {
    private Long id;
    private Long orderNo;
    private Long productId;
    private Integer amount;
    /** TRY/CONFIRMED/CANCELLED/EXPIRED */
    private String status;
    private LocalDateTime expireAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

