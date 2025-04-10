package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.domain.concert.ConcertStatus;
import kr.hhplus.be.server.support.exception.CustomException;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CreateConcertCommandTest {

    @DisplayName("정상 입력 시 예외 없이 생성된다")
    @Test
    void create_success() {
        // when/then
        new CreateConcertCommand("방탄 콘서트", 2, ConcertStatus.OPENED, LocalDateTime.now().plusDays(1));
    }

    @Test
    @DisplayName("concertId가 null이거나 0 이하일 경우 예외 발생")
    void concertId_invalid_should_throw() {
        assertThatThrownBy(() ->
                new CreateConcertSeatCommand(null, "A1", "1층", "A", "VIP", BigDecimal.valueOf(10000))
        ).isInstanceOf(CustomException.class)
                .hasMessageContaining("콘서트 ID는 필수이며 0보다 커야 합니다.");

        assertThatThrownBy(() ->
                new CreateConcertSeatCommand(0L, "A1", "1층", "A", "VIP", BigDecimal.valueOf(10000))
        ).isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("좌석 번호가 null 또는 공백이면 예외 발생")
    void seatNumber_invalid_should_throw() {
        assertThatThrownBy(() ->
                new CreateConcertSeatCommand(1L, null, "1층", "A", "VIP", BigDecimal.valueOf(10000))
        ).isInstanceOf(CustomException.class);

        assertThatThrownBy(() ->
                new CreateConcertSeatCommand(1L, "   ", "1층", "A", "VIP", BigDecimal.valueOf(10000))
        ).isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("section, row, grade가 null 또는 공백이면 예외 발생")
    void others_invalid_should_throw() {
        assertThatThrownBy(() ->
                new CreateConcertSeatCommand(1L, "A1", null, "A", "VIP", BigDecimal.valueOf(10000))
        ).isInstanceOf(CustomException.class);

        assertThatThrownBy(() ->
                new CreateConcertSeatCommand(1L, "A1", "1층", null, "VIP", BigDecimal.valueOf(10000))
        ).isInstanceOf(CustomException.class);

        assertThatThrownBy(() ->
                new CreateConcertSeatCommand(1L, "A1", "1층", "A", null, BigDecimal.valueOf(10000))
        ).isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("가격이 null이거나 0 이하일 경우 예외 발생")
    void price_invalid_should_throw() {
        assertThatThrownBy(() ->
                new CreateConcertSeatCommand(1L, "A1", "1층", "A", "VIP", null)
        ).isInstanceOf(CustomException.class);

        assertThatThrownBy(() ->
                new CreateConcertSeatCommand(1L, "A1", "1층", "A", "VIP", BigDecimal.ZERO)
        ).isInstanceOf(CustomException.class);
    }
}
