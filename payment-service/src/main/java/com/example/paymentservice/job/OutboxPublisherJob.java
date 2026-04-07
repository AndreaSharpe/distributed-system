package com.example.paymentservice.job;

import com.example.paymentservice.entity.OutboxEvent;
import com.example.paymentservice.mapper.OutboxEventMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OutboxPublisherJob {

    @Autowired
    private OutboxEventMapper outboxEventMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${outbox.publisher.delay-ms:1000}")
    public void publishDue() {
        List<OutboxEvent> due = outboxEventMapper.findDue(LocalDateTime.now(), 50);
        if (due == null || due.isEmpty()) return;

        for (OutboxEvent e : due) {
            try {
                kafkaTemplate.send(e.getTopic(), e.getAggregateId(), e.getPayload()).get();
                outboxEventMapper.markSent(e.getId());
            } catch (Exception ex) {
                int retry = e.getRetryCount() == null ? 1 : (e.getRetryCount() + 1);
                if (retry >= 12) {
                    outboxEventMapper.markFailed(e.getId(), retry);
                    continue;
                }
                int seconds = Math.min(300, (int) Math.pow(2, Math.min(8, retry)));
                outboxEventMapper.markRetry(e.getId(), retry, LocalDateTime.now().plusSeconds(seconds));
            }
        }
    }
}

