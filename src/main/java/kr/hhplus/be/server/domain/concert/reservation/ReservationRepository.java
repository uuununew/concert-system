package kr.hhplus.be.server.domain.concert.reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    /**
     * 예약 저장
     */
    Reservation save(Reservation reservation);

    /**
     * 좌석 ID와 상태로 예약 조회
     * - 해당 좌석에 대한 예약이 이미 존재하는지 확인할 때 사용
     */
    Optional<Reservation> findByConcertSeatIdAndStatus(Long concertSeatId, ReservationStatus status);

    /**
     * ID로 예약 조회
     */
    Optional<Reservation> findById(Long id);

    /**
     * 특정 상태이며 지정된 시간 이전에 생성된 모든 예약을 조회
     * - 임시 예약 만료 처리(스케줄러)에 사용됩니다.
     */
    List<Reservation> findAllByStatusAndCreatedAtBefore(ReservationStatus status, LocalDateTime dateTime);
}
