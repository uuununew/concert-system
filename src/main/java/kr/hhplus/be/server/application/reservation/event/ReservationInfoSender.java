package kr.hhplus.be.server.application.reservation.event;

import kr.hhplus.be.server.domain.reservation.Reservation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReservationInfoSender {

    public void send(Reservation reservation) {
        // 실제 외부 시스템 호출 대신 로그
        log.info("[데이터 플랫폼] 예약 정보 전송 완료 → 사용자 ID: {}, 좌석 ID: {}, 금액: {}",
                reservation.getUserId(),
                reservation.getConcertSeat().getId(),
                reservation.getPrice()
        );
    }
}
