package com.example.stockservice.job;

import com.example.stockservice.entity.StockReservation;
import com.example.stockservice.mapper.StockReservationMapper;
import com.example.stockservice.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class StockReservationExpireJob {

    @Autowired
    private StockReservationMapper stockReservationMapper;

    @Autowired
    private StockService stockService;

    @Scheduled(fixedDelayString = "${stock.tcc.expire-job-delay-ms:3000}")
    public void expireTryReservations() {
        List<StockReservation> expired = stockReservationMapper.findExpiredTry(LocalDateTime.now(), 50);
        if (expired == null || expired.isEmpty()) return;
        for (StockReservation r : expired) {
            boolean cancelled = stockService.cancelByOrderNo(r.getOrderNo());
            if (cancelled) {
                stockReservationMapper.updateStatusAny(r.getOrderNo(), "EXPIRED");
            }
        }
    }
}

