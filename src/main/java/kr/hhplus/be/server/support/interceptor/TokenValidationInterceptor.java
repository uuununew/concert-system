package kr.hhplus.be.server.support.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.hhplus.be.server.application.token.TokenCommandService;
import kr.hhplus.be.server.domain.token.QueueToken;
import kr.hhplus.be.server.domain.token.TokenStatus;
import kr.hhplus.be.server.support.exception.CustomException;
import kr.hhplus.be.server.support.exception.ErrorCode;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

public class TokenValidationInterceptor implements HandlerInterceptor {

    private final TokenCommandService tokenCommandService;

    public TokenValidationInterceptor(TokenCommandService tokenCommandService) {
        this.tokenCommandService = tokenCommandService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userIdHeader = request.getHeader("X-USER-ID");

        if (userIdHeader == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "유저 ID가 없습니다.");
        }

        Long userId;
        try {
            userId = Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "유효하지 않은 유저 ID입니다.");
        }

        Optional<QueueToken> tokenOpt = tokenCommandService.status(userId);

        if (tokenOpt.isEmpty() || tokenOpt.get().getStatus() != TokenStatus.ACTIVE) {
            throw new CustomException(ErrorCode.TOKEN_NOT_FOUND, "유효한 토큰이 없습니다.");
        }

        return true;
    }
}
