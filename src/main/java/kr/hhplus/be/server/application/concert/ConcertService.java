package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.ConcertStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertService {

    private final ConcertRepository concertRepository;

    /**
     * 콘서트 등록
     */
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
    public void changeConcertStatus(Long concertId, ConcertStatus newStatus) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 ID의 콘서트를 찾을 수 없습니다."));

        concert.changeStatus(newStatus);
        concertRepository.save(concert);
    }

    /**
     * 공연 전체 목록 조회
     */
    public List<Concert> getConcertList() {
        return concertRepository.findAll();
    }

    /**
     * 특정 id의 공연 조회
     */
    public Concert getConcertById(Long id) {
        return concertRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 ID의 콘서트를 찾을 수 없습니다."));
    }

    /**
     * OPENED 상태인 공연 목록만 조회
     */
    public List<Concert> getOpenedConcerts() {
        return concertRepository.findAll().stream()
                .filter(Concert::isOpened)
                .toList();
    }

}
