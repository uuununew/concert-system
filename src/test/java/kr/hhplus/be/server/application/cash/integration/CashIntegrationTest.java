package kr.hhplus.be.server.application.cash.integration;
import kr.hhplus.be.server.application.cash.CashResult;
import kr.hhplus.be.server.application.cash.ChargeCashCommand;
import kr.hhplus.be.server.application.cash.CashService;

import kr.hhplus.be.server.application.cash.UseCashCommand;
import kr.hhplus.be.server.config.TestContainerConfig;
import kr.hhplus.be.server.domain.cash.CashHistory;
import kr.hhplus.be.server.domain.cash.CashHistoryRepository;
import kr.hhplus.be.server.domain.cash.UserCash;
import kr.hhplus.be.server.domain.cash.UserCashRepository;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Testcontainers
public class CashIntegrationTest extends TestContainerConfig {

    @Autowired
    private CashService cashService;

    @Autowired
    private UserCashRepository userCashRepository;

    @Autowired
    private CashHistoryRepository cashHistoryRepository;

    @Test
    @DisplayName("캐시가 존재하지 않으면 새로 생성하고 충전한다")
    void chargeCash_whenUserCashDoesNotExist_thenCreatesAndCharges() {
        // given
        Long userId = 100L;
        BigDecimal amount = BigDecimal.valueOf(10000);

        // when
        CashResult result = cashService.charge(new ChargeCashCommand(userId, amount));

        // then
        assertThat(result.getAmount()).isEqualByComparingTo(amount);

        UserCash userCash = userCashRepository.findByUserId(userId).orElseThrow();
        assertThat(userCash.getAmount()).isEqualByComparingTo(amount);

        List<CashHistory> historyList = cashHistoryRepository.findByUserId(userId);
        assertThat(historyList).hasSize(1);
        assertThat(historyList.get(0).getAmount()).isEqualByComparingTo(amount);
    }

    @Test
    @DisplayName("같은 유저에게 여러 번 충전 시 잔액이 누적되고 이력도 누적된다")
    void chargeCash_multipleTimes_thenAccumulatesBalanceAndHistory() {
        // given
        Long userId = 400L;
        BigDecimal firstCharge = BigDecimal.valueOf(10000);
        BigDecimal secondCharge = BigDecimal.valueOf(15000);

        // when
        cashService.charge(new ChargeCashCommand(userId, firstCharge));
        cashService.charge(new ChargeCashCommand(userId, secondCharge));

        // then
        UserCash userCash = userCashRepository.findByUserId(userId).orElseThrow();
        assertThat(userCash.getAmount()).isEqualByComparingTo(firstCharge.add(secondCharge));

        List<CashHistory> historyList = cashHistoryRepository.findByUserId(userId);
        assertThat(historyList).hasSize(2);
        assertThat(historyList.get(0).getAmount().add(historyList.get(1).getAmount()))
                .isEqualByComparingTo(firstCharge.add(secondCharge));
    }

    @Test
    @DisplayName("기존 캐시에서 금액을 차감하고 이력을 남긴다")
    void useCash_whenSufficientBalance_thenUsesSuccessfully() {
        // given
        Long userId = 200L;
        BigDecimal chargeAmount = BigDecimal.valueOf(20000);
        BigDecimal useAmount = BigDecimal.valueOf(5000);

        cashService.charge(new ChargeCashCommand(userId, chargeAmount));

        // when
        CashResult result = cashService.use(new UseCashCommand(userId, useAmount));

        // then
        assertThat(result.getAmount()).isEqualByComparingTo(chargeAmount.subtract(useAmount));

        UserCash userCash = userCashRepository.findByUserId(userId).orElseThrow();
        assertThat(userCash.getAmount()).isEqualByComparingTo(chargeAmount.subtract(useAmount));

        List<CashHistory> historyList = cashHistoryRepository.findByUserId(userId);
        assertThat(historyList).hasSize(2); // 충전 + 사용
    }

    @Test
    @DisplayName("잔액 전액 사용 시 잔액이 0이 되고 이력이 두 개 남는다")
    void useCash_entireBalance_thenZeroBalance() {
        // given
        Long userId = 500L;
        BigDecimal chargeAmount = BigDecimal.valueOf(10000);

        cashService.charge(new ChargeCashCommand(userId, chargeAmount));

        // when
        CashResult result = cashService.use(new UseCashCommand(userId, chargeAmount));

        // then
        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);

        UserCash userCash = userCashRepository.findByUserId(userId).orElseThrow();
        assertThat(userCash.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);

        List<CashHistory> historyList = cashHistoryRepository.findByUserId(userId);
        assertThat(historyList).hasSize(2); // 충전 + 사용
    }

    @Test
    @DisplayName("잔액 부족 시 예외 발생")
    void useCash_whenInsufficientBalance_thenThrowsException() {
        // given
        Long userId = 300L;
        BigDecimal chargeAmount = BigDecimal.valueOf(3000);
        BigDecimal useAmount = BigDecimal.valueOf(10000);

        cashService.charge(new ChargeCashCommand(userId, chargeAmount));

        // when // then
        assertThatThrownBy(() -> cashService.use(new UseCashCommand(userId, useAmount)))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        List<CashHistory> historyList = cashHistoryRepository.findByUserId(userId);
        assertThat(historyList).hasSize(1); // 충전만 됨
    }

    @Test
    @DisplayName("음수 금액 충전 시 예외 발생")
    void chargeCash_whenAmountIsNegative_thenThrowsException() {
        // given
        Long userId = 999L;
        BigDecimal invalidAmount = BigDecimal.valueOf(-5000);

        // when // then
        assertThatThrownBy(() -> cashService.charge(new ChargeCashCommand(userId, invalidAmount)))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("충전 금액은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("캐시가 존재하지 않는 유저가 사용 시도하면 예외 발생")
    void useCash_whenUserCashDoesNotExist_thenThrowsException() {
        // given
        Long userId = 888L;
        BigDecimal useAmount = BigDecimal.valueOf(5000);

        // when // then
        assertThatThrownBy(() -> cashService.use(new UseCashCommand(userId, useAmount)))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
    }
}
