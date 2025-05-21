package kr.hhplus.be.server.infrastructure.reservation;

import kr.hhplus.be.server.domain.reservation.ReservationInfoSender;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpReservationInfoSender implements ReservationInfoSender {

    private final ReservationRepository reservationRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendReservationInfo(Long reservationId, LocalDateTime reservedAt) {
        reservationRepository.findById(reservationId).ifPresentOrElse(reservation -> {
            try {
                Map<String, Object> payload = Map.of(
                        "reservationId", reservation.getId(),
                        "userId", reservation.getUserId(),
                        "price", reservation.getPrice(),
                        "reservedAt", reservedAt.toString()
                );

                ResponseEntity<Void> response = restTemplate.postForEntity(
                        "http://mock-api.local/reservations", payload, Void.class
                );
                log.info("예약 정보 전송 완료, 응답 코드: {}", response.getStatusCode());
            } catch (Exception e) {
                log.error("예약 정보 전송 실패: {}", e.getMessage(), e);
            }
        }, () -> {
            log.warn("예약 ID {}에 해당하는 예약을 찾을 수 없습니다", reservationId);
        });
    }
}
