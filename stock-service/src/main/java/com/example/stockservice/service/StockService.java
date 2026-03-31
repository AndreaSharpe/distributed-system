package com.example.stockservice.service;

import com.example.stockservice.dto.SeckillReserveResult;
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

    /** Redis 秒杀库存预扣（削峰用缓存库存），成功后再由 Kafka 消费者落库扣减 */
    SeckillReserveResult reserveSeckillStock(Long productId, int amount);

    /** 预扣回滚（发送 Kafka 失败或 DB 扣减失败时调用） */
    void releaseSeckillStock(Long productId, int amount);

    /** 仅数据库扣减（Kafka 消费者确认下单时调用，与 Redis 预扣对齐） */
    boolean deductStockDbOnly(Long productId, int amount);
}
