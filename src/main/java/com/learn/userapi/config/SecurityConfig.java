package com.learn.userapi.config;

import com.learn.userapi.security.JwtAccessDeniedHandler;
import com.learn.userapi.security.JwtAuthEntryPoint;
import com.learn.userapi.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity           // enables @PreAuthorize on controller methods
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final JwtAuthEntryPoint authEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          JwtAuthEntryPoint authEntryPoint,
                          JwtAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authEntryPoint = authEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // disable CSRF — not needed for stateless JWT APIs
                // CSRF protects cookie-based sessions; we use tokens
                .csrf(AbstractHttpConfigurer::disable)

                // stateless — no HttpSession created or used
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // custom 401 and 403 handlers — return ApiResponse JSON
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))

                // route-level authorization rules
                .authorizeHttpRequests(auth -> auth

                        // Public routes
                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/api/users/me").hasAnyRole("USER", "ADMIN")

                        // ADMIN only
                        .requestMatchers(HttpMethod.GET,
                                "/api/users",
                                "/api/users/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/users/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,
                                "/api/products").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/products/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/products/{id}").hasRole("ADMIN")
                        .requestMatchers("/api/analytics/**").hasRole("ADMIN")

                        // USER + ADMIN
                        .requestMatchers(HttpMethod.GET,
                                "/api/products",
                                "/api/products/{id}").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(
                                "/api/users/{userId}/orders/**").hasAnyRole("USER", "ADMIN")


                        // Any authenticated user
                        .anyRequest().authenticated()
                )

                // insert JWT filter BEFORE Spring's UsernamePasswordAuthenticationFilter
                // this ensures our filter runs first and populates the SecurityContext
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}