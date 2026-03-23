package com.example.productservice.service.impl;

import com.example.productservice.document.ProductDocument;
import com.example.productservice.entity.Product;
import com.example.productservice.mapper.ProductMapper;
import com.example.productservice.repository.ProductSearchRepository;
import com.example.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
public class ProductServiceImpl implements ProductService {
    private static final String PRODUCT_DETAIL_KEY_PREFIX = "product:detail:";
    private static final String PRODUCT_DETAIL_LOCK_PREFIX = "lock:product:detail:";
    private static final String NULL_SENTINEL = "__NULL__";
    private static final long NULL_TTL_SECONDS = 60;
    private static final long BASE_TTL_SECONDS = 300;
    private static final long JITTER_SECONDS = 60;
    private static final long LOCK_TTL_SECONDS = 10;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ProductSearchRepository productSearchRepository;

    @Override
    @Cacheable(value = "products")
    public List<Product> getAllProducts() {
        return productMapper.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        if (id == null) return null;

        String key = PRODUCT_DETAIL_KEY_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            if (NULL_SENTINEL.equals(cached)) return null;
            return (Product) cached;
        }

        String lockKey = PRODUCT_DETAIL_LOCK_PREFIX + id;
        String lockVal = UUID.randomUUID().toString();

        for (int i = 0; i < 6; i++) {
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, lockVal, LOCK_TTL_SECONDS, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(locked)) {
                try {
                    // Double-check after acquiring lock
                    Object cachedAfterLock = redisTemplate.opsForValue().get(key);
                    if (cachedAfterLock != null) {
                        if (NULL_SENTINEL.equals(cachedAfterLock)) return null;
                        return (Product) cachedAfterLock;
                    }

                    Product fromDb = productMapper.findById(id);
                    if (fromDb == null) {
                        redisTemplate.opsForValue().set(key, NULL_SENTINEL, NULL_TTL_SECONDS, TimeUnit.SECONDS);
                        return null;
                    }

                    long ttl = BASE_TTL_SECONDS + ThreadLocalRandom.current().nextLong(0, JITTER_SECONDS + 1);
                    redisTemplate.opsForValue().set(key, fromDb, ttl, TimeUnit.SECONDS);
                    return fromDb;
                } finally {
                    Object current = redisTemplate.opsForValue().get(lockKey);
                    if (lockVal.equals(current)) {
                        redisTemplate.delete(lockKey);
                    }
                }
            }

            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            Object cachedRetry = redisTemplate.opsForValue().get(key);
            if (cachedRetry != null) {
                if (NULL_SENTINEL.equals(cachedRetry)) return null;
                return (Product) cachedRetry;
            }
        }

        // Fallback: DB read (avoid total failure under extreme contention)
        Product fallback = productMapper.findById(id);
        if (fallback == null) {
            redisTemplate.opsForValue().set(key, NULL_SENTINEL, NULL_TTL_SECONDS, TimeUnit.SECONDS);
            return null;
        }
        long ttl = BASE_TTL_SECONDS + ThreadLocalRandom.current().nextLong(0, JITTER_SECONDS + 1);
        redisTemplate.opsForValue().set(key, fallback, ttl, TimeUnit.SECONDS);
        return fallback;
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }
        try {
            List<ProductDocument> docs = productSearchRepository.findByNameContainingOrDescriptionContaining(keyword, keyword);
            List<Product> result = new ArrayList<>();
            for (ProductDocument doc : docs) {
                Product p = new Product();
                p.setId(doc.getId());
                p.setName(doc.getName());
                p.setDescription(doc.getDescription());
                p.setPrice(doc.getPrice());
                p.setStock(doc.getStock());
                p.setCreatedAt(doc.getCreatedAt());
                result.add(p);
            }
            return result;
        } catch (Exception e) {
            // Fallback to DB fuzzy matching if Elasticsearch is temporarily unavailable.
            return productMapper.findAll();
        }
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public Product createProduct(Product product) {
        product.setCreatedAt(LocalDateTime.now());
        productMapper.insertProduct(product);
        syncToSearch(product);
        return product;
    }

    @Override
    @CacheEvict(value = {"products", "product"}, key = "#product.id")
    public Product updateProduct(Product product) {
        productMapper.updateProduct(product);
        if (product != null && product.getId() != null) {
            redisTemplate.delete(PRODUCT_DETAIL_KEY_PREFIX + product.getId());
            syncToSearch(productMapper.findById(product.getId()));
        }
        return product;
    }

    @Override
    @CacheEvict(value = {"products", "product"}, key = "#id")
    public void deleteProduct(Long id) {
        productMapper.deleteById(id);
        if (id != null) {
            redisTemplate.delete(PRODUCT_DETAIL_KEY_PREFIX + id);
            removeFromSearch(id);
        }
    }

    private void syncToSearch(Product product) {
        if (product == null || product.getId() == null) return;
        try {
            ProductDocument doc = new ProductDocument();
            doc.setId(product.getId());
            doc.setName(product.getName());
            doc.setDescription(product.getDescription());
            doc.setPrice(product.getPrice());
            doc.setStock(product.getStock());
            doc.setCreatedAt(product.getCreatedAt());
            productSearchRepository.save(doc);
        } catch (Exception ignored) {
            // Keep core transaction unaffected if search index write fails.
        }
    }

    private void removeFromSearch(Long id) {
        try {
            productSearchRepository.deleteById(id);
        } catch (Exception ignored) {
            // Keep core transaction unaffected if search index delete fails.
        }
    }
}
