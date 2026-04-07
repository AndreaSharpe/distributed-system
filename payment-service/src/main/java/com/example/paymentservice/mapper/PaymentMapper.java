package com.example.paymentservice.mapper;

import com.example.paymentservice.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PaymentMapper {
    Payment findByPaymentNo(@Param("paymentNo") Long paymentNo);
    int insert(Payment payment);
    int updateStatus(@Param("paymentNo") Long paymentNo, @Param("fromStatus") String fromStatus, @Param("toStatus") String toStatus);
    int updateStatusAny(@Param("paymentNo") Long paymentNo, @Param("toStatus") String toStatus);
}

