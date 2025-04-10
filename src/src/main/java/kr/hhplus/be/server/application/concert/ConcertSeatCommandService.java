package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.ConcertSeat;
import kr.hhplus.be.server.domain.concert.ConcertSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 콘서트 좌석 등록 및 수정 등 변경 로직을 담당하는 Command 전용 서비스
 */
@Service
@RequiredArgsConstructor
public class ConcertSeatCommandService {

    private final ConcertRepository concertRepository;
    private final ConcertSeatRepository concertSeatRepository;

    /**
     * 좌석을 등록합니다.
     * 콘서트가 존재하지 않으면 예외가 발생합니다.
     *
     * @param command 좌석 생성 커맨드
     * @return 생성된 좌석
     */
    public ConcertSeat registerSeat(CreateConcertSeatCommand command) {
        Concert concert = concertRepository.findById(command.concertId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 ID의 콘서트가 존재하지 않습니다."));

        ConcertSeat seat = ConcertSeat.of(
                concert.getId(),
                command.seatNumber(),
                command.section(),
                command.row(),
                command.grade(),
                command.price()
        );

        return concertSeatRepository.save(seat);
    }

    /**
     * 좌석 정보를 수정합니다.
     * 좌석이 존재하지 않으면 예외가 발생합니다.
     *
     * @param seatId 좌석 ID
     * @param command 수정할 정보가 담긴 커맨드
     * @return 수정된 좌석
     */
    public ConcertSeat updateSeat(Long seatId, UpdateConcertSeatCommand command) {
        ConcertSeat seat = concertSeatRepository.findById(seatId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 좌석이 존재하지 않습니다."));

        seat.updateSeatInfo(
                command.seatNumber(),
                command.section(),
                command.row(),
                command.grade(),
                command.price()
        );

        return concertSeatRepository.save(seat);
    }
}
