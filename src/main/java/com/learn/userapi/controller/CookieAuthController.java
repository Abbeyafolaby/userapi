package com.learn.userapi.controller;

import com.learn.userapi.dto.request.LoginRequest;
import com.learn.userapi.dto.request.RefreshRequest;
import com.learn.userapi.dto.request.RegisterRequest;
import com.learn.userapi.dto.response.ApiResponse;
import com.learn.userapi.dto.response.AuthResponse;
import com.learn.userapi.dto.response.UserResponse;
import com.learn.userapi.model.User;
import com.learn.userapi.security.CookieService;
import com.learn.userapi.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/cookie")
public class CookieAuthController {

    private final AuthService authService;
    private final CookieService cookieService;

    public CookieAuthController(AuthService authService,
                                CookieService cookieService) {
        this.authService = authService;
        this.cookieService = cookieService;
    }

    // Register — tokens set as cookies, NOT in body
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String deviceInfo = httpRequest.getHeader("User-Agent");
        AuthResponse authResponse = authService.register(request, deviceInfo);

        // set tokens as HttpOnly cookies
        cookieService.setTokenCookies(httpResponse, authResponse);

        // return only the user — no tokens in body (cookies carry them)
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful",
                        authResponse.getUser()));
    }

    // Login — tokens set as cookies
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String deviceInfo = httpRequest.getHeader("User-Agent");
        AuthResponse authResponse = authService.login(request, deviceInfo);

        cookieService.setTokenCookies(httpResponse, authResponse);

        return ResponseEntity.ok(ApiResponse.success("Login successful",
                authResponse.getUser()));
    }

    // Refresh — reads refresh token FROM cookie, sets new cookies
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        // extract refresh token from cookie — client sends nothing in body
        String refreshTokenValue = cookieService
                .extractRefreshToken(httpRequest)
                .orElseThrow(() -> new com.learn.userapi.exception
                        .InvalidTokenException("Refresh token cookie not found"));

        // reuse AuthService — wrap cookie value in the request DTO
        RefreshRequest refreshRequest = new RefreshRequest();
        refreshRequest.setRefreshToken(refreshTokenValue);

        AuthResponse authResponse = authService.refresh(refreshRequest);

        // set new cookies with rotated tokens
        cookieService.setTokenCookies(httpResponse, authResponse);

        // no body needed — cookies updated
        return ResponseEntity.ok(ApiResponse.success(
                "Token refreshed successfully", null));
    }

    // Logout — clears cookies server-side
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse,
            @AuthenticationPrincipal User currentUser) {

        // revoke refresh token in DB
        cookieService.extractRefreshToken(httpRequest)
                .ifPresent(token -> {
                    RefreshRequest req = new RefreshRequest();
                    req.setRefreshToken(token);
                    authService.logout(req);
                });

        // clear cookies — browser deletes them on receiving maxAge=0
        cookieService.clearTokenCookies(httpResponse);

        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }
}