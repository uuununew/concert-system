package kr.hhplus.be.server.domain.concert.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * [유스케이스] 미결제 예약을 일정 시간 이후 자동 취소하는 로직
 * - 스케줄러에서 호출되며, 직접 트랜잭션 및 저장 처리 수행
 */
@Service
@RequiredArgsConstructor
public class ReservationScheduleService {

    private final ReservationRepository reservationRepository;

    /**
     * 결제되지 않고 일정 시간이 지난 예약을 모두 취소 처리
     */
    public void cancelReservationsBefore(LocalDateTime cutoffTime) {
        List<Reservation> targets = reservationRepository
                .findAllByStatusAndCreatedAtBefore(ReservationStatus.RESERVED, cutoffTime);

        for (Reservation reservation : targets) {
            Reservation canceled = reservation.cancel();
            reservationRepository.save(canceled);
        }
    }
}
