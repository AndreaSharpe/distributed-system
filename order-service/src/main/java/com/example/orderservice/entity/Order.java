package com.example.orderservice.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 业务订单号（雪花算法等） */
    private Long orderNo;
    private Long userId;
    private Long productId;
    private Long stockId;
    private Integer amount;
    private String status;
    private LocalDateTime createdAt;
}
