package com.example.stockservice.controller;

import com.example.stockservice.entity.Stock;
import com.example.stockservice.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stocks")
public class StockController {
    @Autowired
    private StockService stockService;

    @GetMapping
    public Map<String, Object> getAllStocks() {
        List<Stock> stocks = stockService.getAllStocks();
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 0);
        resp.put("message", "success");
        resp.put("data", stocks);
        return resp;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getStockById(@PathVariable Long id) {
        Stock stock = stockService.getStockById(id);
        Map<String, Object> resp = new HashMap<>();
        if (stock == null) {
            resp.put("code", 404);
            resp.put("message", "Stock not found");
            return resp;
        }
        resp.put("code", 0);
        resp.put("message", "success");
        resp.put("data", stock);
        return resp;
    }

    @GetMapping("/product/{productId}")
    public Map<String, Object> getStockByProductId(@PathVariable Long productId) {
        Stock stock = stockService.getStockByProductId(productId);
        Map<String, Object> resp = new HashMap<>();
        if (stock == null) {
            resp.put("code", 404);
            resp.put("message", "Stock not found");
            return resp;
        }
        resp.put("code", 0);
        resp.put("message", "success");
        resp.put("data", stock);
        return resp;
    }

    @PostMapping
    public Map<String, Object> createStock(@RequestBody Stock stock) {
        Stock created = stockService.createStock(stock);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 0);
        resp.put("message", "success");
        resp.put("data", created);
        return resp;
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateStock(@PathVariable Long id, @RequestBody Stock stock) {
        stock.setId(id);
        Stock updated = stockService.updateStock(stock);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 0);
        resp.put("message", "success");
        resp.put("data", updated);
        return resp;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteStock(@PathVariable Long id) {
        stockService.deleteStock(id);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 0);
        resp.put("message", "success");
        return resp;
    }

    @PostMapping("/deduct")
    public Map<String, Object> deductStock(@RequestBody Map<String, Object> req) {
        Long productId = Long.valueOf(req.get("productId").toString());
        int amount = Integer.valueOf(req.get("amount").toString());
        boolean success = stockService.deductStock(productId, amount);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", success ? 0 : 1);
        resp.put("message", success ? "success" : "failed");
        return resp;
    }
}
