package com.example.paymentservice.service;

import com.example.paymentservice.entity.Payment;

import java.math.BigDecimal;

public interface PaymentService {
    Payment createPayment(Long orderNo, Long userId, BigDecimal amount, String channel);
    Payment getByPaymentNo(Long paymentNo);
    Payment callback(Long paymentNo, boolean success);
}

