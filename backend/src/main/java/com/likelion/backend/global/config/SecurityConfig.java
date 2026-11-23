package com.likelion.backend.global.config;

import com.likelion.backend.domain.auth.jwt.JwtAuthenticationFilter;
import com.likelion.backend.global.security.JwtAccessDeniedHandler;
import com.likelion.backend.global.security.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Value("${cors.allowed-origins}")
    private String corsAllowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

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

                // 기본 로그아웃 방식 비활성화
                .logout(logout -> logout.disable())

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
                                "/auth/login",
                                "/auth/check-username"
                        ).permitAll()

                        // IoT 센서 데이터 수집 (JWT 대신 X-Device-Id 헤더로 서비스에서 인증)
                        .requestMatchers(
                                HttpMethod.POST,
                                "/enclosures/*/sensor/readings"
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

                        // 에러 응답 (처리되지 않은 예외가 403으로 가려지지 않도록)
                        .requestMatchers(
                                "/error"
                        ).permitAll()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                // 인증/인가 실패 시 JSON 응답 (401 / 403)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}