package com.example.orderservice.kafka;

import com.example.orderservice.dto.SeckillOrderKafkaMessage;
import com.example.orderservice.mapper.InboxConsumedMapper;
import com.example.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderKafkaConsumer {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InboxConsumedMapper inboxConsumedMapper;

    @Value("${spring.kafka.consumer.group-id:order-service}")
    private String consumerGroup;

    @KafkaListener(
            topics = "${seckill.order-topic:seckill-order}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onSeckillOrder(String payload) {
        try {
            SeckillOrderKafkaMessage msg = objectMapper.readValue(payload, SeckillOrderKafkaMessage.class);
            orderService.consumeSeckillOrder(msg);
            if (msg.getEventId() != null && !msg.getEventId().isEmpty()) {
                try {
                    inboxConsumedMapper.insert(consumerGroup, msg.getEventId());
                } catch (DuplicateKeyException ignore) {
                    return;
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Kafka seckill consume failed", e);
        }
    }
}
