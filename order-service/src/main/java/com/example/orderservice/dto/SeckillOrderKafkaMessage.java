package com.example.orderservice.dto;

import lombok.Data;

@Data
public class SeckillOrderKafkaMessage {
    /** 全链路事件幂等ID（用于 Inbox 去重） */
    private String eventId;
    private Long orderNo;
    private Long userId;
    private Long productId;
    private Integer amount;
    private Long stockId;
}
