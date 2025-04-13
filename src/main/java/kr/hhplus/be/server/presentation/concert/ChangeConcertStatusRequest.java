package kr.hhplus.be.server.presentation.concert;

import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.domain.concert.ConcertStatus;

public record ChangeConcertStatusRequest(
        @NotNull(message = "새로운 상태는 필수입니다.")
        ConcertStatus newStatus
) {}
