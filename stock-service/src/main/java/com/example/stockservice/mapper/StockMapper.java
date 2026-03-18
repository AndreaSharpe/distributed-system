package com.example.stockservice.mapper;

import com.example.stockservice.entity.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StockMapper {
    List<Stock> findAll();
    Stock findById(@Param("id") Long id);
    Stock findByProductId(@Param("productId") Long productId);
    int insertStock(Stock stock);
    int updateStock(Stock stock);
    int deleteById(@Param("id") Long id);
}
