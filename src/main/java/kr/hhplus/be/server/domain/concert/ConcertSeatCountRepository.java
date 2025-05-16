package kr.hhplus.be.server.domain.concert;

public interface ConcertSeatCountRepository {
    /**
     * 콘서트 좌석 수를 1 감소시킵니다.
     * @param concertId 콘서트 ID
     * @return 감소 후 남은 좌석 수
     */
    long decrementRemainCount(Long concertId);

    /**
     * 콘서트 좌석 수를 1 증가시킵니다.
     * @param concertId 콘서트 ID
     * @return 증가 후 좌석 수
     */
    long incrementRemainCount(Long concertId);

    /**
     * 좌석 수를 조회합니다.
     */
    long getRemainCount(Long concertId);
}
