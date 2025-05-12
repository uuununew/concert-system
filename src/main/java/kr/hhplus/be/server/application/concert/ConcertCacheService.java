package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.presentation.concert.ConcertResponse;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import static kr.hhplus.be.server.support.cache.CacheConstants.CONCERT_ALL_CACHE;
import static kr.hhplus.be.server.support.cache.CacheConstants.CONCERT_ALL_KEY;

@Service
@RequiredArgsConstructor
public class ConcertCacheService {

    private final ConcertRepository concertRepository;

    @Cacheable(value = CONCERT_ALL_CACHE, key = CONCERT_ALL_KEY)
    public List<ConcertResponse> getAllConcertResponses() {
        return concertRepository.findAll().stream()
                .map(ConcertResponse::from)
                .toList();
    }

    @Cacheable(value = CONCERT_ALL_CACHE, key = "'page:' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public List<ConcertResponse> getPagedConcertResponses(Pageable pageable) {
        List<ConcertResponse> all = getAllConcertResponses();
        validatePageable(pageable, all.size());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        return all.subList(start, end);
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
