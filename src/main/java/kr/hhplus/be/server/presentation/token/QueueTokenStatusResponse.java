package kr.hhplus.be.server.presentation.token;

import kr.hhplus.be.server.domain.token.TokenStatus;

public record QueueTokenStatusResponse(
        String tokenId,
        Long userId,
        TokenStatus status,
        Integer position
) {
}
