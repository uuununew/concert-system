package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.domain.reservation.ReservationScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationScheduleService reservationScheduleService;


    /**
     * [스케줄링]
     * 결제되지 않고 일정 시간이 지난 예약을 자동으로 취소
     * - 매 1분마다 실행됨
    **/
    @Scheduled(cron = "0 */1 * * * *")
    @Transactional
    public void cancelUnpaidReservations() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(10);
        reservationScheduleService.cancelReservationsBefore(cutoffTime);
        log.info("10분 이상 미결제 예약 자동 취소 스케줄 실행됨");
    }
}
