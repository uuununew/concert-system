package kr.hhplus.be.server.infrastructure.reservation;

import kr.hhplus.be.server.domain.reservation.ReservationEventPublisher;
import kr.hhplus.be.server.domain.reservation.ReservationCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
@RequiredArgsConstructor
public class SpringReservationEventPublisher implements ReservationEventPublisher{

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publishReservationCompleted(Long reservationId, LocalDateTime reservedAt) {
        eventPublisher.publishEvent(new ReservationCompletedEvent(reservationId, reservedAt));
    }
}
