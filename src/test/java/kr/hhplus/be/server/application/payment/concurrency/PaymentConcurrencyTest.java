package kr.hhplus.be.server.application.payment.concurrency;


import kr.hhplus.be.server.application.cash.CashCommandService;
import kr.hhplus.be.server.application.cash.ChargeCashCommand;
import kr.hhplus.be.server.application.concert.ConcertService;
import kr.hhplus.be.server.application.concert.CreateConcertCommand;
import kr.hhplus.be.server.application.payment.CreatePaymentCommand;
import kr.hhplus.be.server.application.payment.PaymentCommandService;
import kr.hhplus.be.server.application.reservation.CreateReservationCommand;
import kr.hhplus.be.server.application.reservation.ReservationCommandService;
import kr.hhplus.be.server.config.TestContainerConfig;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertSeat;
import kr.hhplus.be.server.domain.concert.ConcertSeatRepository;
import kr.hhplus.be.server.domain.concert.ConcertStatus;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.token.TokenStatus;
import kr.hhplus.be.server.support.concurrency.ConcurrencyTestExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class PaymentConcurrencyTest extends TestContainerConfig {

    @Autowired
    private PaymentCommandService paymentCommandService;

    @Autowired
    private ReservationCommandService reservationCommandService;

    @Autowired
    private CashCommandService cashCommandService;

    @Autowired
    private ConcertService concertService;

    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private Long reservationId;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        // 1. 유저에게 잔액 충전
        cashCommandService.charge(new ChargeCashCommand(userId, BigDecimal.valueOf(100000)));

        // 2. 콘서트 및 좌석 생성
        Concert concert = concertService.registerConcert(new CreateConcertCommand(
                "IU 콘서트", 1, ConcertStatus.OPENED, LocalDateTime.of(2025, 6, 1, 20, 0)
        ));

        ConcertSeat seat = ConcertSeat.of(
                concert, "1", "1층", "A", "R", BigDecimal.valueOf(100000)
        );
        concertSeatRepository.save(seat);

        // 3. 토큰 사전 발급
        tokenRepository.save(QueueToken.create(userId));

        // 4. 예약 생성
        Reservation reservation = reservationCommandService.reserve(
                new CreateReservationCommand(userId, seat.getId(), BigDecimal.valueOf(100000))
        );
        reservationId = reservation.getId();
    }

    @DisplayName("동시에 결제 요청이 들어올 경우 하나만 성공해야 한다.")
    @Test
    void pay_concurrent_fail_on_duplicate_payment() throws InterruptedException{

        //given
        int threadCount = 10;
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        //when
        ConcurrencyTestExecutor.run(threadCount, () -> {
            try{
                paymentCommandService.pay(new CreatePaymentCommand(userId, reservationId, BigDecimal.valueOf(10000)));
            }catch (Throwable t){
                exceptions.add(t);
            }
        });

        //then
        long successCount = threadCount - exceptions.size();
        assertThat(successCount).isEqualTo(1);
        assertThat(paymentRepository.findByUserId(userId).size()).isEqualTo(1);
    }
}
