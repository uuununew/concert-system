package kr.hhplus.be.server.application.concert;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import lombok.Getter;

import java.math.BigDecimal;

public record CreateConcertSeatCommand(
        Long concertId,
        String seatNumber,
        String section,
        String row,
        String grade,
        BigDecimal price
) {
    public CreateConcertSeatCommand {
        if (concertId == null || concertId <= 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "콘서트 ID는 필수이며 0보다 커야 합니다.");
        }
        if (seatNumber == null || seatNumber.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "좌석 번호는 필수입니다.");
        }
        if (section == null || section.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "구역 정보는 필수입니다.");
        }
        if (row == null || row.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "행 정보는 필수입니다.");
        }
        if (grade == null || grade.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "등급 정보는 필수입니다.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "가격은 0보다 커야 합니다.");
        }
    }
}