package kr.hhplus.be.server.application.cash.concurrency;

import jakarta.persistence.OptimisticLockException;
import kr.hhplus.be.server.application.cash.CashCommandService;
import kr.hhplus.be.server.application.cash.CashService;
import kr.hhplus.be.server.application.cash.ChargeCashCommand;
import kr.hhplus.be.server.application.cash.UseCashCommand;
import kr.hhplus.be.server.config.TestContainerConfig;
import kr.hhplus.be.server.domain.cash.UserCash;
import kr.hhplus.be.server.domain.cash.UserCashRepository;
import kr.hhplus.be.server.infrastructure.cash.UserCashJpaRepository;
import kr.hhplus.be.server.support.concurrency.ConcurrencyTestExecutor;
import kr.hhplus.be.server.support.config.RedisTestContainerConfig;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@Import(RedisTestContainerConfig.class)
public class CashConcurrencyTest{

    @Autowired
    private CashCommandService cashCommandService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UserCashRepository userCashRepository;

    @Autowired
    private UserCashJpaRepository userCashJpaRepository;

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

    @DisplayName("동일 사용자가 동시에 포인트 5번 사용하면 1번은 성공하고 4번은 예외 발생")
    @Test
    void useCash_concurrent_fail_on_insufficient_balance() throws InterruptedException{
        // given
        Long userId = 1L;
        BigDecimal useAmount = BigDecimal.valueOf(800);

        userCashJpaRepository.findByUserId(userId)
                .ifPresent(userCashJpaRepository::delete);
        userCashJpaRepository.save(new UserCash(userId, BigDecimal.valueOf(1000)));

        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger conflictCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    cashCommandService.use(new UseCashCommand(userId, useAmount));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    if (e instanceof ObjectOptimisticLockingFailureException || e instanceof OptimisticLockException || e instanceof CustomException) {
                        conflictCount.incrementAndGet(); // 낙관적 락 충돌
                    } else {
                        throw new RuntimeException("Unexpected exception", e);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);

        // then
        log.info("✅ [{} Threads] 성공: {} / 예외: {}", threadCount, successCount.get(), conflictCount.get());

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(threadCount - 1);

        UserCash userCash = userCashJpaRepository.findByUserId(userId).orElseThrow();
        assertThat(userCash.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(200));
    }

    @DisplayName("동일 사용자가 동시에 포인트 3번 충전하면 1번은 성공하고 2번은 예외 발생")
    @Test
    void charge_concurrent_only_one_succeed() throws InterruptedException {
        // given
        Long userId = 100L;
        BigDecimal amount = BigDecimal.valueOf(1000);

        userCashJpaRepository.findByUserId(userId)
                .ifPresent(userCashJpaRepository::delete);
        userCashJpaRepository.save(new UserCash(userId, BigDecimal.ZERO));

        int threadCount = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger redisLockFailCount = new AtomicInteger();
        AtomicInteger optimisticLockFailCount = new AtomicInteger();
        AtomicInteger unknownErrorCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    Thread.sleep(new Random().nextInt(100)); // 락 충돌 타이밍 분산
                    cashCommandService.charge(new ChargeCashCommand(userId, amount));
                    successCount.incrementAndGet();
                } catch (IllegalStateException e) {
                    redisLockFailCount.incrementAndGet();  // Redis 락 실패
                } catch (CustomException e) {
                    if (e.getErrorCode() == ErrorCode.CONCURRENT_REQUEST) {
                        optimisticLockFailCount.incrementAndGet();  // 낙관적 락 충돌
                    } else {
                        unknownErrorCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    unknownErrorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS); //10초 대기

        // then
        assertThat(successCount.get())
                .withFailMessage("성공 수가 1이 아니고 %d 입니다.", successCount.get())
                .isEqualTo(1);

        assertThat(redisLockFailCount.get() + optimisticLockFailCount.get() + unknownErrorCount.get())
                .withFailMessage("실패 카운트가 예상과 다릅니다. (RedisLock 실패: %d, Optimistic 실패: %d, 기타: %d)",
                        redisLockFailCount.get(), optimisticLockFailCount.get(), unknownErrorCount.get())
                .isEqualTo(threadCount - 1);

        UserCash userCash = userCashJpaRepository.findByUserId(userId).orElseThrow();
        assertThat(userCash.getAmount())
                .withFailMessage("잔액이 예상과 다릅니다. 현재: %s", userCash.getAmount())
                .isEqualByComparingTo(amount);
    }
}
