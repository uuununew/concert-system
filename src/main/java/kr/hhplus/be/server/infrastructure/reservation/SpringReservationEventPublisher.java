package kr.hhplus.be.server.infrastructure.reservation;

import kr.hhplus.be.server.domain.reservation.ReservationEventPublisher;
import kr.hhplus.be.server.domain.reservation.ReservationCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringReservationEventPublisher implements ReservationEventPublisher{

    private final ApplicationEventPublisher eventPublisher;
    private final KafkaReservationEventProducer kafkaReservationEventProducer;

    @Override
    public void publishReservationCompleted(Long reservationId, Long userId, BigDecimal price, LocalDateTime reservedAt) {
        eventPublisher.publishEvent(new ReservationCompletedEvent(reservationId, userId, price, reservedAt));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAfterCommit(ReservationCompletedEvent event) {
        log.info("트랜잭션 커밋 후 Kafka 발행 시작: {}", event);
        kafkaReservationEventProducer.send(event);
    }
}
