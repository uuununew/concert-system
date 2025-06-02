package kr.hhplus.be.server.infrastructure.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.application.token.TokenCommandService;
import kr.hhplus.be.server.infrastructure.token.WaitingTokenRequestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaitingTokenKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final TokenCommandService tokenCommandService;

    @KafkaListener(
            topics = "waiting.token.request",
            groupId = "token-consumer-group"
    )
    public void listen(ConsumerRecord<String, String> record) {
        try {
            String message = record.value();
            log.info("[Kafka] 대기열 요청 수신: {}", message);

            WaitingTokenRequestMessage payload = objectMapper.readValue(
                    message, WaitingTokenRequestMessage.class
            );

            tokenCommandService.processWaitingToken(
                    payload.userId(),
                    payload.concertId(),
                    payload.requestAt()
            );
        } catch (Exception e) {
            log.error("[Kafka] 대기열 요청 처리 실패", e);
        }
    }
}
