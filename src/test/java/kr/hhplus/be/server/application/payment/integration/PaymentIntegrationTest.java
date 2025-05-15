package kr.hhplus.be.server.application.payment.integration;

import jakarta.persistence.EntityManager;
import kr.hhplus.be.server.application.cash.CashService;
import kr.hhplus.be.server.application.cash.ChargeCashCommand;
import kr.hhplus.be.server.application.payment.CreatePaymentCommand;
import kr.hhplus.be.server.application.payment.PaymentCommandService;
import kr.hhplus.be.server.application.payment.PaymentQueryService;
import kr.hhplus.be.server.application.reservation.CreateReservationCommand;
import kr.hhplus.be.server.application.reservation.ReservationCommandService;
import kr.hhplus.be.server.application.token.TokenCommandService;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private CashService cashService;

    @Autowired
    private TokenCommandService tokenCommandService;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void clearRedis() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    private Reservation createReservation() {
        Concert concert = concertRepository.save(
                Concert.create("Test Concert", 1, ConcertStatus.READY, LocalDateTime.now().plusDays(1)));

        ConcertSeat seat = concertSeatRepository.save(
                ConcertSeat.of(concert, "A1", "1층", "A", "VIP", BigDecimal.valueOf(10000)));

        QueueToken token = new QueueToken(1L, LocalDateTime.now());
        token.activate(); // 상태를 WAITING -> ACTIVE 로 전환
        tokenRepository.save(token);

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

        // 유저 1, 유저 2 각각 대기열 발급
        QueueToken token1 = new QueueToken(1L, LocalDateTime.now());
        token1.activate();
        tokenRepository.save(token1);

        QueueToken token2 = new QueueToken(2L, LocalDateTime.now());
        token2.activate();
        tokenRepository.save(token2);

        // 토큰 활성화
        tokenCommandService.activate(1L);
        tokenCommandService.activate(2L);

        // 캐시 충전
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

    @Test
    @DisplayName("매진되면 랭킹 기록이 수행된다")
    void pay_lastSeat_triggersRankingRecord() {
        // given
        Concert concert = concertRepository.save(
                Concert.create("매진 콘서트", 1, ConcertStatus.READY, LocalDateTime.now().plusMinutes(5))
        );
        ConcertSeat seat = concertSeatRepository.save(
                ConcertSeat.of(concert, "Z9", "3층", "Z", "R", BigDecimal.valueOf(30000))
        );

        // 좌석 수 1개만 등록한 상태에서 Redis에 좌석 카운트 = 1 로 세팅
        String remainKey = "concert-seat-remain:" + concert.getId();
        redisTemplate.opsForValue().set(remainKey, "1");

        QueueToken token = new QueueToken(1L, LocalDateTime.now());
        token.activate();
        tokenRepository.save(token);

        cashService.charge(new ChargeCashCommand(1L, BigDecimal.valueOf(50000)));

        Reservation reservation = reservationCommandService.reserve(
                new CreateReservationCommand(1L, seat.getId(), seat.getPrice())
        );

        // when
        Payment payment = paymentService.pay(
                new CreatePaymentCommand(1L, reservation.getId(), seat.getPrice())
        );

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);

        String rankingKey = "concert-soldout-ranking:" + LocalDate.now();
        Set<String> rankingMembers = redisTemplate.opsForZSet().range(rankingKey, 0, -1);
        assertThat(rankingMembers).contains(String.valueOf(concert.getId()));
    }
}
