package com.example.paymentservice.mapper;

import com.example.paymentservice.entity.OutboxEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OutboxEventMapper {
    int insert(OutboxEvent event);
    List<OutboxEvent> findDue(@Param("now") LocalDateTime now, @Param("limit") int limit);
    int markSent(@Param("id") Long id);
    int markRetry(@Param("id") Long id, @Param("retryCount") int retryCount, @Param("nextRetryAt") LocalDateTime nextRetryAt);
    int markFailed(@Param("id") Long id, @Param("retryCount") int retryCount);
}

