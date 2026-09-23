package com.pixevent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${app.cors-origins:}")
    private String corsOrigins;

    @Value("${app.base-url:}")
    private String baseUrl;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        List<String> origins = resolveOrigins();

        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                var mapping = registry.addMapping("/**")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false);

                if (origins.isEmpty()) {
                    //CORS_ORIGINS configurar.
                    mapping.allowedOriginPatterns("*");
                } else {
                    mapping.allowedOrigins(origins.toArray(new String[0]));
                }
            }
        };
    }

    private List<String> resolveOrigins() {
        String raw = (corsOrigins != null && !corsOrigins.isBlank()) ? corsOrigins : baseUrl;
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
