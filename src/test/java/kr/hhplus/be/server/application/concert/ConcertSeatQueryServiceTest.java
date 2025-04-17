package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.ConcertSeat;
import kr.hhplus.be.server.domain.concert.ConcertSeatRepository;
import kr.hhplus.be.server.support.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConcertSeatQueryServiceTest {

    @Mock
    private ConcertRepository concertRepository;

    @Mock
    private ConcertSeatRepository concertSeatRepository;

    @InjectMocks
    private ConcertSeatQueryService queryService;

    @Test
    @DisplayName("좌석 목록 전체 조회 성공")
    void get_all_seats_success() {
        // given
        Long concertId = 1L;
        when(concertRepository.existsById(concertId)).thenReturn(true);
        when(concertSeatRepository.findAllByConcert_Id(concertId)).thenReturn(List.of(mock(ConcertSeat.class)));

        // when
        List<ConcertSeat> seats = queryService.getSeats(concertId);

        // then
        assertThat(seats).hasSize(1);
    }

    @Test
    @DisplayName("좌석 조회 시 콘서트 없으면 예외")
    void get_seats_fail_when_concert_not_found() {
        // given
        Long concertId = 999L;
        when(concertRepository.existsById(concertId)).thenReturn(false);

        //when//then
        assertThatThrownBy(() -> queryService.getSeats(concertId))
                .isInstanceOf(CustomException.class);
    }

}
