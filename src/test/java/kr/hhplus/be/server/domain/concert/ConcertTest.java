package kr.hhplus.be.server.domain.concert;

import kr.hhplus.be.server.support.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ConcertTest {
    @DisplayName("status가 OPENED이면 isOpened는 true를 반환한다")
    @Test
    void isOpened_should_return_true_when_status_is_OPENED() {
        // given
        Concert concert = new Concert("IU 콘서트", 1, ConcertStatus.OPENED, LocalDateTime.now().plusDays(1));

        // when
        boolean result = concert.isOpened();

        // then
        assertThat(result).isTrue();
    }

    @DisplayName("status가 OPENED가 아니면 isOpened는 false를 반환한다")
    @Test
    void isOpened_should_return_false_when_status_is_not_OPENED() {
        // given
        Concert concert = new Concert("BTS 콘서트", 1, ConcertStatus.CLOSED, LocalDateTime.now().plusDays(1));

        // when
        boolean result = concert.isOpened();

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("정적 팩토리 메서드 create로 콘서트를 생성할 수 있다")
    void createConcert_success() {
        // when
        Concert concert = Concert.create("New Concert", 2, ConcertStatus.OPENED, LocalDateTime.now().plusDays(5));

        // then
        assertThat(concert.getTitle()).isEqualTo("New Concert");
        assertThat(concert.getRound()).isEqualTo(2);
        assertThat(concert.getStatus()).isEqualTo(ConcertStatus.OPENED);
    }

    @DisplayName("READY 상태인 콘서트는 취소할 수 있다.")
    @Test
    void cancel_ready_success() {
        Concert concert = Concert.withStatus(ConcertStatus.READY);

        concert.cancel();

        assertThat(concert.getStatus()).isEqualTo(ConcertStatus.CANCELED);
    }

    @DisplayName("CANCELED 상태는 취소할 수 없다.")
    void cancel_canceled_fail() {
        Concert concert = Concert.withStatus(ConcertStatus.CANCELED);

        assertThatThrownBy(concert::cancel)
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("READY 또는 OPENED 상태만 취소할 수 있습니다.");
    }

    @DisplayName("CLOSED 상태는 취소할 수 없다.")
    @Test
    void cancel_closed_fail() {
        Concert concert = Concert.withStatus(ConcertStatus.CLOSED);

        assertThatThrownBy(concert::cancel)
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("READY 또는 OPENED 상태만 취소할 수 있습니다.");
    }
}
