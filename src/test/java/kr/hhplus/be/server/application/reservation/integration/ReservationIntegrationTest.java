package kr.hhplus.be.server.application.reservation.integration;

import kr.hhplus.be.server.application.reservation.CreateReservationCommand;
import kr.hhplus.be.server.application.reservation.ReservationCommandService;
import kr.hhplus.be.server.config.TestContainerConfig;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.token.QueueToken;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import kr.hhplus.be.server.domain.concert.*;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.support.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootTest
@Transactional
@Testcontainers
public class ReservationIntegrationTest extends TestContainerConfig {

    @Autowired
    private ReservationCommandService reservationCommandService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Autowired
    private TokenRepository tokenRepository;

    private Long concertSeatId;

    @BeforeEach
    void setUp() {
        // 콘서트 생성
        Concert concert = concertRepository.save(
                Concert.create("테스트 콘서트", 1, ConcertStatus.READY, LocalDateTime.now().plusDays(1)));
        // 좌석 생성
        ConcertSeat seat = concertSeatRepository.save(
                ConcertSeat.of(concert, "A1", "1층", "A", "VIP", BigDecimal.valueOf(10000)));
        concertSeatId = seat.getId();

        // 토큰 발급
        tokenRepository.save(new QueueToken(1L, LocalDateTime.now()));
    }

    @Test
    @DisplayName("좌석을 정상적으로 예약할 수 있다")
    void reserve_success() {
        // given
        CreateReservationCommand command = new CreateReservationCommand(
                1L, concertSeatId, BigDecimal.valueOf(10000));

        // when
        Reservation reservation = reservationCommandService.reserve(command);

        // then
        assertThat(reservation).isNotNull();
        assertThat(reservation.getUserId()).isEqualTo(1L);
        assertThat(reservation.getConcertSeat().getId()).isEqualTo(concertSeatId);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }

    @Test
    @DisplayName("예약 ID로 예약 정보를 조회할 수 있다")
    void find_reservation_by_id() {
        // given
        CreateReservationCommand command = new CreateReservationCommand(1L, concertSeatId, BigDecimal.valueOf(10000));
        Reservation reservation = reservationCommandService.reserve(command);

        // when
        Reservation found = reservationRepository.findById(reservation.getId())
                .orElseThrow();

        // then
        assertThat(found.getUserId()).isEqualTo(1L);
        assertThat(found.getConcertSeat().getId()).isEqualTo(concertSeatId);
    }

    @Test
    @DisplayName("이미 예약된 좌석은 예약할 수 없다")
    void reserve_fail_if_already_reserved() {
        // given
        CreateReservationCommand command = new CreateReservationCommand(
                1L, concertSeatId, BigDecimal.valueOf(10000));

        // 기존 토큰 삭제
        tokenRepository.findByUserId(1L)
                .ifPresent(token -> tokenRepository.delete(token.getUserId()));

        // 새 토큰 저장 및 활성화
        QueueToken token = new QueueToken(1L, LocalDateTime.now().minusSeconds(10));
        token.activate();
        tokenRepository.save(token);

        reservationCommandService.reserve(command); // 첫 예약 성공

        // 두 번째 토큰 재등록 (다시 활성화)
        tokenRepository.findByUserId(1L)
                .ifPresent(t -> tokenRepository.delete(t.getUserId()));
        QueueToken secondToken = new QueueToken(1L, LocalDateTime.now().minusSeconds(5));
        secondToken.activate();
        tokenRepository.save(secondToken);

        // when // then
        assertThatThrownBy(() -> reservationCommandService.reserve(command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("예약할 수 없는 좌석입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 좌석 ID로 예약하면 예외가 발생한다")
    void reserve_fail_if_seat_not_found() {
        // given
        CreateReservationCommand command = new CreateReservationCommand(
                1L, 9999L, BigDecimal.valueOf(10000));

        // when //then
        assertThatThrownBy(() -> reservationCommandService.reserve(command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("해당 좌석 정보를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("토큰이 없는 유저가 예약을 시도하면 예외가 발생한다")
    void reserve_fail_if_token_missing() {
        // given
        CreateReservationCommand command = new CreateReservationCommand(
                999L, concertSeatId, BigDecimal.valueOf(10000));

        // when //then
        assertThatThrownBy(() -> reservationCommandService.reserve(command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("토큰 정보가 없습니다");
    }
}
