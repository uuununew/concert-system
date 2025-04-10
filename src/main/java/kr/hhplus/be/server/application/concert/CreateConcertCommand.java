package kr.hhplus.be.server.application.concert;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertStatus;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import lombok.Getter;

import java.time.LocalDateTime;

public record CreateConcertCommand(
        String title,
        Integer round,
        ConcertStatus status,
        LocalDateTime concertDateTime
) {
    public CreateConcertCommand {
        // 비즈니스 유효성 검증
        if (title == null || title.trim().length() <= 1) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "제목은 1자 이상이어야 합니다.");
        }
        if (round == null || round <= 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "회차는 1 이상이어야 합니다.");
        }
        if (status == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "상태는 필수입니다.");
        }
        if (concertDateTime == null || concertDateTime.isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "콘서트 시작 시간은 현재 이후여야 합니다.");
        }
    }

    public Concert toEntity() {
        return Concert.create(title, round, status, concertDateTime);
    }
}