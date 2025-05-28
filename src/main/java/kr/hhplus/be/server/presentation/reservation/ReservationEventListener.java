package kr.hhplus.be.server.presentation.reservation;

import kr.hhplus.be.server.domain.reservation.ReservationCompletedEvent;
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

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReservationCompleted(ReservationCompletedEvent event) {
        log.info("예약 완료 이벤트 수신: reservationId={}, reservedAt={}",
                event.reservationId(), event.reservedAt());

        // 예약 정보 조회 후 로그 출력 (또는 외부 전송 등 처리 가능)
        reservationRepository.findById(event.reservationId())
                .ifPresentOrElse(
                        reservation -> log.info("예약 정보 전송 완료: userId={}, price={}, reservedAt={}",
                                reservation.getUserId(), reservation.getPrice(), event.reservedAt()),
                        () -> log.warn("예약 ID {}에 해당하는 예약을 찾을 수 없습니다", event.reservationId())
                );
    }
}
