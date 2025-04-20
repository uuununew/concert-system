package kr.hhplus.be.server.application.concert.integration;

import kr.hhplus.be.server.application.concert.*;
import kr.hhplus.be.server.config.TestContainerConfig;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertSeat;
import kr.hhplus.be.server.domain.concert.ConcertStatus;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@Transactional
@Testcontainers
public class ConcertSeatIntegrationTest extends TestContainerConfig {

    @Autowired
    private ConcertService concertService;

    @Autowired
    private ConcertSeatCommandService seatCommandService;

    @Autowired
    private ConcertSeatQueryService seatQueryService;

    @Test
    @DisplayName("좌석을 등록하고 조회할 수 있다")
    void test_create_and_find_seats() {
        // given
        Concert concert = concertService.registerConcert(
                new CreateConcertCommand("싸이 흠뻑쇼", 1, ConcertStatus.READY, LocalDateTime.now().plusDays(2))
        );

        CreateConcertSeatCommand command = new CreateConcertSeatCommand(
                concert.getId(), "A1", "1층", "1열", "VIP", BigDecimal.valueOf(10000));
        // when
        ConcertSeat created = seatCommandService.registerSeat(command);
        List<ConcertSeat> seats = seatQueryService.getSeats(concert.getId());

        // then
        assertThat(seats).hasSize(1);
        assertThat(seats.get(0).getId()).isEqualTo(created.getId());
        assertThat(seats.get(0).getSeatNumber()).isEqualTo("A1");
        assertThat(seats.get(0).getSection()).isEqualTo("1층");
        assertThat(seats.get(0).getRow()).isEqualTo("1열");
        assertThat(seats.get(0).getGrade()).isEqualTo("VIP");
        assertThat(seats.get(0).getPrice()).isEqualTo(BigDecimal.valueOf(10000));
    }

    @Test
    @DisplayName("좌석 정보를 수정할 수 있다")
    void test_update_seat() {
        // given
        Concert concert = concertService.registerConcert(
                new CreateConcertCommand("수정 콘서트", 1, ConcertStatus.READY, LocalDateTime.now().plusDays(2)));

        ConcertSeat created = seatCommandService.registerSeat(
                new CreateConcertSeatCommand(concert.getId(), "B1", "2층", "2열", "R", BigDecimal.valueOf(12000)));

        // when
        ConcertSeat updated = seatCommandService.updateSeat(
                created.getId(),
                new UpdateConcertSeatCommand("VIP-B1", "3층", "3열", "VIP", BigDecimal.valueOf(20000)));

        // then
        assertThat(updated.getSeatNumber()).isEqualTo("VIP-B1");
        assertThat(updated.getSection()).isEqualTo("3층");
        assertThat(updated.getRow()).isEqualTo("3열");
        assertThat(updated.getGrade()).isEqualTo("VIP");
        assertThat(updated.getPrice()).isEqualTo(BigDecimal.valueOf(20000));
    }

    @Test
    @DisplayName("좌석 가격이 음수로 수정되면 예외 발생")
    void test_update_seat_with_negative_price() {
        // given
        Concert concert = concertService.registerConcert(new CreateConcertCommand(
                "유효성 테스트 콘서트", 1, ConcertStatus.READY, LocalDateTime.now().plusDays(1)));

        ConcertSeat created = seatCommandService.registerSeat(
                new CreateConcertSeatCommand(concert.getId(), "Z1", "4층", "1열", "B", BigDecimal.valueOf(10000)));

        // when // then
        assertThatThrownBy(() -> seatCommandService.updateSeat(
                created.getId(),
                new UpdateConcertSeatCommand("Z1", "4층", "1열", "B", BigDecimal.valueOf(-10000))))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("가격은 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("좌석을 여러 개 등록하고 모두 조회할 수 있다")
    void test_create_multiple_seats() {
        // given
        Concert concert = concertService.registerConcert(new CreateConcertCommand("다중 좌석 콘서트", 1, ConcertStatus.READY, LocalDateTime.now().plusDays(1)));

        seatCommandService.registerSeat(new CreateConcertSeatCommand(concert.getId(), "C1", "3층", "1", "S", new BigDecimal("10000")));
        seatCommandService.registerSeat(new CreateConcertSeatCommand(concert.getId(), "C2", "3층", "2", "S", new BigDecimal("15000")));
        seatCommandService.registerSeat(new CreateConcertSeatCommand(concert.getId(), "C3", "3층", "3", "S", new BigDecimal("20000")));

        // when
        List<ConcertSeat> seats = seatQueryService.getSeats(concert.getId());

        // then
        assertThat(seats).hasSize(3);
        assertThat(seats).extracting(ConcertSeat::getSeatNumber).containsExactlyInAnyOrder("C1", "C2", "C3");
    }

    @Test
    @DisplayName("존재하지 않는 콘서트에 좌석 등록 시 예외 발생")
    void test_register_seat_with_invalid_concert() {
        // given
        CreateConcertSeatCommand command = new CreateConcertSeatCommand(
                999999L, "C1", "1층", "1열", "S", BigDecimal.valueOf(15000));

        // when //then
        assertThatThrownBy(() -> seatCommandService.registerSeat(command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("해당 ID의 콘서트가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 좌석 수정 시 예외 발생")
    void test_update_seat_with_invalid_id() {
        // when // then
        assertThatThrownBy(() -> seatCommandService.registerSeat(
                new CreateConcertSeatCommand(
                        -1L, "C1", "1층", "1열", "S", BigDecimal.valueOf(15000))))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("콘서트 ID는 필수이며 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("콘서트가 없으면 좌석 조회 시 예외 발생")
    void test_get_seat_with_invalid_concert() {
        CustomException ex = (CustomException) catchThrowable(() -> seatQueryService.getSeats(-1L));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
        assertThat(ex.getMessage()).contains("해당 콘서트가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("좌석이 없는 콘서트 조회 시 빈 리스트를 반환한다")
    void test_get_seats_empty() {
        // given
        Concert concert = concertService.registerConcert(new CreateConcertCommand("빈 좌석 콘서트", 1, ConcertStatus.READY, LocalDateTime.now().plusDays(1)));

        // when
        List<ConcertSeat> seats = seatQueryService.getSeats(concert.getId());

        // then
        assertThat(seats).isEmpty();
    }

}
