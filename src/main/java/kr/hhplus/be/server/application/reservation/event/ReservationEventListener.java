package kr.hhplus.be.server.application.reservation.event;

import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventListener {

    private final ReservationRepository reservationRepository;
    private final ReservationInfoSender reservationInfoSender;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReservationCompleted(ReservationCompletedEvent event) {
        Long reservationId = event.reservationId();

        reservationRepository.findById(reservationId)
                .ifPresentOrElse(
                        reservation -> {
                            log.info("[이벤트] 예약 정보 전송 시작: {}", reservation.getId());
                            try {
                                reservationInfoSender.send(reservation);
                            } catch (Exception e) {
                                log.error("예약 정보 전송 실패 (ID: {}): {}", reservation.getId(), e.getMessage(), e);
                            }
                        },
                        () -> log.warn("[이벤트] 예약 정보 없음: {}", reservationId)
                );
    }
}
