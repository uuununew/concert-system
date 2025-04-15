package kr.hhplus.be.server.application.concert.reservation;

import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertSeat;
import kr.hhplus.be.server.domain.concert.SeatStatus;
import kr.hhplus.be.server.domain.concert.reservation.Reservation;
import kr.hhplus.be.server.domain.concert.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.concert.reservation.ReservationScheduleService;
import kr.hhplus.be.server.domain.concert.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReservationSchedulerServiceTest {
    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationScheduleService reservationScheduleService;

    @BeforeEach
    void setUp() {
        reservationScheduleService = new ReservationScheduleService(reservationRepository);
    }

    @Test
    @DisplayName("예약 스케줄러가 정상적으로 호출되어 예약을 취소한다")
    void cancel_expired_reservations_success() {
        // given
        Reservation expired = mock(Reservation.class);
        given(reservationRepository.findAllByStatusAndCreatedAtBefore(
                eq(ReservationStatus.RESERVED),
                any(LocalDateTime.class)
        )).willReturn(List.of(expired));

        given(expired.cancel()).willReturn(expired);

        // when
        reservationScheduleService.cancelReservationsBefore(LocalDateTime.now());

        // then
        verify(reservationRepository).save(expired);
    }

    @Test
    @DisplayName("미결제 예약을 취소 처리한다")
    void cancel_unpaid_reservations_success() {
        // given
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(10);
        BigDecimal price1 = BigDecimal.valueOf(10000);

        User user = new User(1L);

        Concert concert = Concert.withStatus(kr.hhplus.be.server.domain.concert.ConcertStatus.READY);
        ConcertSeat seat = ConcertSeat.withAll(
                10L, concert, "A1", "1층", "A", "VIP", price1, SeatStatus.AVAILABLE, LocalDateTime.now());

        Reservation reservation1 = Reservation.create(user, seat, price1);

        when(reservationRepository.findAllByStatusAndCreatedAtBefore(ReservationStatus.RESERVED, cutoffTime))
                .thenReturn(List.of(reservation1));

        // when
        reservationScheduleService.cancelReservationsBefore(cutoffTime);

        // then
        verify(reservationRepository, times(1)).save(argThat(res ->
                res.getUserId().equals(reservation1.getUserId()) &&
                        res.getConcertSeat().equals(reservation1.getConcertSeat()) &&
                        res.getStatus() == ReservationStatus.CANCELED
        ));
    }
}
