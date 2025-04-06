package kr.hhplus.be.server.config.jpa;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("콘서트 예약 시스템 API")
                        .description("설계 기반 Swagger 문서")
                        .version("v1.0.0"));
    }
}
