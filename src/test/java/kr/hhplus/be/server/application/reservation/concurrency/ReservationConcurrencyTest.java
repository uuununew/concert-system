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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
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

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private EntityManager entityManager;

    private Long seatId;

    @DisplayName("동시에 10명이 같은 좌석을 예약할 경우 1명만 성공하고 나머지는 예외 발생")
    @Test
    void reserveSeat_concurrent_fail_on_duplicate() throws InterruptedException{
        // 1. 콘서트 및 좌석 생성
        Concert concert = concertService.registerConcert(
                new CreateConcertCommand("콜드플레이", 1, ConcertStatus.OPENED,
                        LocalDateTime.of(2025, 6, 20, 20, 0)));

        ConcertSeat seat = ConcertSeat.of(concert, "1", "1층", "A", "R", BigDecimal.valueOf(100000));
        concertSeatRepository.save(seat);
        seatId = seat.getId();

        // 2. Redis 락 초기화
        redisTemplate.delete("seat:" + seatId);

        // 3. 토큰 생성: userId=1이 가장 앞서도록 issuedAt 설정
        LocalDateTime now = LocalDateTime.now();
        for (long userId = 1; userId <= 10; userId++) {
            LocalDateTime issuedAt = now.minusSeconds(100 + (10 - userId));  // userId=1이 가장 오래됨
            tokenRepository.save(new QueueToken(userId, issuedAt));
        }

        // given : 테스트용 좌석에 대해 동시에 예약을 시도할 유저 수 설정
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1); // 시작 신호용
        CountDownLatch doneLatch = new CountDownLatch(threadCount); // 모든 작업 완료 대기용
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger conflictCount = new AtomicInteger();

        // userId=1 먼저 실행되도록 정렬
        List<Long> userIds = LongStream.rangeClosed(1, 10)
                .boxed()
                .sorted(Comparator.comparingLong(id -> id != 1 ? 1 : 0)) // userId=1 맨 앞
                .collect(Collectors.toList());

        // when : 동시에 각기 다른 유저가 같은 좌석을 예약 시도
        for (Long userId : userIds) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    Thread.sleep(userId == 1 ? 5 : 20 + (long) (Math.random() * 20));

                    // 락 획득에 실패하면 null 또는 예외 발생할 수 있음
                    reservationCommandService.reserve(
                            new CreateReservationCommand(userId, seatId, BigDecimal.valueOf(10000)));

                    log.info("예약 성공: userId={}", userId);
                    successCount.incrementAndGet(); // 예외 없이 통과되면 성공
                } catch (CustomException e) {
                    log.warn("[CustomException] userId: {}, message: {}", userId, e.getMessage());
                    conflictCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("[UnexpectedException] userId: {}, message: {}", userId, e.getMessage());
                    conflictCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        // 5. 실행
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        log.info("성공 수: {}, 실패 수: {}", successCount.get(), conflictCount.get());

        // 6. 검증
        //then
        assertThat(successCount.get())
                .withFailMessage("성공 수가 예상과 다릅니다. 기대값: 1, 실제값: %d", successCount.get())
                .isEqualTo(1);

        assertThat(conflictCount.get())
                .withFailMessage("실패 수가 예상과 다릅니다. 기대값: 9, 실제값: %d", conflictCount.get())
                .isEqualTo(9);
    }
}
