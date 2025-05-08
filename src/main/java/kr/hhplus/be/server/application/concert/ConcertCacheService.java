package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.presentation.concert.ConcertResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertCacheService {

    private final ConcertRepository concertRepository;

    private static final String CACHE_NAME = "concertAll";

    @Cacheable(value = CACHE_NAME)
    public List<ConcertResponse> getAllConcertResponses() {
        return concertRepository.findAll().stream()
                .map(ConcertResponse::from)
                .toList();
    }
}
