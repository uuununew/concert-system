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
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.token.TokenStatus;
import kr.hhplus.be.server.support.exception.CustomException;
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
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Testcontainers(disabledWithoutDocker = true)
public class PaymentIntegrationTest extends TestContainerConfig {

    @Autowired
    private PaymentCommandService paymentCommandService;

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

    private Long userId = 1L;
    private Long seatId;

    @BeforeEach
    void setUp() throws InterruptedException {
        clearRedisQueue();
        Reservation reservation = createReservation(userId);
        seatId = reservation.getConcertSeat().getId();
        entityManager.flush();
    }

    private Reservation createReservation(Long userId) throws InterruptedException {
        Concert concert = concertRepository.save(
                Concert.create("통합 테스트용 콘서트", 1, ConcertStatus.OPENED, LocalDateTime.now().plusMinutes(5))
        );

        ConcertSeat seat = concertSeatRepository.save(
                ConcertSeat.of(concert, "A1", "1층", "A", "VIP", BigDecimal.valueOf(10000))
        );

        String tokenId = tokenCommandService.issue(userId);
        Thread.sleep(100); // Redis 반영 대기
        waitUntilTokenIsFirst(tokenId);
        tokenCommandService.activateEligibleTokens(1000);

        cashService.charge(new ChargeCashCommand(userId, BigDecimal.valueOf(20000)));

        return reservationCommandService.reserve(
                new CreateReservationCommand(tokenId, userId, seat.getId(), seat.getPrice())
        );
    }

    @Test
    @DisplayName("결제를 성공적으로 생성할 수 있다")
    void create_payment_success() {
        // given
        CreatePaymentCommand command = new CreatePaymentCommand(userId, seatId, BigDecimal.valueOf(10000));

        // when
        paymentCommandService.pay(command);

        // then
        Optional<Payment> result = paymentRepository.findByUserId(userId).stream()
                .filter(p -> p.getReservation().getConcertSeat().getId().equals(seatId))
                .findFirst();

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    @DisplayName("유저 ID로 결제 내역을 조회할 수 있다")
    void get_payment_history_by_userId() throws InterruptedException {
        // given
        Reservation r1 = createReservation(userId);
        Reservation r2 = createReservation(userId);

        paymentCommandService.pay(new CreatePaymentCommand(1L, r1.getId(), BigDecimal.valueOf(3000)));
        paymentCommandService.pay(new CreatePaymentCommand(1L, r2.getId(), BigDecimal.valueOf(7000)));

        List<Payment> payments = paymentQueryService.findByUserId(1L);

        assertThat(payments).hasSize(2);
        assertThat(payments).allMatch(p -> p.getReservation().getUserId().equals(1L));
    }

    @Test
    @DisplayName("중복 결제 시 예외 발생")
    void pay_duplicate_shouldFail() {
        // given
        CreatePaymentCommand command = new CreatePaymentCommand(userId, seatId, BigDecimal.valueOf(10000));
        paymentCommandService.pay(command);

        // expect
        assertThatThrownBy(() -> paymentCommandService.pay(command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이미 결제된 예약입니다");
    }

    @Test
    @DisplayName("결제를 취소할 수 있다")
    void cancel_payment_success() throws InterruptedException {
        // given
        Reservation reservation = createReservation(1L);
        Payment payment = paymentCommandService.pay(new CreatePaymentCommand(userId, reservation.getId(), BigDecimal.valueOf(3000)));

        // when
        paymentCommandService.cancel(payment.getId());

        // then
        Payment canceled = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(canceled.getStatus()).isEqualTo(PaymentStatus.CANCELED);
    }

    @Test
    @DisplayName("결제 금액이 0 이하일 경우 예외가 발생한다")
    void pay_withInvalidAmount_throwsException() throws InterruptedException {
        // given
        Reservation reservation = createReservation(userId);

        // when // then
        assertThatThrownBy(() ->
                paymentCommandService.pay(new CreatePaymentCommand(1L, reservation.getId(), BigDecimal.ZERO)))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("price는 필수이며 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("매진되면 랭킹 기록이 수행된다")
    void pay_lastSeat_triggersRankingRecord() throws InterruptedException {
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

        String tokenId = tokenCommandService.issue(userId);
        Thread.sleep(1);
        tokenCommandService.activateEligibleTokens(1000);
        cashService.charge(new ChargeCashCommand(userId, BigDecimal.valueOf(50000)));

        Reservation reservation = reservationCommandService.reserve(
                new CreateReservationCommand(tokenId, userId, seat.getId(), seat.getPrice()));

        // when
        Payment payment = paymentCommandService.pay(
                new CreatePaymentCommand(userId, reservation.getId(), seat.getPrice())
        );

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);

        String rankingKey = "concert-soldout-ranking:" + LocalDate.now();
        Set<String> rankingMembers = redisTemplate.opsForZSet().range(rankingKey, 0, -1);
        assertThat(rankingMembers).contains(String.valueOf(concert.getId()));
    }

    private void clearRedisQueue() {
        redisTemplate.delete("waiting-token:WAITING");
        redisTemplate.delete("active-token");

        Set<String> tokenKeys = redisTemplate.keys("token:*");
        if (tokenKeys != null && !tokenKeys.isEmpty()) {
            redisTemplate.delete(tokenKeys);
        }
    }

    private void waitUntilTokenIsFirst(String tokenId) throws InterruptedException {
        int retry = 0;
        while (retry++ < 200) {  // 최대 1초 대기 (200 * 5ms)
            Set<String> first = redisTemplate.opsForZSet().range("waiting-token:WAITING", 0, 0);
            if (first != null && first.contains(tokenId)) {
                return; // 선두일 때만 통과
            }
            Thread.sleep(5);
        }
        Set<String> currentQueue = redisTemplate.opsForZSet().range("waiting-token:WAITING", 0, -1);
        System.out.println("❌ 대기열 선두 아님: " + tokenId);
        System.out.println("📌 현재 대기열: " + currentQueue);

        throw new IllegalStateException("토큰이 대기열 선두가 되지 못했습니다: " + tokenId);
    }
}
