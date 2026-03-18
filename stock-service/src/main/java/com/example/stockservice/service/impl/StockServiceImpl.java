package com.example.stockservice.service.impl;

import com.example.stockservice.entity.Stock;
import com.example.stockservice.mapper.StockMapper;
import com.example.stockservice.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class StockServiceImpl implements StockService {
    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Cacheable(value = "stocks")
    public List<Stock> getAllStocks() {
        return stockMapper.findAll();
    }

    @Override
    @Cacheable(value = "stock", key = "#id")
    public Stock getStockById(Long id) {
        return stockMapper.findById(id);
    }

    @Override
    @Cacheable(value = "stock", key = "#productId")
    public Stock getStockByProductId(Long productId) {
        return stockMapper.findByProductId(productId);
    }

    @Override
    @CacheEvict(value = "stocks", allEntries = true)
    public Stock createStock(Stock stock) {
        stock.setUpdatedAt(LocalDateTime.now());
        stockMapper.insertStock(stock);
        return stock;
    }

    @Override
    @CacheEvict(value = {"stocks", "stock"}, key = "#stock.id")
    public Stock updateStock(Stock stock) {
        stock.setUpdatedAt(LocalDateTime.now());
        stockMapper.updateStock(stock);
        return stock;
    }

    @Override
    @CacheEvict(value = {"stocks", "stock"}, key = "#id")
    public void deleteStock(Long id) {
        stockMapper.deleteById(id);
    }

    @Override
    public boolean deductStock(Long productId, int amount) {
        String lockKey = "lock:stock:" + productId;
        String lockValue = Thread.currentThread().getName() + ":" + System.currentTimeMillis();

        // Try to acquire lock
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, 10, TimeUnit.SECONDS);
        if (locked == null || !locked) {
            return false; // Lock failed
        }

        try {
            Stock stock = stockMapper.findByProductId(productId);
            if (stock == null || stock.getQuantity() < amount) {
                return false;
            }
            stock.setQuantity(stock.getQuantity() - amount);
            stockMapper.updateStock(stock);
            // Evict cache
            redisTemplate.delete("stock::" + productId);
            redisTemplate.delete("stocks");
            return true;
        } finally {
            // Release lock
            String currentValue = (String) redisTemplate.opsForValue().get(lockKey);
            if (lockValue.equals(currentValue)) {
                redisTemplate.delete(lockKey);
            }
        }
    }
}
