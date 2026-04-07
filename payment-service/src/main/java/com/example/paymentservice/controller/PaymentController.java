package com.example.paymentservice.controller;

import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody Map<String, Object> req) {
        Long orderNo = Long.valueOf(req.get("orderNo").toString());
        Long userId = Long.valueOf(req.get("userId").toString());
        BigDecimal amount = req.get("amount") == null ? BigDecimal.ZERO : new BigDecimal(req.get("amount").toString());
        String channel = req.get("channel") == null ? "mock" : req.get("channel").toString();
        Payment p = paymentService.createPayment(orderNo, userId, amount, channel);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 0);
        resp.put("message", "success");
        resp.put("data", p);
        return resp;
    }

    @GetMapping("/{paymentNo}")
    public Map<String, Object> get(@PathVariable Long paymentNo) {
        Payment p = paymentService.getByPaymentNo(paymentNo);
        Map<String, Object> resp = new HashMap<>();
        if (p == null) {
            resp.put("code", 404);
            resp.put("message", "not found");
            return resp;
        }
        resp.put("code", 0);
        resp.put("message", "success");
        resp.put("data", p);
        return resp;
    }

    @PostMapping("/callback")
    public Map<String, Object> callback(@RequestBody Map<String, Object> req) {
        Long paymentNo = Long.valueOf(req.get("paymentNo").toString());
        boolean success = Boolean.parseBoolean(req.get("success").toString());
        Payment p = paymentService.callback(paymentNo, success);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 0);
        resp.put("message", "success");
        resp.put("data", p);
        return resp;
    }
}

