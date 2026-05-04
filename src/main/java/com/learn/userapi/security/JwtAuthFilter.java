package com.learn.userapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTH_HEADER = "Authorization";
    private final CookieService cookieService;

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService,
                         UserDetailsService userDetailsService,
                         CookieService cookieService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.cookieService = cookieService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // step 1 — extract token from Authorization header
        String token = extractToken(request);

        if (token == null) {
            // no token present — let the request through
            // AuthorizationFilter will block it if the route is protected
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // step 2 — extract email from token claims
            String email = jwtService.extractEmail(token);

            // step 3 — only process if email found and not already authenticated
            if (StringUtils.hasText(email) &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                // step 4 — load full UserDetails from database
                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(email);

                // step 5 — validate token against the loaded UserDetails
                if (jwtService.validateToken(token, userDetails)) {

                    // step 6 — build authenticated token
                    // null credentials — we don't store or pass the password here
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // attach request details (IP, session) to authentication
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request));

                    // step 7 — store in SecurityContext
                    // from this point, the user is "authenticated" for this request
                    SecurityContextHolder.getContext()
                            .setAuthentication(authToken);

                    log.debug("Authenticated user: {} for URI: {}",
                            email,
                            isBearerToken(request) ? "Bearer" : "Cookie",
                            request.getRequestURI());
                }
            }
        } catch (Exception e) {
            // token parsing failed — log and continue without authentication
            // the AuthorizationFilter will reject protected routes
            log.warn("JWT processing failed for URI {}: {}",
                    request.getRequestURI(), e.getMessage());
        }

        // always continue the filter chain
        // AuthorizationFilter downstream handles route protection
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        // 1. try Authorization header first (Bearer token)
        String bearerToken = extractBearerToken(request);
        if (bearerToken != null) return bearerToken;

        // 2. fall back to cookie
        return cookieService.extractAccessToken(request).orElse(null);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private boolean isBearerToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        return StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX);
    }
}