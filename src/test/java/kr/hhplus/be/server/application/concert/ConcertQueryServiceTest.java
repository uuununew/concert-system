package kr.hhplus.be.server.application.concert;


import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.ConcertStatus;
import kr.hhplus.be.server.presentation.concert.ConcertResponse;
import kr.hhplus.be.server.support.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConcertQueryServiceTest {

    private ConcertCacheService concertCacheService;
    private ConcertQueryService concertQueryService;

    @BeforeEach
    void setUp() {
        concertCacheService = mock(ConcertCacheService.class);
        concertQueryService = new ConcertQueryService(concertCacheService);
    }

    @Test
    @DisplayName("전체 콘서트 조회 (캐시 적용된 서비스 호출) - 성공")
    void getAllConcerts_success() {
        // given
        List<ConcertResponse> cachedResponses = List.of(
                new ConcertResponse(1L, "BTS", 1, ConcertStatus.OPENED, LocalDateTime.now()),
                new ConcertResponse(2L, "BLACKPINK", 1, ConcertStatus.OPENED, LocalDateTime.now())
        );
        when(concertCacheService.getAllConcertResponses()).thenReturn(cachedResponses);

        Pageable pageable = PageRequest.of(0, 2);

        // when
        List<ConcertResponse> result = concertQueryService.getAllConcerts(pageable);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("BTS");
        verify(concertCacheService, times(1)).getAllConcertResponses();
    }

    @Test
    @DisplayName("페이지 유효 - 정상 조회 (subList 확인)")
    void getAllConcerts_validPage() {
        // given
        List<ConcertResponse> cachedResponses = List.of(
                new ConcertResponse(1L, "BTS", 1, ConcertStatus.OPENED, LocalDateTime.now()),
                new ConcertResponse(2L, "BLACKPINK", 1, ConcertStatus.OPENED, LocalDateTime.now()),
                new ConcertResponse(3L, "IU", 1, ConcertStatus.OPENED, LocalDateTime.now())
        );
        when(concertCacheService.getAllConcertResponses()).thenReturn(cachedResponses);

        Pageable pageable = PageRequest.of(0, 2);

        // when
        List<ConcertResponse> result = concertQueryService.getAllConcerts(pageable);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("BTS");
        assertThat(result.get(1).getTitle()).isEqualTo("BLACKPINK");
    }

    @Test
    @DisplayName("페이지 크기 <= 0 → CustomException 발생")
    void validatePageable_invalidSize() {
        // given
        Pageable pageable = Mockito.mock(Pageable.class);
        when(pageable.getPageSize()).thenReturn(0);
        when(pageable.getOffset()).thenReturn(0L);

        List<ConcertResponse> cachedResponses = List.of(
                new ConcertResponse(1L, "BTS", 1, ConcertStatus.OPENED, LocalDateTime.now())
        );
        when(concertCacheService.getAllConcertResponses()).thenReturn(cachedResponses);

        // when // then
        assertThatThrownBy(() -> concertQueryService.getAllConcerts(pageable))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("페이지 크기는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("페이지 offset >= totalSize → CustomException 발생")
    void validatePageable_offsetExceedsTotal() {
        // given
        List<ConcertResponse> cachedResponses = List.of(
                new ConcertResponse(1L, "BTS", 1, ConcertStatus.OPENED, LocalDateTime.now())
        );
        when(concertCacheService.getAllConcertResponses()).thenReturn(cachedResponses);

        Pageable pageable = PageRequest.of(1, 1); // offset = 1, totalSize = 1 → 범위 벗어남

        // when // then
        assertThatThrownBy(() -> concertQueryService.getAllConcerts(pageable))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("요청한 페이지가 데이터 범위를 벗어났습니다.");
    }
}
