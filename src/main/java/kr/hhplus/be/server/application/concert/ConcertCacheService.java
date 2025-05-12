package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.presentation.concert.ConcertResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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
}
