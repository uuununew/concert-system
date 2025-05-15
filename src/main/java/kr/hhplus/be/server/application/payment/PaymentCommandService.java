package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.application.cash.CashCommandService;
import kr.hhplus.be.server.application.cash.UseCashCommand;
import kr.hhplus.be.server.application.concert.ConcertRankingService;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import kr.hhplus.be.server.infrastructure.concert.ConcertSeatCountRedisRepository;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCommandService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final CashCommandService cashCommandService;
    private final ConcertRankingService concertRankingService;
    private final ConcertSeatCountRedisRepository concertSeatCountRedisRepository;
    private final ConcertRepository concertRepository;

    /**
     * 결제 처리
     * - 예약 상태 확인 (RESERVED 상태여야 결제 가능)
     * - 예약 상태를 PAID로 변경
     * - 결제 정보 저장
     */
    @Transactional
    public Payment pay(CreatePaymentCommand command) {
        Reservation reservation = reservationRepository.findById(command.reservationId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "예약 정보를 찾을 수 없습니다."));

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "결제 가능한 상태가 아닙니다.");
        }

        // 금액 차감
        cashCommandService.use(new UseCashCommand(command.userId(), command.amount()));

        // 예약 상태 변경
        reservation.pay();
        reservationRepository.save(reservation);

        // 결제 객체 생성 및 결제 처리
        Payment payment = Payment.create(reservation, command.amount());
        payment.pay(); // 상태를 READY → PAID 로 변경
        paymentRepository.save(payment);

        // Redis 카운트 감소
        // 4. 좌석 수 감소 (Redis)
        Long concertId = reservation.getConcertSeat().getConcert().getId();
        long remain = concertSeatCountRedisRepository.decrementRemainCount(concertId);
        log.info("남은 좌석 수: {} (concertId={})", remain, concertId);

        // 5. 매진이면 랭킹 기록
        if (remain <= 0) {
            recordSoldOutRanking(concertId, reservation.getConcertSeat().getConcert().getConcertDateTime());
        }
        return payment;
    }

    /**
     * 결제 취소 처리
     */
    @Transactional
    public Payment cancel(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "결제 정보를 찾을 수 없습니다."));

        payment.cancel(); // 내부 상태 변경
        return paymentRepository.save(payment);
    }

    private void recordSoldOutRanking(Long concertId, LocalDateTime concertDateTime) {
        long soldOutAtMillis = System.currentTimeMillis();
        long openedAtMillis = concertDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
        long duration = soldOutAtMillis - openedAtMillis;

        // 매진 시간이 오픈 시간보다 빠른 경우 저장 생략
        if (duration < 0) {
            log.warn("❗ 매진 시간보다 오픈 시간이 늦어 랭킹 저장을 생략합니다. concertId={}, duration={}ms", concertId, duration);
            return;
        }
        concertRankingService.recordSoldOutTime(concertId, soldOutAtMillis, openedAtMillis);
    }
}
