package kr.hhplus.be.server.infrastructure.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.application.payment.PaymentCommandService;
import kr.hhplus.be.server.domain.reservation.ReservationCompletedEvent;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationCompletedEventConsumer {

    private final ObjectMapper objectMapper;
    private final ReservationRepository reservationRepository;

    @KafkaListener(
            topics = "reservation.completed",
            groupId = "reservation-consumer-group"
    )
    public void listen(String message) {
        try {
            log.info("예약 완료 메시지 수신: {}", message);

            ReservationCompletedEvent event = objectMapper.readValue(
                    message, ReservationCompletedEvent.class
            );

            reservationRepository.findById(event.reservationId())
                    .ifPresentOrElse(
                            reservation -> log.info("예약 정보 전송 완료: userId={}, price={}, reservedAt={}",
                                    reservation.getUserId(), reservation.getPrice(), event.reservedAt()),
                            () -> log.warn("예약 ID {}에 해당하는 예약을 찾을 수 없습니다", event.reservationId())
                    );
        } catch (Exception e) {
            log.error("예약 완료 이벤트 처리 중 예외 발생", e);
        }
    }
}

