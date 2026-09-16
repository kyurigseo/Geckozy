package com.likelion.backend.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${cors.allowed-origins}")
    private String corsAllowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                Arrays.asList(corsAllowedOrigins.split(","))
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PATCH",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                // CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                // CORS 설정
                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource())
                )

                // 기본 로그인 방식 비활성화
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())

                // 세션 사용하지 않음
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // URL별 접근 권한
                .authorizeHttpRequests(auth -> auth

                        // CORS Preflight 요청 허용
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // 회원가입 / 로그인은 인증 없이 접근 가능
                        .requestMatchers(
                                "/auth/signup",
                                "/auth/login"
                        ).permitAll()

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Health Check
                        .requestMatchers(
                                "/health"
                        ).permitAll()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}