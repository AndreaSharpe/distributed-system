package com.example.orderservice.kafka;

import com.example.orderservice.dto.SeckillOrderKafkaMessage;
import com.example.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderKafkaConsumer {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${seckill.order-topic:seckill-order}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onSeckillOrder(String payload) {
        try {
            SeckillOrderKafkaMessage msg = objectMapper.readValue(payload, SeckillOrderKafkaMessage.class);
            orderService.consumeSeckillOrder(msg);
        } catch (Exception e) {
            throw new IllegalStateException("Kafka seckill consume failed", e);
        }
    }
}
