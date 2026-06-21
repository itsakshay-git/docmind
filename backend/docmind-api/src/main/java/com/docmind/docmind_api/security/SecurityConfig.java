package com.docmind.docmind_api.security;

import com.docmind.docmind_api.security.jwt.JwtAuthenticationFilter;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Instant;
import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final List<String> allowedOrigins;

    public SecurityConfig(
            JwtAuthenticationFilter jwtFilter,
            @Value("${docmind.cors.allowed-origins}") List<String> allowedOrigins
    ) {

        this.jwtFilter =
                jwtFilter;
        this.allowedOrigins =
                allowedOrigins;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .cors(cors -> {
                })
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter()
                                    .write(authErrorJson(
                                            401,
                                            "Unauthorized",
                                            "Your session expired. Please sign in again.",
                                            request.getRequestURI()
                                    ));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter()
                                    .write(authErrorJson(
                                            403,
                                            "Forbidden",
                                            "You do not have permission to access this resource.",
                                            request.getRequestURI()
                                    ));
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        )
                        .permitAll()
                        .requestMatchers(
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        )
                        .permitAll()
                        .requestMatchers(EndpointRequest.to("health"))
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                );

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(allowedOrigins);

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setAllowCredentials(
                true
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    private String authErrorJson(
            int status,
            String error,
            String message,
            String path
    ) {

        return """
                {"timestamp":"%s","status":%d,"error":"%s","message":"%s","path":"%s"}
                """.formatted(
                Instant.now(),
                status,
                error,
                message,
                path
        ).trim();
    }
}