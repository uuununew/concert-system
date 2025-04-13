package kr.hhplus.be.server.application.concert.reservation;

import kr.hhplus.be.server.domain.concert.ConcertSeat;
import kr.hhplus.be.server.domain.concert.ConcertSeatRepository;
import kr.hhplus.be.server.domain.concert.reservation.Reservation;
import kr.hhplus.be.server.domain.concert.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.concert.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.application.token.TokenCommandService;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationCommandService {

    private final ReservationRepository reservationRepository;
    private final ConcertSeatRepository concertSeatRepository;
    private final TokenCommandService tokenCommandService;
    private final TokenRepository tokenRepository;

    /**
     * 좌석 예약 처리
     * - 해당 좌석에 이미 예약이 있는지 확인
     * - 없다면 예약 생성 후 저장
     */
    public Reservation reserve(CreateReservationCommand command) {
        // 1: 토큰 검증 및 활성화
        try {
            tokenCommandService.activate(command.userId());
        } catch (IllegalStateException e) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "유효하지 않은 토큰입니다: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.NOT_FOUND, "토큰 정보가 없습니다.");
        }

        // 2: 이미 예약된 좌석인지 확인
        boolean alreadyReserved = reservationRepository
                .findByConcertSeatIdAndStatus(command.concertSeatId(), ReservationStatus.RESERVED)
                .isPresent();

        if (alreadyReserved) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "이미 예약된 좌석입니다.");
        }

        // 3 : concertSeat 객체 조회
        ConcertSeat seat = concertSeatRepository.findById(command.concertSeatId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 좌석 정보를 찾을 수 없습니다."));

        // 4 : 예약 생성 (User는 임시 도메인으로 ID 기반 생성)
        User user = User.from(command.userId());
        Reservation reservation = Reservation.create(user, seat, command.price());


        // 5: 토큰 사용 완료 처리
        tokenCommandService.complete(command.userId());

        return reservationRepository.save(reservation);
    }

    /**
     * 예약 취소 처리
     */
    public Reservation cancel(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "예약 정보를 찾을 수 없습니다."));

        // 1: 토큰 복구
        try {
            tokenCommandService.restore(reservation.getUserId());
        } catch (Exception e) {
            // 복구 실패해도 예약 취소는 문제없이 진행되어야 하므로 로깅 처리만
            System.out.println("토큰 복구 실패: " + e.getMessage());
        }

        // 2: 예약 취소 및 저장
        return reservationRepository.save(reservation.cancel());
    }

    /**
     * 유저의 토큰 상태를 조회합니다.
     * @param userId 유저 식별자
     * @return Optional<QueueToken> 존재하면 반환, 없으면 empty
     */
    public Optional<QueueToken> status(Long userId) {
        return tokenRepository.findByUserId(userId);
    }
}
