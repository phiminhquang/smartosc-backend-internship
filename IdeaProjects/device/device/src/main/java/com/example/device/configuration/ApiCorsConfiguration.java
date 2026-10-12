package com.example.device.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Configuration
public class ApiCorsConfiguration {

    @Bean
    public CorsConfigurationSource apiCorsConfigurationSource(
            @Value("${app.cors.allowed-origins:}") String configuredOrigins
    ) {
        List<String> origins = Arrays.stream(configuredOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
        origins.forEach(ApiCorsConfiguration::validateOrigin);

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Request-ID"));
        configuration.setExposedHeaders(List.of("X-Request-ID", "Retry-After"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        if (!origins.isEmpty()) {
            source.registerCorsConfiguration("/api/**", configuration);
        }
        return source;
    }

    private static void validateOrigin(String origin) {
        URI uri = URI.create(origin);
        boolean https = "https".equals(uri.getScheme());
        boolean loopbackHttp = "http".equals(uri.getScheme())
                && List.of("localhost", "127.0.0.1", "[::1]").contains(uri.getHost());
        if ((!https && !loopbackHttp) || uri.getHost() == null || origin.contains("*")
                || uri.getRawUserInfo() != null || uri.getRawQuery() != null
                || uri.getRawFragment() != null || !"".equals(uri.getRawPath())) {
            throw new IllegalArgumentException("CORS origins must be exact HTTPS origins or HTTP loopback origins");
        }
    }
}
