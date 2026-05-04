package com.learn.userapi.security;

import com.learn.userapi.dto.response.AuthResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
public class CookieService {

    private final CookieProperties cookieProperties;
    private final JwtProperties jwtProperties;

    public CookieService(CookieProperties cookieProperties,
                         JwtProperties jwtProperties) {
        this.cookieProperties = cookieProperties;
        this.jwtProperties = jwtProperties;
    }

    // ── Set tokens as HTTP-only cookies ──────────────────────────────────

    public void setTokenCookies(HttpServletResponse response,
                                AuthResponse authResponse) {
        addCookie(response,
                cookieProperties.getAccessTokenName(),
                authResponse.getAccessToken(),
                (int) (jwtProperties.getAccessTokenExpiration() / 1000));

        addCookie(response,
                cookieProperties.getRefreshTokenName(),
                authResponse.getRefreshToken(),
                (int) (jwtProperties.getRefreshTokenExpiration() / 1000));
    }

    // ── Clear cookies on logout ───────────────────────────────────────────

    public void clearTokenCookies(HttpServletResponse response) {
        clearCookie(response, cookieProperties.getAccessTokenName());
        clearCookie(response, cookieProperties.getRefreshTokenName());
    }

    // ── Read token from incoming request cookies ──────────────────────────

    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return extractCookieValue(request, cookieProperties.getAccessTokenName());
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return extractCookieValue(request, cookieProperties.getRefreshTokenName());
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private void addCookie(HttpServletResponse response,
                           String name, String value, int maxAgeSeconds) {
        // ResponseCookie gives us full control over all cookie attributes
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)                              // JS cannot read this
                .secure(cookieProperties.isSecure())        // HTTPS only in prod
                .sameSite(cookieProperties.getSameSite())   // CSRF protection
                .domain(cookieProperties.getDomain())
                .path("/")                                   // available to all paths
                .maxAge(maxAgeSeconds)
                .build();

        // addHeader (not setHeader) — allows multiple Set-Cookie headers
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void clearCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite())
                .domain(cookieProperties.getDomain())
                .path("/")
                .maxAge(0)          // maxAge=0 tells browser to delete the cookie
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    private Optional<String> extractCookieValue(HttpServletRequest request,
                                                String cookieName) {
        if (request.getCookies() == null) return Optional.empty();

        return Arrays.stream(request.getCookies())
                .filter(c -> cookieName.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}