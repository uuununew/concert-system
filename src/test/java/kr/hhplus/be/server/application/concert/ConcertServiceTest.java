package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.ConcertStatus;
import kr.hhplus.be.server.support.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConcertServiceTest {

    @Mock
    private ConcertRepository concertRepository;

    @InjectMocks
    private ConcertService concertService;

    @Test
    @DisplayName("콘서트를 정상 등록한다.")
    void register_concert_success() {
        // given
        CreateConcertCommand command = new CreateConcertCommand("워터밤", 1, ConcertStatus.OPENED, LocalDateTime.now().plusDays(1));
        Concert concert = new Concert("워터밤", 1, ConcertStatus.OPENED, LocalDateTime.now().plusDays(1));
        when(concertRepository.save(any())).thenReturn(concert);
        // when
        Concert result = concertService.registerConcert(command);

        // then
        assertEquals("워터밤", result.getTitle());
        verify(concertRepository).save(any());
    }

    @DisplayName("전체 콘서트를 조회할 수 있다")
    @Test
    void get_all_concerts() {
        // given : 저장되어 있는 콘서트 2개
        Concert concert1 = new Concert("BTS - 서울", 1, ConcertStatus.OPENED, LocalDateTime.now().plusDays(1));
        Concert concert2 = new Concert("BTS - 부산", 2, ConcertStatus.CLOSED, LocalDateTime.now().plusDays(2));
        List<Concert> concerts = Arrays.asList(concert1, concert2);

        when(concertRepository.findAll()).thenReturn(concerts);

        // when : 전체 콘서트 목록 조회
        List<Concert> result = concertService.getConcertList();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("BTS - 서울");
        assertThat(result.get(1).getTitle()).isEqualTo("BTS - 부산");
        verify(concertRepository).findAll();
    }

    @DisplayName("ID로 콘서트를 조회할 수 있다")
    @Test
    void get_concert_by_id_success() {
        // given : ID 1L인 콘서트가 저장되어 있다고 가정
        Concert concert = new Concert("IU", 1, ConcertStatus.OPENED, LocalDateTime.now().plusDays(1));
        when(concertRepository.findById(1L)).thenReturn(Optional.of(concert));

        // when : ID 1L로 콘서트 조회
        Concert result = concertService.getConcertById(1L);

        // then : 콘서트 제목이 "IU"인지 확인
        assertThat(result.getTitle()).isEqualTo("IU");
    }

    @DisplayName("존재하지 않는 ID로 조회 시 예외 발생")
    @Test
    void get_concert_by_id_fail() {
        // given : ID 99L에 해당하는 콘서트가 없음
        when(concertRepository.findById(99L)).thenReturn(Optional.empty());

        // when//then : 예외 발
        assertThatThrownBy(() -> concertService.getConcertById(99L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("해당 ID의 콘서트를 찾을 수 없습니다.");
    }

    @DisplayName("콘서트 상태 변경 테스트")
    @Test
    void change_concert_status_success() {
        // given
        Long concertId = 1L;
        Concert concert = new Concert("테스트 공연", 1, ConcertStatus.OPENED, LocalDateTime.now().plusDays(1));

        when(concertRepository.findById(concertId)).thenReturn(Optional.of(concert));
        when(concertRepository.save(any())).thenReturn(concert);

        // when : 상태를 CLOSED로 변경
        concertService.changeConcertStatus(concertId, ConcertStatus.CLOSED);

        // then : 상태 변경 확인
        assertEquals(ConcertStatus.CLOSED, concert.getStatus());
        verify(concertRepository).save(concert);
    }

    @DisplayName("OPENED 상태의 콘서트만 필터링하여 조회한다")
    @Test
    void get_opened_concerts() {
        // given
        List<Concert> concerts = Arrays.asList(
                new Concert("BLACKPINK - 서울", 1, ConcertStatus.OPENED, LocalDateTime.now().plusDays(1)),
                new Concert("BLACKPINK - 부산", 2, ConcertStatus.CLOSED, LocalDateTime.now().plusDays(2)),
                new Concert("BLACKPINK - 고양", 3, ConcertStatus.OPENED, LocalDateTime.now().plusDays(3))
        );
        when(concertRepository.findAll()).thenReturn(concerts);

        // when : OPEND 상태 콘서트만 필터링
        List<Concert> result = concertService.getOpenedConcerts();

        // then : 총 2개 반환, 모두 OPEN인지 확인
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStatus()).isEqualTo(ConcertStatus.OPENED);
        assertThat(result.get(1).getStatus()).isEqualTo(ConcertStatus.OPENED);
    }

    @DisplayName("OPENED 상태가 하나도 없으면 빈 리스트를 반환한다")
    @Test
    void get_opened_concerts_empty() {
        // given : 모두 CLOSED 상태
        List<Concert> concerts = List.of(
                new Concert("ABC 콘서트", 1, ConcertStatus.CLOSED, LocalDateTime.now().plusDays(1)),
                new Concert("DEF 콘서트", 2, ConcertStatus.CLOSED, LocalDateTime.now().plusDays(2))
        );
        when(concertRepository.findAll()).thenReturn(concerts);

        // when
        List<Concert> result = concertService.getOpenedConcerts();

        // then
        assertThat(result).isEmpty();
    }
}
