package kr.hhplus.be.server.domain.concert;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

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
}
