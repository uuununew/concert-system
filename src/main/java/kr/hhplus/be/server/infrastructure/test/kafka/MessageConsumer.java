package kr.hhplus.be.server.infrastructure.test.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageConsumer {
    @KafkaListener(topics = "test-topic", groupId = "test-group")
    public void listen(String message) {
        log.info("📩 Received message from Kafka: {}", message);
    }
}
