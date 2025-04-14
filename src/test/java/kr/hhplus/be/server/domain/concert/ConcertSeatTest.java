package kr.hhplus.be.server.domain.concert;


import kr.hhplus.be.server.support.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ConcertSeatTest {

    private Concert dummyConcert() {
        return Concert.withAll(1L, "제니 콘서트", 1, ConcertStatus.READY, LocalDateTime.now());
    }

    @Test
    @DisplayName("AVAILABLE 상태의 좌석은 RESERVED 상태로 전환할 수 있다")
    void reserve_success() {
        // given : 예약 가능한 AVAILABLE 상태의 좌석 생성
        Concert concert = Concert.withStatus(ConcertStatus.READY);
        ConcertSeat seat = ConcertSeat.withAll(
                1L, concert, "A1", "1층", "A", "VIP", BigDecimal.valueOf(100000),
                SeatStatus.AVAILABLE, LocalDateTime.now()
        );


        // when : 좌석 예약을 시도
        seat.reserve();

        // then : 상태가 RESERVED로 변경
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.RESERVED);
    }

    @Test
    @DisplayName("AVAILABLE 상태가 아닌 좌석은 예약할 수 없다")
    void reserve_fail_when_not_available() {
        // given : 이미 SOLD_OUT 상태인 좌석 생성
        ConcertSeat seat = ConcertSeat.withAll(
                1L, dummyConcert(), "A1", "1층", "A", "VIP",
                BigDecimal.valueOf(100000), SeatStatus.SOLD_OUT, LocalDateTime.now());

        // when//then : 예약 시도 시 예외 발생
        assertThatThrownBy(seat::reserve)
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("예약할 수 없는 좌석");
    }

    @Test
    @DisplayName("RESERVED 상태의 좌석은 SOLD_OUT 상태로 전환할 수 있다")
    void markSoldOut_success() {
        // given : RESERVED 상태의 좌석 생성
        ConcertSeat seat = ConcertSeat.withAll(
                1L, dummyConcert(), "A1", "1층", "A", "VIP",
                BigDecimal.valueOf(100000), SeatStatus.RESERVED, LocalDateTime.now());

        // when : 결제 완료 처리 시도
        seat.markSoldOut();

        // then : 상태가 SOLD_OUT으로 변경
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.SOLD_OUT);
    }

    @Test
    @DisplayName("RESERVED 상태가 아닌 좌석은 결제 완료로 전환할 수 없다")
    void markSoldOut_fail_when_not_reserved() {
        // given : 현재 상태가 AVAILABLE인 좌석
        ConcertSeat seat = ConcertSeat.withAll(
                1L, dummyConcert(), "A1", "1층", "A", "VIP",
                BigDecimal.valueOf(100000), SeatStatus.AVAILABLE, LocalDateTime.now());

        // when//then : 결제 완료 시도 시 예외
        assertThatThrownBy(seat::markSoldOut)
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("RESERVED일 때만");
    }
}
