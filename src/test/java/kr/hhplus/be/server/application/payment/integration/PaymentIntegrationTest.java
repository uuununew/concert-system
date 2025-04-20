package kr.hhplus.be.server.application.payment.integration;

import kr.hhplus.be.server.application.cash.CashService;
import kr.hhplus.be.server.application.cash.ChargeCashCommand;
import kr.hhplus.be.server.application.payment.CreatePaymentCommand;
import kr.hhplus.be.server.application.payment.PaymentCommandService;
import kr.hhplus.be.server.application.payment.PaymentQueryService;
import kr.hhplus.be.server.application.reservation.CreateReservationCommand;
import kr.hhplus.be.server.application.reservation.ReservationCommandService;
import kr.hhplus.be.server.config.TestContainerConfig;
import kr.hhplus.be.server.domain.concert.*;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import kr.hhplus.be.server.domain.payment.PaymentStatus;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Testcontainers
public class PaymentIntegrationTest extends TestContainerConfig {

    @Autowired
    private PaymentCommandService paymentService;

    @Autowired
    private PaymentQueryService paymentQueryService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReservationCommandService reservationCommandService;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private CashService cashService;

    private Reservation createReservation() {
        Concert concert = concertRepository.save(
                Concert.create("Test Concert", 1, ConcertStatus.READY, LocalDateTime.now().plusDays(1)));

        ConcertSeat seat = concertSeatRepository.save(
                ConcertSeat.of(concert, "A1", "1층", "A", "VIP", BigDecimal.valueOf(10000)));

        tokenRepository.save(new QueueToken(1L, LocalDateTime.now()));
        cashService.charge(new ChargeCashCommand(1L, BigDecimal.valueOf(100000)));

        return reservationCommandService.reserve(
                new CreateReservationCommand(1L, seat.getId(), BigDecimal.valueOf(10000)));
    }

    @Test
    @DisplayName("결제를 성공적으로 생성할 수 있다")
    void create_payment_success() {
        // given
        Reservation reservation = createReservation();
        CreatePaymentCommand command = new CreatePaymentCommand(1L, reservation.getId(), BigDecimal.valueOf(5000));

        // when
        Payment payment = paymentService.pay(command);

        // then
        assertThat(payment).isNotNull();
        assertThat(payment.getReservation().getUserId()).isEqualTo(1L);
        assertThat(payment.getAmount()).isEqualTo(BigDecimal.valueOf(5000));
    }

    @Test
    @DisplayName("유저 ID로 결제 내역을 조회할 수 있다")
    void get_payment_history_by_userId() {
        // given
        Concert concert = concertRepository.save(
                Concert.create("Payment Test Concert", 1, ConcertStatus.READY, LocalDateTime.now().plusDays(1)));

        ConcertSeat seat1 = concertSeatRepository.save(
                ConcertSeat.of(concert, "B1", "1층", "B", "R", BigDecimal.valueOf(10000)));

        ConcertSeat seat2 = concertSeatRepository.save(
                ConcertSeat.of(concert, "B2", "1층", "B", "R", BigDecimal.valueOf(10000)));

        // 유저 1, 유저 2 각각 대기열 발급 및 캐시 충전
        tokenRepository.save(new QueueToken(1L, LocalDateTime.now()));
        tokenRepository.save(new QueueToken(2L, LocalDateTime.now()));
        cashService.charge(new ChargeCashCommand(1L, BigDecimal.valueOf(100000)));
        cashService.charge(new ChargeCashCommand(2L, BigDecimal.valueOf(100000)));

        // 유저 1, 2 각각 예약
        Reservation reservation1 = reservationCommandService.reserve(
                new CreateReservationCommand(1L, seat1.getId(), BigDecimal.valueOf(10000))
        );
        Reservation reservation2 = reservationCommandService.reserve(
                new CreateReservationCommand(1L, seat2.getId(), BigDecimal.valueOf(10000))
        );

        // 유저 1이 두 건 결제
        paymentService.pay(new CreatePaymentCommand(1L, reservation1.getId(), BigDecimal.valueOf(3000)));
        paymentService.pay(new CreatePaymentCommand(1L, reservation2.getId(), BigDecimal.valueOf(7000)));

        // when
        List<Payment> payments = paymentQueryService.findByUserId(1L);

        // then
        assertThat(payments).hasSize(2);
        assertThat(payments).allMatch(p -> p.getReservation().getUserId().equals(1L));
    }

    @Test
    @DisplayName("결제 시 잔액 부족으로 예외가 발생한다")
    void payment_fail_due_to_insufficient_cash() {
        // given
        Reservation reservation = createReservation();
        CreatePaymentCommand command = new CreatePaymentCommand(1L, reservation.getId(), BigDecimal.valueOf(1000000));

        // when // then
        assertThatThrownBy(() -> paymentService.pay(command))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    @DisplayName("결제를 취소할 수 있다")
    void cancel_payment_success() {
        // given
        Reservation reservation = createReservation();
        Payment payment = paymentService.pay(new CreatePaymentCommand(1L, reservation.getId(), BigDecimal.valueOf(3000)));

        // when
        paymentService.cancel(payment.getId());

        // then
        Payment canceled = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(canceled.getStatus()).isEqualTo(PaymentStatus.CANCELED);
    }

    @Test
    @DisplayName("결제 금액이 0 이하일 경우 예외가 발생한다")
    void pay_withInvalidAmount_throwsException() {
        // given
        Reservation reservation = createReservation();

        // when // then
        assertThatThrownBy(() ->
                new CreatePaymentCommand(1L, reservation.getId(), BigDecimal.ZERO))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("price는 필수이며 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("결제 완료 시 상태가 PAID로 설정된다")
    void pay_success_setsStatusToPaid() {
        // given
        Reservation reservation = createReservation();
        CreatePaymentCommand command = new CreatePaymentCommand(1L, reservation.getId(), BigDecimal.valueOf(3000));

        // when
        Payment payment = paymentService.pay(command);

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
    }
}
