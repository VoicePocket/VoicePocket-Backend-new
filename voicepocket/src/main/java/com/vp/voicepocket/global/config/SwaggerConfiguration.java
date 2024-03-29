package com.vp.voicepocket.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI demoOpenAPI() {

        var info = new Info().title("Voice Pocket project API")
            .description("Voice Pocket application").version("v0.0.1");

        var jwtScheme = "jwtAuth";
        var securityRequirement = new SecurityRequirement().addList(jwtScheme);
        var components = new Components()
            .addSecuritySchemes(jwtScheme, new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .in(SecurityScheme.In.HEADER)
                .scheme("Bearer")
                .bearerFormat("JWT"));

        return new OpenAPI()
            .components(new Components())
            .info(info)
            .addSecurityItem(securityRequirement)
            .components(components);
    }
}
