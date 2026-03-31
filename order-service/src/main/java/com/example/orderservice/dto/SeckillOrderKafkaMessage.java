package com.example.orderservice.dto;

import lombok.Data;

@Data
public class SeckillOrderKafkaMessage {
    private Long orderNo;
    private Long userId;
    private Long productId;
    private Integer amount;
    private Long stockId;
}
