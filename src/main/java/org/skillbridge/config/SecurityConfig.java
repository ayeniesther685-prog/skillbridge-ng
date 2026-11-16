package org.skillbridge.config;

import org.skillbridge.security.JwtAuthenticationFilter;
import org.skillbridge.security.JwtProperties;
import org.skillbridge.security.RestAccessDeniedHandler;
import org.skillbridge.security.RestAuthenticationEntryPoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final RestAuthenticationEntryPoint entryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter,
                          RestAuthenticationEntryPoint entryPoint,
                          RestAccessDeniedHandler accessDeniedHandler) {
        this.jwtFilter = jwtFilter;
        this.entryPoint = entryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                // Phase 3A: email verification endpoints
                                "/api/auth/verify-email",
                                "/api/auth/resend-verification",
                                "/error"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/jobs", "/api/jobs/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/skills", "/api/skills/**").permitAll()

                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/users/{userId:[0-9]+}/profile").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/{userId:[0-9]+}/skills").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/{userId:[0-9]+}/education").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/{userId:[0-9]+}/experience").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/{userId:[0-9]+}/certifications").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/{userId:[0-9]+}/portfolio").permitAll()

                        // Public academy catalog — must come before anyRequest()
                        .requestMatchers(
                                new AntPathRequestMatcher("/api/academy/programs", "GET"),
                                new AntPathRequestMatcher("/api/academy/departments", "GET"),
                                new AntPathRequestMatcher("/api/academy/courses", "GET"),
                                new AntPathRequestMatcher("/api/academy/sessions", "GET"),
                                new AntPathRequestMatcher("/api/academy/sessions/*/semesters", "GET"),
                                new AntPathRequestMatcher("/api/academy/courses/*/lessons", "GET"),
                                new AntPathRequestMatcher("/api/academy/courses/*/lessons/*", "GET"),
                                new AntPathRequestMatcher("/api/academy/courses/*/assignments", "GET"),
                                new AntPathRequestMatcher("/api/academy/assignments/*", "GET"),
                                new AntPathRequestMatcher("/api/certificates/verify/*", "GET"),
                                new AntPathRequestMatcher("/api/challenges", "GET"),
                                new AntPathRequestMatcher("/api/challenges/*", "GET"),
                                new AntPathRequestMatcher("/api/providers/*/verification-status", "GET")
                        ).permitAll()

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/me/**").authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(
                java.util.Arrays.stream(System.getenv().getOrDefault("CORS_ALLOWED_ORIGINS", "http://localhost:5173,http://localhost:3000").split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new)));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
