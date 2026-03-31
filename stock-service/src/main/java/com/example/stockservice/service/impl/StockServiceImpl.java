package com.example.stockservice.service.impl;

import com.example.stockservice.dto.SeckillReserveResult;
import com.example.stockservice.entity.Stock;
import com.example.stockservice.mapper.StockMapper;
import com.example.stockservice.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class StockServiceImpl implements StockService {
    private static final String SECKILL_STOCK_KEY = "stock:seckill:qty:";

    private static final DefaultRedisScript<Long> RESERVE_SCRIPT = new DefaultRedisScript<>();
    static {
        RESERVE_SCRIPT.setScriptText(
                "local k=KEYS[1] " +
                "local n=tonumber(ARGV[1]) " +
                "local v=redis.call('GET',k) " +
                "if v==false then return -1 end " +
                "local q=tonumber(v) " +
                "if q<n then return -2 end " +
                "redis.call('DECRBY',k,n) " +
                "return q-n"
        );
        RESERVE_SCRIPT.setResultType(Long.class);
    }

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

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

    @Override
    public SeckillReserveResult reserveSeckillStock(Long productId, int amount) {
        SeckillReserveResult r = new SeckillReserveResult();
        if (productId == null || amount <= 0) {
            return r;
        }
        ensureSeckillStockLoaded(productId);
        String key = SECKILL_STOCK_KEY + productId;
        Long res = stringRedisTemplate.execute(RESERVE_SCRIPT, Collections.singletonList(key), String.valueOf(amount));
        if (res == null || res == -1L) {
            return r;
        }
        if (res == -2L) {
            return r;
        }
        Stock stockRow = stockMapper.findByProductId(productId);
        if (stockRow == null) {
            stringRedisTemplate.opsForValue().increment(key, amount);
            return r;
        }
        r.setSuccess(true);
        r.setStockId(stockRow.getId());
        r.setRemaining(res.intValue());
        return r;
    }

    @Override
    public void releaseSeckillStock(Long productId, int amount) {
        if (productId == null || amount <= 0) return;
        String key = SECKILL_STOCK_KEY + productId;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
            stringRedisTemplate.opsForValue().increment(key, amount);
        }
        redisTemplate.delete("stock::" + productId);
        redisTemplate.delete("stocks");
    }

    @Override
    public boolean deductStockDbOnly(Long productId, int amount) {
        if (productId == null || amount <= 0) return false;
        int rows = stockMapper.decrementIfEnough(productId, amount);
        if (rows <= 0) return false;
        redisTemplate.delete("stock::" + productId);
        redisTemplate.delete("stocks");
        return true;
    }

    private void ensureSeckillStockLoaded(Long productId) {
        String key = SECKILL_STOCK_KEY + productId;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
            return;
        }
        Stock s = stockMapper.findByProductId(productId);
        if (s == null) {
            return;
        }
        stringRedisTemplate.opsForValue().setIfAbsent(key, String.valueOf(s.getQuantity()));
    }
}
