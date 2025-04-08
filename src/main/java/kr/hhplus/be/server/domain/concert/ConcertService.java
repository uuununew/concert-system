package kr.hhplus.be.server.domain.concert;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final ConcertSeatRepository concertSeatRepository;

    /**
     * 공연 전체 목록 조회
     */
    public List<Concert> getConcertList(){
        return concertRepository.findAll();
    }

    /**
     * 공연 단건 조회
     */
    public Concert getConcertById(Long concertId){
        return concertRepository.findById(concertId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 콘서트를 찾을 수 없습니다."));
    }

    /**
     * 공연의 좌석 목록 조회
     * - 공연 존재 여부 확인 포함
     */
    public List<ConcertSeat> getSeats(Long concertId){
        Concert concert = getConcertById(concertId); //공연 존재 여부 확인
        return concertSeatRepository.findAllByConcertId(concertId);
    }

    /**
     * OPENED 상태인 공연 목록만 조회
     */
    public List<Concert> getOpenedConcerts() {
        return concertRepository.findAll().stream()
                .filter(Concert::isOpened)
                .toList();
    }

    /**
     * 공연 ID 기준 예약 가능한 좌석 목록 조회 (AVAILABLE 상태만)
     */
    public List<ConcertSeat> getAvailableSeats(Long concertId){
        getConcertById(concertId); //공연 존재 여부 확인
        return concertSeatRepository.findAllByConcertId(concertId).stream()
                .filter(ConcertSeat::isAvailable)
                .toList();
    }

    /**
     * 공연 좌석 총 개수 반환
     */
    public int getTotalSeatCount(Long concertId) {
        getConcertById(concertId); // 공연 존재 여부 확인
        return concertSeatRepository.findAllByConcertId(concertId).size();
    }

}
