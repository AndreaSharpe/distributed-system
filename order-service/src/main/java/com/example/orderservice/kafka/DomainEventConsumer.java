package com.example.orderservice.kafka;

import com.example.orderservice.dto.DomainEvent;
import com.example.orderservice.mapper.InboxConsumedMapper;
import com.example.orderservice.service.OrderPaymentOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DomainEventConsumer {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InboxConsumedMapper inboxConsumedMapper;

    @Autowired
    private OrderPaymentOrchestrator orchestrator;

    @Value("${spring.kafka.consumer.group-id:order-service}")
    private String consumerGroup;

    @KafkaListener(topics = "${events.topic:domain-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void onEvent(String payload) {
        try {
            DomainEvent evt = objectMapper.readValue(payload, DomainEvent.class);
            orchestrator.handle(evt);
            if (evt.getEventId() != null && !evt.getEventId().isEmpty()) {
                try {
                    inboxConsumedMapper.insert(consumerGroup, evt.getEventId());
                } catch (DuplicateKeyException ignore) {
                    return;
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("domain-events consume failed", e);
        }
    }
}

