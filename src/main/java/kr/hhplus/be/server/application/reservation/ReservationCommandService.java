package kr.hhplus.be.server.application.reservation;

import kr.hhplus.be.server.domain.concert.ConcertSeat;
import kr.hhplus.be.server.domain.concert.ConcertSeatRepository;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.application.token.TokenCommandService;
import kr.hhplus.be.server.domain.token.TokenRepository;
import kr.hhplus.be.server.domain.token.TokenStatus;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import kr.hhplus.be.server.support.lock.RedisSimpleLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationCommandService {

    private final TokenCommandService tokenCommandService;
    private final ReservationRepository reservationRepository;
    private final ConcertSeatRepository concertSeatRepository;
    private final TokenRepository tokenRepository;

    /**
     * 좌석 예약 처리
     * - 해당 좌석에 이미 예약이 있는지 확인
     * - 없다면 예약 생성 후 저장
     */
    @Transactional
    @RedisSimpleLock(key = "'seat:' + #command.concertSeatId()")
    public Reservation reserve(CreateReservationCommand command) {
        // 1: 토큰 검증 및 활성화
        QueueToken token = tokenCommandService.status(command.userId())
                .orElseThrow(() -> new CustomException(ErrorCode.TOKEN_NOT_FOUND, "토큰 정보가 없습니다."));

        if (token.getStatus() != TokenStatus.ACTIVE) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "아직 대기열이 활성화되지 않았습니다.");
        }
        // 2: concertSeat 객체 조회
        ConcertSeat seat = concertSeatRepository.findByIdWithOptimistic(command.concertSeatId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 좌석 정보를 찾을 수 없습니다."));

        // 3: 좌석 상태 변경
        seat.reserve();

        // 4 : 예약 생성
        User user = User.from(command.userId());
        Reservation reservation = Reservation.create(user, seat, command.price());

        // 5: 예약 저장 및 충돌 처리
        try {
            Reservation saved = reservationRepository.save(reservation);
            tokenCommandService.complete(command.userId());
            return saved;
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            throw new CustomException(ErrorCode.ALREADY_RESERVED);
        }
    }

    /**
     * 예약 취소 처리
     */
    @Transactional
    public Reservation cancel(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "예약 정보를 찾을 수 없습니다."));

        // 1: 토큰 복구
        try {
            tokenCommandService.restore(reservation.getUserId());
        } catch (Exception e) {
            log.info("토큰 복구 실패: {}", e.getMessage());
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
