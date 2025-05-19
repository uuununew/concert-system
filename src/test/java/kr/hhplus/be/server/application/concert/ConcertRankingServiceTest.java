package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.ConcertStatus;
import kr.hhplus.be.server.domain.concert.ranking.ConcertRankingDetail;
import kr.hhplus.be.server.domain.concert.ranking.ConcertRankingRepository;
import kr.hhplus.be.server.domain.concert.ranking.ConcertRankingResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConcertRankingServiceTest {

    @InjectMocks
    private ConcertRankingService concertRankingService;

    @Mock
    private ConcertRankingRepository concertRankingRepository;

    @Mock
    private ConcertRepository concertRepository;

    @Test
    @DisplayName("매진 시점 기록이 랭킹 저장소에 정상적으로 기록된다")
    void recordSoldOutTime_success() {
        // given
        Long concertId = 1L;
        LocalDateTime openedAt = LocalDateTime.of(2025, 5, 15, 12, 0);
        LocalDateTime soldOutAt = LocalDateTime.of(2025, 5, 15, 14, 0);

        long expectedOpenMillis = openedAt.toEpochSecond(ZoneOffset.UTC) * 1000;
        long expectedSoldMillis = soldOutAt.toEpochSecond(ZoneOffset.UTC) * 1000;

        // when
        concertRankingService.recordSoldOutTime(concertId, expectedSoldMillis, expectedOpenMillis);

        // then
        verify(concertRankingRepository).saveSoldOutRanking(eq(concertId), eq(expectedSoldMillis), eq(expectedOpenMillis));
    }

    @Test
    @DisplayName("상위 N개의 콘서트 랭킹을 조회한다")
    void getTopConcerts_success() {
        // given
        List<ConcertRankingResult> rankings = List.of(
                new ConcertRankingResult(1L, 7200000.0),
                new ConcertRankingResult(2L, 7500000.0)
        );
        when(concertRankingRepository.getTopRankings(2)).thenReturn(rankings);

        Concert concert1 = mock(Concert.class);
        Concert concert2 = mock(Concert.class);

        when(concert1.getId()).thenReturn(1L);
        when(concert1.getTitle()).thenReturn("BTS 콘서트");
        when(concert1.getConcertDateTime()).thenReturn(LocalDateTime.of(2025, 5, 15, 20, 0));

        when(concert2.getId()).thenReturn(2L);
        when(concert2.getTitle()).thenReturn("IU 콘서트");
        when(concert2.getConcertDateTime()).thenReturn(LocalDateTime.of(2025, 5, 16, 18, 0));

        when(concertRepository.findById(1L)).thenReturn(Optional.of(concert1));
        when(concertRepository.findById(2L)).thenReturn(Optional.of(concert2));

        // when
        List<ConcertRankingDetail> result = concertRankingService.getTopConcertDetails(2);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).concertId()).isEqualTo(1L);
        assertThat(result.get(0).soldOutDurationMillis()).isEqualTo(7200000.0);
    }

    @Test
    @DisplayName("상위 콘서트 랭킹 상세 정보를 조회한다")
    void getTopConcertDetails_success() {
        // given
        ConcertRankingResult result1 = new ConcertRankingResult(1L, 5000.0);
        ConcertRankingResult result2 = new ConcertRankingResult(2L, 7000.0);
        when(concertRankingRepository.getTopRankings(2))
                .thenReturn(List.of(result1, result2));

        Concert concert1 = Concert.withStatus(ConcertStatus.READY);
        ReflectionTestUtils.setField(concert1, "id", 1L);
        ReflectionTestUtils.setField(concert1, "title", "BTS 콘서트");
        ReflectionTestUtils.setField(concert1, "concertDateTime", LocalDateTime.of(2025, 5, 20, 20, 0));

        Concert concert2 = Concert.withStatus(ConcertStatus.READY);
        ReflectionTestUtils.setField(concert2, "id", 2L);
        ReflectionTestUtils.setField(concert2, "title", "IU 콘서트");
        ReflectionTestUtils.setField(concert2, "concertDateTime", LocalDateTime.of(2025, 5, 20, 18, 30));


        when(concertRepository.findById(1L)).thenReturn(Optional.of(concert1));
        when(concertRepository.findById(2L)).thenReturn(Optional.of(concert2));

        // when
        List<ConcertRankingDetail> details = concertRankingService.getTopConcertDetails(2);

        // then
        assertThat(details).hasSize(2);

        assertThat(details.get(0).concertId()).isEqualTo(1L);
        assertThat(details.get(0).title()).isEqualTo("BTS 콘서트");
        assertThat(details.get(0).concertDateTime()).isEqualTo(concert1.getConcertDateTime());
        assertThat(details.get(0).soldOutDurationMillis()).isEqualTo(5000.0);

        assertThat(details.get(1).concertId()).isEqualTo(2L);
        assertThat(details.get(1).title()).isEqualTo("IU 콘서트");
        assertThat(details.get(1).concertDateTime()).isEqualTo(concert2.getConcertDateTime());
        assertThat(details.get(1).soldOutDurationMillis()).isEqualTo(7000.0);
    }

    @Test
    @DisplayName("오늘 랭킹 데이터를 초기화한다")
    void clearTodayRanking_success() {
        // when
        concertRankingService.clearTodayRanking();

        // then
        verify(concertRankingRepository).clearTodayRanking();
    }
}