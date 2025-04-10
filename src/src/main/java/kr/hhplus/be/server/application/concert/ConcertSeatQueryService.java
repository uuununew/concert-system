package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.ConcertSeat;
import kr.hhplus.be.server.domain.concert.ConcertSeatRepository;
import kr.hhplus.be.server.domain.concert.SeatStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 콘서트 좌석 관련 조회 로직을 담당하는 Query 전용 서비스입니다.
 * - 전체 좌석 조회
 * - 예약 가능한 좌석 조회
 * - 좌석 개수 조회
 */
@Service
@RequiredArgsConstructor
public class ConcertSeatQueryService {

    private final ConcertRepository concertRepository;
    private final ConcertSeatRepository concertSeatRepository;

    /**
     * 특정 콘서트의 전체 좌석을 조회합니다.
     *
     * @param concertId 콘서트 ID
     * @return 해당 콘서트의 전체 좌석 목록
     */
    public List<ConcertSeat> getSeats(Long concertId) {
        validateConcert(concertId);
        return concertSeatRepository.findAllByConcertId(concertId);
    }

    /**
     * 특정 콘서트의 예약 가능한 좌석만 조회합니다.
     *
     * @param concertId 콘서트 ID
     * @return 예약 가능한 좌석 목록
     */
    public List<ConcertSeat> getAvailableSeats(Long concertId) {
        validateConcert(concertId);
        return concertSeatRepository.findAllByConcertIdAndStatus(concertId, SeatStatus.AVAILABLE);
    }

    /**
     * 특정 콘서트의 전체 좌석 수를 반환합니다.
     *
     * @param concertId 콘서트 ID
     * @return 좌석 총 개수
     */
    public int getTotalSeatCount(Long concertId) {
        validateConcert(concertId);
        return concertSeatRepository.findAllByConcertId(concertId).size();
    }

    /**
     * 콘서트 ID의 존재 여부를 확인합니다.
     * 존재하지 않는 경우 NotFoundException 예외를 발생시킵니다.
     *
     * @param concertId 콘서트 ID
     */
    private void validateConcert(Long concertId) {
        if (!concertRepository.existsById(concertId)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "해당 콘서트가 존재하지 않습니다.");
        }
    }
}
