package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.domain.concert.ranking.ConcertRankingRepository;
import kr.hhplus.be.server.domain.concert.ranking.ConcertRankingResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConcertRankingServiceTest {

    @InjectMocks
    private ConcertRankingService concertRankingService;

    @Mock
    private ConcertRankingRepository concertRankingRepository;

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

        // when
        List<ConcertRankingResult> result = concertRankingService.getTopConcerts(2);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).concertId()).isEqualTo(1L);
        assertThat(result.get(0).soldOutDurationMillis()).isEqualTo(7200000.0);
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