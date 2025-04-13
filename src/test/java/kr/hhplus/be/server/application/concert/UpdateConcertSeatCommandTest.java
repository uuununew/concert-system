package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.support.exception.CustomException;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateConcertSeatCommandTest {
    @Test
    @DisplayName("좌석 번호가 null이면 예외 발생")
    void seatNumber_null_should_throw_exception() {
        assertThatThrownBy(() ->
                new UpdateConcertSeatCommand(null, "A구역", "1열", "VIP", BigDecimal.valueOf(10000))
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining("좌석 번호는 필수입니다.");
    }

    @Test
    @DisplayName("좌석 번호가 공백이면 예외 발생")
    void seatNumber_blank_should_throw_exception() {
        assertThatThrownBy(() ->
                new UpdateConcertSeatCommand("   ", "A구역", "1열", "VIP", BigDecimal.valueOf(10000))
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining("좌석 번호는 필수입니다.");
    }

    @Test
    @DisplayName("구역 정보가 null이면 예외 발생")
    void section_null_should_throw_exception() {
        assertThatThrownBy(() ->
                new UpdateConcertSeatCommand("A1", null, "1열", "VIP", BigDecimal.valueOf(10000))
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining("구역 정보는 필수입니다.");
    }

    @Test
    @DisplayName("구역 정보가 공백이면 예외 발생")
    void section_blank_should_throw_exception() {
        assertThatThrownBy(() ->
                new UpdateConcertSeatCommand("A1", "  ", "1열", "VIP", BigDecimal.valueOf(10000))
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining("구역 정보는 필수입니다.");
    }

    @Test
    @DisplayName("행 정보가 null이면 예외 발생")
    void row_null_should_throw_exception() {
        assertThatThrownBy(() ->
                new UpdateConcertSeatCommand("A1", "A구역", null, "VIP", BigDecimal.valueOf(10000))
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining("행 정보는 필수입니다.");
    }

    @Test
    @DisplayName("행 정보가 공백이면 예외 발생")
    void row_blank_should_throw_exception() {
        assertThatThrownBy(() ->
                new UpdateConcertSeatCommand("A1", "A구역", " ", "VIP", BigDecimal.valueOf(10000))
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining("행 정보는 필수입니다.");
    }

    @Test
    @DisplayName("등급 정보가 null이면 예외 발생")
    void grade_null_should_throw_exception() {
        assertThatThrownBy(() ->
                new UpdateConcertSeatCommand("A1", "A구역", "1열", null, BigDecimal.valueOf(10000))
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining("등급 정보는 필수입니다.");
    }

    @Test
    @DisplayName("등급 정보가 공백이면 예외 발생")
    void grade_blank_should_throw_exception() {
        assertThatThrownBy(() ->
                new UpdateConcertSeatCommand("A1", "A구역", "1열", "   ", BigDecimal.valueOf(10000))
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining("등급 정보는 필수입니다.");
    }

    @Test
    @DisplayName("가격이 null이면 예외 발생")
    void price_null_should_throw_exception() {
        assertThatThrownBy(() ->
                new UpdateConcertSeatCommand("A1", "A구역", "1열", "VIP", null)
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining("가격은 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("가격이 0 이하면 예외 발생")
    void price_zero_or_negative_should_throw_exception() {
        assertThatThrownBy(() ->
                new UpdateConcertSeatCommand("A1", "A구역", "1열", "VIP", BigDecimal.valueOf(0))
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining("가격은 0보다 커야 합니다.");
    }
}
