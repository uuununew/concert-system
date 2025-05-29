package kr.hhplus.be.server.infrastructure.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.application.token.WaitingTokenPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaitingTokenKafkaProducer implements WaitingTokenPublisher {

    private static final String TOPIC = "waiting.token.request";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(WaitingTokenRequestMessage message) {
        try {
            String key = String.valueOf(message.concertId());
            String value = objectMapper.writeValueAsString(message);

            kafkaTemplate.send(TOPIC, key, value)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("[Kafka] 대기열 요청 발행 실패: {}", message, ex);
                        } else {
                            log.info("[Kafka] 대기열 요청 발행 성공: {}", message);
                        }
                    });
        } catch (Exception e) {
            log.error("[Kafka] 메시지 직렬화 실패", e);
        }
    }
}
