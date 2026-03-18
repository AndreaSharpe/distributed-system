package com.example.stockservice.service;

import com.example.stockservice.entity.Stock;

import java.util.List;

public interface StockService {
    List<Stock> getAllStocks();
    Stock getStockById(Long id);
    Stock getStockByProductId(Long productId);
    Stock createStock(Stock stock);
    Stock updateStock(Stock stock);
    void deleteStock(Long id);
    boolean deductStock(Long productId, int amount);
}
