package kr.hhplus.be.server.application.cash;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ChargeCashCommandTest {

    @Test
    @DisplayName("유효한 userId와 금액으로 Command를 생성할 수 있다.")
    void valid_command_success() {
        assertDoesNotThrow(() -> new ChargeCashCommand(1L, BigDecimal.valueOf(1000)));
    }

    @Test
    @DisplayName("userId가 null이면 예외가 발생한다.")
    void null_userId_fail() {
        assertThrows(IllegalArgumentException.class, () ->
                new ChargeCashCommand(null, BigDecimal.valueOf(1000)));
    }

    @Test
    @DisplayName("금액이 null이면 예외가 발생한다.")
    void null_amount_fail() {
        assertThrows(IllegalArgumentException.class, () ->
                new ChargeCashCommand(1L, null));
    }

    @Test
    @DisplayName("userId가 0이면 예외가 발생한다.")
    void zero_userId_fail() {
        assertThrows(IllegalArgumentException.class, () ->
                new ChargeCashCommand(0L, BigDecimal.valueOf(500)));
    }

    @Test
    @DisplayName("amount가 0보다 작거나 같으면 예외가 발생한다.")
    void non_positive_amount_fail() {
        assertThrows(IllegalArgumentException.class, () ->
                new ChargeCashCommand(1L, BigDecimal.ZERO));

        assertThrows(IllegalArgumentException.class, () ->
                new ChargeCashCommand(1L, BigDecimal.valueOf(-100)));
    }
}
