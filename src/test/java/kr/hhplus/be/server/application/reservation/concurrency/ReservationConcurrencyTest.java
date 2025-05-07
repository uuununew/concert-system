package kr.hhplus.be.server.application.reservation.concurrency;

import kr.hhplus.be.server.application.concert.ConcertService;
import kr.hhplus.be.server.application.concert.CreateConcertCommand;
import kr.hhplus.be.server.application.reservation.CreateReservationCommand;
import kr.hhplus.be.server.application.reservation.ReservationCommandService;
import kr.hhplus.be.server.domain.concert.*;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.support.config.RedisTestContainerConfig;
import kr.hhplus.be.server.support.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@Import(RedisTestContainerConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ReservationConcurrencyTest{

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
            "콜드플레이", 1, ConcertStatus.OPENED, LocalDateTime.of(2025, 5, 20,  20, 0)));

        //좌석 생성
        ConcertSeat seat = ConcertSeat.of(
                concert, "1", "1층", "A", "R", BigDecimal.valueOf(100000));
        concertSeatRepository.save(seat);
        seatId = seat.getId();

        // 미리 대기열에 WAITING 토큰 등록 (가장 먼저 줄 선 사람)
        QueueToken waitingToken = new QueueToken(1L, LocalDateTime.now().minusSeconds(5));
        tokenRepository.save(waitingToken);
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
            final long userId = i + 1;

            executorService.submit(() -> {
                try {
                    Thread.sleep((long) (Math.random() * 50));

                    if (userId != 1L) {
                        QueueToken token = new QueueToken(userId, LocalDateTime.now());
                        tokenRepository.save(token); // WAITING 상태
                    }

                    // 락 획득에 실패하면 null 또는 예외 발생할 수 있음
                    Reservation reservation = reservationCommandService.reserve(
                            new CreateReservationCommand(userId, seatId, BigDecimal.valueOf(10000)));

                    if (reservation != null) {
                        successCount.incrementAndGet(); // 실제 예약 성공 시
                    } else {
                        conflictCount.incrementAndGet(); // 락 실패
                    }

                } catch (CustomException e) {
                    conflictCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        //then
        assertThat(successCount.get())
                .withFailMessage("성공 수가 예상과 다릅니다. 기대값: 1, 실제값: %d", successCount.get())
                .isEqualTo(1);

        assertThat(conflictCount.get())
                .withFailMessage("실패 수가 예상과 다릅니다. 기대값: %d, 실제값: %d", threadCount - 1, conflictCount.get())
                .isEqualTo(threadCount - 1);
    }

}
