package kr.hhplus.be.server.infrastructure.reservation;

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
    private static final String TOPIC = "reservation.completed";

    public void send(ReservationCompletedEvent event) {
        String message = String.format("reservationId=%d, userId=%d, price=%s, reservedAt=%s",
                event.reservationId(), event.userId(), event.price(), event.reservedAt());
        kafkaTemplate.send(TOPIC, message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("❌ Kafka 메시지 발행 실패", ex);
                    } else {
                        log.info("✅ Kafka 메시지 발행 성공: {}", message);
                    }
                });
    }
}
