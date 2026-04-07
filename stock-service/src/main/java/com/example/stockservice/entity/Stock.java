package com.example.stockservice.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Stock implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long productId;
    private Integer quantity;
    private LocalDateTime updatedAt;
}
