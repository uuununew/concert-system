package kr.hhplus.be.server.support.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.hhplus.be.server.application.token.TokenCommandService;
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
        String tokenId = request.getHeader("X-TOKEN-ID");

        if (tokenId == null || tokenId.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "토큰 ID가 없습니다.");
        }

        // Redis ZSet에 포함되어 있으면 아직 WAITING 상태 -> ACTIVE가 아님
        Optional<Integer> position = tokenCommandService.status(tokenId);
        if (position.isPresent()) {
            throw new CustomException(ErrorCode.TOKEN_NOT_FOUND, "활성화되지 않은 토큰입니다.");
        }
        return true;
    }
}
