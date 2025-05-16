package kr.hhplus.be.server.support.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import jakarta.servlet.Filter;

@Slf4j
public class LoggingFilter implements Filter {

    private static final String REQUEST_ID = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Request, Response를 ContentCachingWrapper로 감싸서 content를 여러번 읽을 수 있도록 함
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper((HttpServletRequest) request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper((HttpServletResponse) response);

        // 각 요청마다 고유 식별자 생성 → 로그 추적에 활용
        String requestId = UUID.randomUUID().toString();
        MDC.put(REQUEST_ID, requestId);

        long startTime = System.currentTimeMillis();

        try {
            // 요청 처리
            chain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            logRequest(requestWrapper);
            logResponse(responseWrapper);
            log.info("Request ID: {} | Duration: {}ms", requestId, duration);

            responseWrapper.copyBodyToResponse(); // 바디 내용 복사
            MDC.clear();
        }
    }

    /**
     * 요청 정보를 로깅한다.
     * - GET은 바디를 제외하고 출력
     * - 나머지는 바디까지 포함하여 출력
     */
    private void logRequest(ContentCachingRequestWrapper request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        try {
            request.getParameterMap(); // 또는 request.getInputStream().readAllBytes();
        } catch (Exception e) {
            log.warn("Request content 초기화 실패", e);
        }

        if ("GET".equalsIgnoreCase(method)) {
            log.info("[Request] {} {}", method, uri);
        } else {
            String body = new String(request.getContentAsByteArray(), StandardCharsets.UTF_8);
            log.info("[Request] {} {} | body={}", method, uri, body);
        }
    }

    /**
     * 응답 정보를 로깅한다.
     * - 상태 코드 및 응답 바디 출력
     */
    private void logResponse(ContentCachingResponseWrapper response) {
        String body = new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);
        int status = response.getStatus();

        log.info("[Response] status={} | body={}", status, body);
    }
}
