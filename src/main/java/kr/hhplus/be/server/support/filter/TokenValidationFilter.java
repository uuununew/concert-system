package kr.hhplus.be.server.support.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

public class TokenValidationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 헤더에서 userId 읽기
        String userId = httpRequest.getHeader("X-USER-ID");

        if (userId == null || userId.isBlank()) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "userId가 필요합니다.");
            return;
        }

        // request attribute로 전달
        httpRequest.setAttribute("userId", Long.valueOf(userId));

        chain.doFilter(request, response);
    }
}
