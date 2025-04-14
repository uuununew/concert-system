package kr.hhplus.be.server.application.concert.reservation;

import kr.hhplus.be.server.application.token.TokenCommandService;
import kr.hhplus.be.server.domain.concert.*;
import kr.hhplus.be.server.domain.concert.reservation.Reservation;
import kr.hhplus.be.server.domain.concert.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.concert.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.support.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ConcertSeatRepository concertSeatRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private TokenCommandService tokenCommandService;

    private ReservationCommandService reservationCommandService;

    @BeforeEach
    void setUp() {
        reservationCommandService = new ReservationCommandService(
                tokenCommandService,
                reservationRepository,
                concertSeatRepository,
                tokenRepository
        );
    }


    @Test
    @DisplayName("예약을 정상적으로 등록한다")
    void reserve_success() {
        // given
        CreateReservationCommand command = new CreateReservationCommand(1L, 2L, BigDecimal.valueOf(10000));

        Concert concert = new Concert("테스트 콘서트", 1, ConcertStatus.READY, LocalDateTime.now());

        ConcertSeat seat = ConcertSeat.withAll(
                2L, concert, "A1", "1층", "A", "VIP", BigDecimal.valueOf(10000), SeatStatus.AVAILABLE, LocalDateTime.now());

        when(reservationRepository.findByConcertSeatIdAndStatus(2L, ReservationStatus.RESERVED))
                .thenReturn(Optional.empty());
        when(concertSeatRepository.findById(2L))
                .thenReturn(Optional.of(seat));
        when(reservationRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Reservation result = reservationCommandService.reserve(command);

        // then
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getConcertSeatId()).isEqualTo(2L);
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        verify(reservationRepository).save(any());
    }

    @Test
    @DisplayName("이미 예약된 좌석이면 예외 발생")
    void reserve_fail_when_already_reserved() {
        // given
        CreateReservationCommand command = new CreateReservationCommand(1L, 2L, BigDecimal.valueOf(10000));
        when(reservationRepository.findByConcertSeatIdAndStatus(2L, ReservationStatus.RESERVED))
                .thenReturn(Optional.of(mock(Reservation.class)));

        // expect
        assertThatThrownBy(() -> reservationCommandService.reserve(command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이미 예약된 좌석입니다.");
    }

    @Test
    @DisplayName("예약을 정상적으로 취소한다")
    void cancel_success() {
        // given
        Long reservationId = 10L;
        Reservation mockReservation = mock(Reservation.class);
        when(mockReservation.cancel()).thenReturn(mock(Reservation.class));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(mockReservation));
        when(reservationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Reservation result = reservationCommandService.cancel(reservationId);

        // then
        verify(reservationRepository).findById(reservationId);
        verify(mockReservation).cancel();
        verify(reservationRepository).save(any());
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
