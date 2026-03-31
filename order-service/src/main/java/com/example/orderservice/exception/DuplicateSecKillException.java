package com.example.orderservice.exception;

/**
 * 同一用户同一商品仅允许一次成功秒杀（幂等）。
 */
public class DuplicateSecKillException extends RuntimeException {

    private final Long orderNo;

    public DuplicateSecKillException(Long orderNo) {
        super("duplicate seckill for same user and product");
        this.orderNo = orderNo;
    }

    public Long getOrderNo() {
        return orderNo;
    }
}
