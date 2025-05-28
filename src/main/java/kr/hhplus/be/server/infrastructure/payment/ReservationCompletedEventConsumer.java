package kr.hhplus.be.server.infrastructure.payment;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReservationCompletedEventConsumer {

    @KafkaListener(
            topics = "reservation.completed",
            groupId = "reservation-consumer-group"
    )
    public void listen(ConsumerRecord<String, String> record) {
        String message = record.value();
        log.info("📥 예약 완료 메시지 수신: {}", message);
    }
}
