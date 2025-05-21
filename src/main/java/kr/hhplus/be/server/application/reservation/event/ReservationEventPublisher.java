package kr.hhplus.be.server.application.reservation.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ReservationEventPublisher {

    private final ApplicationEventPublisher publisher;

    public ReservationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishReservationCompleted(Long reservationId) {
        publisher.publishEvent(new ReservationCompletedEvent(reservationId));
    }
}
