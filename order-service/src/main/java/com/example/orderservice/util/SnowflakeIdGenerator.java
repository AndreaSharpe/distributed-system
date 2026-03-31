package com.example.orderservice.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 简化版雪花 ID（41 位时间戳 + 5 位机器 + 12 位序列），满足课程“Snowflake / Gene 类算法”要求。
 */
@Component
public class SnowflakeIdGenerator {

    private static final long EPOCH = 1609459200000L;
    private static final long WORKER_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_WORKER = ~(-1L << WORKER_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private final long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(@Value("${snowflake.worker-id:1}") long workerId) {
        if (workerId < 0 || workerId > MAX_WORKER) {
            throw new IllegalArgumentException("snowflake.worker-id must be 0..31");
        }
        this.workerId = workerId;
    }

    public synchronized long nextId() {
        long ts = System.currentTimeMillis();
        if (ts < lastTimestamp) {
            throw new IllegalStateException("Clock moved backwards");
        }
        if (ts == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                ts = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = ts;
        return ((ts - EPOCH) << (WORKER_BITS + SEQUENCE_BITS))
                | (workerId << SEQUENCE_BITS)
                | sequence;
    }

    private static long waitNextMillis(long last) {
        long ts = System.currentTimeMillis();
        while (ts <= last) {
            ts = System.currentTimeMillis();
        }
        return ts;
    }
}
