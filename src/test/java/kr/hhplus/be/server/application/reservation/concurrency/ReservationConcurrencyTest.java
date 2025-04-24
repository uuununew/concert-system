package kr.hhplus.be.server.application.reservation.concurrency;

import jakarta.persistence.OptimisticLockException;
import kr.hhplus.be.server.application.concert.ConcertSeatCommandService;
import kr.hhplus.be.server.application.concert.ConcertService;
import kr.hhplus.be.server.application.concert.CreateConcertCommand;
import kr.hhplus.be.server.application.reservation.CreateReservationCommand;
import kr.hhplus.be.server.application.reservation.ReservationCommandService;
import kr.hhplus.be.server.config.TestContainerConfig;
import kr.hhplus.be.server.domain.concert.*;
import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.support.concurrency.ConcurrencyTestExecutor;
import kr.hhplus.be.server.support.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@Testcontainers
public class ReservationConcurrencyTest extends TestContainerConfig {

    @Autowired
    private ConcertService concertService;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private ReservationCommandService reservationCommandService;

    private Long seatId;

    private long startTime;

    @BeforeEach
    void startTimer() {
        startTime = System.currentTimeMillis();
    }

    @AfterEach
    void endTimer() {
        long endTime = System.currentTimeMillis();
        long elapsed = endTime - startTime;
        log.info("⏱️ 테스트 실행 시간: {}ms", elapsed);
    }

    @BeforeEach
    void setUp(){
        //콘서트 생성
        Concert concert = concertService.registerConcert(new CreateConcertCommand(
            "콜드플레이", 1, ConcertStatus.OPENED, LocalDateTime.of(2025, 5, 1,  20, 0)));

        //좌석 생성
        ConcertSeat seat = ConcertSeat.of(
                concert, "1", "1층", "A", "R", BigDecimal.valueOf(100000));
        concertSeatRepository.save(seat);
        seatId = seat.getId();
    }

    @DisplayName("동시에 10명이 같은 좌석을 예약할 경우 1명만 성공하고 나머지는 예외 발생")
    @Test
    void reserveSeat_concurrent_fail_on_duplicate() throws InterruptedException{
        // given : 테스트용 좌석에 대해 동시에 예약을 시도할 유저 수 설정
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger conflictCount = new AtomicInteger();

        // when : 동시에 각기 다른 유저가 같은 좌석을 예약 시도
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    Long userId = Thread.currentThread().getId();
                    tokenRepository.save(new QueueToken(userId, LocalDateTime.now()));

                    reservationCommandService.reserve(
                            new CreateReservationCommand(userId, seatId, BigDecimal.valueOf(10000))
                    );

                    successCount.incrementAndGet();
                } catch (Exception e) {
                    if (e instanceof ObjectOptimisticLockingFailureException || e instanceof OptimisticLockException || e instanceof CustomException) {
                        conflictCount.incrementAndGet(); // 낙관적 락 충돌 등 예상 가능한 예외
                    } else {
                        throw new RuntimeException("Unexpected exception", e);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);

        // then : 성공한 예약 요청은 오직 1건, 나머지는 예외 발생
        log.info("✅ [{} Threads] 성공: {} / 예외: {}", threadCount, successCount.get(), conflictCount.get());
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(threadCount - 1);

        executorService.shutdown();
    }

}
