package com.example.stockservice.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Stock {
    private Long id;
    private Long productId;
    private Integer quantity;
    private LocalDateTime updatedAt;
}
