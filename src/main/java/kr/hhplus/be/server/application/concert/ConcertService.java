package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.ConcertStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertService {

    private final ConcertRepository concertRepository;

    private static final String CACHE_NAME = "concertAll";

    /**
     * 콘서트 등록
     */
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public Concert registerConcert(CreateConcertCommand command) {
        Concert concert = new Concert(
                command.title(),
                command.round(),
                command.status(),
                command.concertDateTime()
        );
        return concertRepository.save(concert);
    }

    /**
     * 콘서트 상태 변경
     */
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void changeConcertStatus(Long concertId, ConcertStatus newStatus) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 ID의 콘서트를 찾을 수 없습니다."));

        switch (newStatus) {
            case OPENED -> concert.open();
            case CLOSED -> concert.close();
            case CANCELED -> concert.cancel();
            default -> throw new CustomException(ErrorCode.INVALID_CONCERT_STATUS, "허용되지 않은 콘서트 상태입니다.");
        }
        concertRepository.save(concert);
    }

    /**
     * 콘서트 삭제
     */
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void deleteConcert(Long concertId) {
        if (!concertRepository.existsById(concertId)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "해당 ID의 콘서트를 찾을 수 없습니다.");
        }
        concertRepository.deleteById(concertId);
    }

    /**
     * 공연 전체 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<Concert> getConcertList(Pageable pageable) {
        return concertRepository.findAll(pageable);
    }

    /**
     * 특정 id의 공연 조회
     */
    @Transactional(readOnly = true)
    public Concert getConcertById(Long id) {
        return concertRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 ID의 콘서트를 찾을 수 없습니다."));
    }

    /**
     * OPENED 상태인 공연 목록만 조회
     */
    @Transactional(readOnly = true)
    public List<Concert> getOpenedConcerts() {
        return concertRepository.findAll().stream()
                .filter(Concert::isOpened)
                .toList();
    }
}
