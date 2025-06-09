package kr.hhplus.be.server.support.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<TokenValidationFilter> tokenValidationFilter() {
        FilterRegistrationBean<TokenValidationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TokenValidationFilter());
        registrationBean.addUrlPatterns("/token/*"); // 필터를 적용할 URI
        registrationBean.setOrder(1); // 필터 우선순위
        return registrationBean;
    }
}