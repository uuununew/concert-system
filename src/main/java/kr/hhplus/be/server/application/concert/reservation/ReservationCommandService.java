package kr.hhplus.be.server.application.concert.reservation;

import kr.hhplus.be.server.domain.concert.reservation.Reservation;
import kr.hhplus.be.server.domain.concert.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.concert.reservation.ReservationStatus;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationCommandService {

    private final ReservationRepository reservationRepository;

    /**
     * 좌석 예약 처리
     * - 해당 좌석에 이미 예약이 있는지 확인
     * - 없다면 예약 생성 후 저장
     */
    public Reservation reserve(CreateReservationCommand command) {
        // 이미 예약된 좌석인지 확인
        boolean alreadyReserved = reservationRepository
                .findByConcertSeatIdAndStatus(command.concertSeatId(), ReservationStatus.RESERVED)
                .isPresent();

        if (alreadyReserved) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "이미 예약된 좌석입니다.");
        }

        // 예약 생성 및 저장
        Reservation reservation = Reservation.create(
                command.userId(),
                command.concertSeatId(),
                command.price()
        );

        return reservationRepository.save(reservation);
    }

    /**
     * 예약 취소 처리
     */
    public Reservation cancel(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "예약 정보를 찾을 수 없습니다."));

        return reservationRepository.save(reservation.cancel());
    }
}
