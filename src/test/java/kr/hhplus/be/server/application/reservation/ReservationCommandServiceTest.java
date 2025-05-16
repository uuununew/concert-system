package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.application.token.TokenCommandService;
import kr.hhplus.be.server.domain.concert.*;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationCommandServiceTest {

    private TokenCommandService tokenCommandService;
    private ReservationRepository reservationRepository;
    private ConcertSeatRepository concertSeatRepository;
    private ReservationCommandService reservationCommandService;

    @BeforeEach
    void setUp() {
        tokenCommandService = mock(TokenCommandService.class);
        reservationRepository = mock(ReservationRepository.class);
        concertSeatRepository = mock(ConcertSeatRepository.class);

        reservationCommandService = new ReservationCommandService(
                tokenCommandService,
                reservationRepository,
                concertSeatRepository
        );
    }

    @Test
    @DisplayName("예약을 정상적으로 등록한다")
    void reserve_success() {
        // given
        String tokenId = "token-1";
        Long userId = 1L;
        Long seatId = 2L;

        when(tokenCommandService.status(tokenId)).thenReturn(Optional.of(0));

        CreateReservationCommand command = new CreateReservationCommand(tokenId, userId, seatId, BigDecimal.valueOf(10000));
        Concert concert = new Concert("테스트 콘서트", 1, ConcertStatus.READY, LocalDateTime.now());

        ConcertSeat seat = ConcertSeat.withAll(
                seatId, concert, "A1", "1층", "A", "VIP", BigDecimal.valueOf(10000), SeatStatus.AVAILABLE, LocalDateTime.now());

        when(concertSeatRepository.findByIdWithOptimistic(seatId)).thenReturn(Optional.of(seat));
        when(reservationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Reservation result = reservationCommandService.reserve(command);

        // then
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getConcertSeat().getId()).isEqualTo(seatId);
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        verify(tokenCommandService).complete(tokenId);
    }

    @Test
    @DisplayName("이미 예약된 좌석이면 예외 발생")
    void reserve_fail_when_already_reserved() {
        // given
        String tokenId = "token-1";
        Long seatId = 2L;

        when(tokenCommandService.status(tokenId)).thenReturn(Optional.of(0));

        CreateReservationCommand command = new CreateReservationCommand(tokenId, 1L, seatId, BigDecimal.valueOf(10000));
        Concert concert = new Concert("테스트 콘서트", 1, ConcertStatus.READY, LocalDateTime.now());

        ConcertSeat seat = ConcertSeat.withAll(
                seatId, concert, "A1", "1층", "A", "VIP",
                BigDecimal.valueOf(10000), SeatStatus.AVAILABLE, LocalDateTime.now()
        );

        when(concertSeatRepository.findByIdWithOptimistic(seatId)).thenReturn(Optional.of(seat));
        when(reservationRepository.save(any()))
                .thenThrow(new org.springframework.orm.ObjectOptimisticLockingFailureException("Reservation", null));

        // expect
        assertThatThrownBy(() -> reservationCommandService.reserve(command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.ALREADY_RESERVED.getMessage());
    }

    @Test
    @DisplayName("예약을 정상적으로 취소한다")
    void cancel_success() {
        // given
        Long reservationId = 10L;
        Reservation reservation = mock(Reservation.class);
        Reservation canceled = mock(Reservation.class);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservation.cancel()).thenReturn(canceled);
        when(reservationRepository.save(canceled)).thenReturn(canceled);

        // when
        Reservation result = reservationCommandService.cancel(reservationId);

        // then
        assertThat(result).isEqualTo(canceled);
        verify(reservationRepository).findById(reservationId);
        verify(reservationRepository).save(canceled);
    }

    @Test
    @DisplayName("예약 취소 시 예약이 존재하지 않으면 예외 발생")
    void cancel_fail_when_not_found() {
        // given
        Long reservationId = 99L;
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> reservationCommandService.cancel(reservationId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("예약 정보를 찾을 수 없습니다.");
    }
}
