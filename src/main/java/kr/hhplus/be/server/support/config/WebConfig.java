package kr.hhplus.be.server.support.config;

import kr.hhplus.be.server.application.token.TokenCommandService;
import kr.hhplus.be.server.support.filter.LoggingFilter;
import kr.hhplus.be.server.support.interceptor.TokenValidationInterceptor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TokenCommandService tokenCommandService;

    public WebConfig(TokenCommandService tokenCommandService) {
        this.tokenCommandService = tokenCommandService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TokenValidationInterceptor(tokenCommandService))
                .addPathPatterns("/reservations/**")
                .excludePathPatterns("/token/**");
    }

    @Bean
    public FilterRegistrationBean<LoggingFilter> loggingFilter() {
        FilterRegistrationBean<LoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new LoggingFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1); // 우선순위 설정
        return registration;
    }
}
