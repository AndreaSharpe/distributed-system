package com.example.orderservice.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InboxConsumedMapper {
    int insert(@Param("consumerGroup") String consumerGroup, @Param("eventId") String eventId);
}

