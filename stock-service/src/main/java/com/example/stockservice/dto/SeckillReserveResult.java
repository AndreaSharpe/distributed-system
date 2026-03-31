package com.example.stockservice.dto;

import lombok.Data;

@Data
public class SeckillReserveResult {
    private boolean success;
    private Long stockId;
    private int remaining;
}
