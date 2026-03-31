package com.example.orderservice.mapper;

import com.example.orderservice.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {
    List<Order> findAll();
    Order findById(@Param("id") Long id);
    Order findByOrderNo(@Param("orderNo") Long orderNo);
    List<Order> findByUserId(@Param("userId") Long userId);
    int insertOrder(Order order);
    int updateOrder(Order order);
    int deleteById(@Param("id") Long id);

    int deleteByOrderNo(@Param("orderNo") Long orderNo);
}
