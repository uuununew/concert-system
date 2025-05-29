package kr.hhplus.be.server.infrastructure.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.domain.reservation.ReservationCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaReservationEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "reservation.completed";

    public void send(ReservationCompletedEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            String key = String.valueOf(event.reservationId());

            kafkaTemplate.send(TOPIC, key, json)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Kafka 메시지 발행 실패", ex);
                        } else {
                            log.info("Kafka 메시지 발행 성공 (key={}): {}", key, json);
                        }
                    });

        } catch (Exception e) {
            log.error("Kafka 메시지 직렬화 실패", e);
        }
    }
}
