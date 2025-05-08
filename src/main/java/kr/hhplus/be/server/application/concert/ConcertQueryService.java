package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.presentation.concert.ConcertResponse;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertQueryService {

    private final ConcertCacheService concertCacheService;

    public List<ConcertResponse> getAllConcerts(Pageable pageable) {
        List<ConcertResponse> concerts = concertCacheService.getAllConcertResponses();
        validatePageable(pageable, concerts.size());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), concerts.size());
        return concerts.subList(start, end);
    }

    private void validatePageable(Pageable pageable, int totalSize) {
        if (pageable.getPageSize() <= 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "페이지 크기는 1 이상이어야 합니다.");
        }
        if (pageable.getOffset() >= totalSize) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "요청한 페이지가 데이터 범위를 벗어났습니다.");
        }
    }
}