package com.badarak.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(security = @SecurityRequirement(name = OpenApiConfig.BEARER_JWT))
@SecurityScheme(
        name = OpenApiConfig.BEARER_JWT,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")

public class OpenApiConfig {
    static final String BEARER_JWT = "bearerAuth";

    @Bean
    public OpenAPI userOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("User API")
                        .description("User REST de l'API User")
                        .version("0.0.1"));
    }
}
