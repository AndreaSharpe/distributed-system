package com.example.orderservice.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order {
    private Long id;
    private Long userId;
    private Long productId;
    private Long stockId;
    private Integer amount;
    private String status;
    private LocalDateTime createdAt;
}
