package com.example.stockservice.mapper;

import com.example.stockservice.entity.StockReservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface StockReservationMapper {
    StockReservation findByOrderNo(@Param("orderNo") Long orderNo);
    int insert(StockReservation reservation);
    int updateStatus(@Param("orderNo") Long orderNo, @Param("fromStatus") String fromStatus, @Param("toStatus") String toStatus);
    int updateStatusAny(@Param("orderNo") Long orderNo, @Param("toStatus") String toStatus);
    List<StockReservation> findExpiredTry(@Param("now") LocalDateTime now, @Param("limit") int limit);
}

