package com.learn.userapi.service;

import com.learn.userapi.dto.request.LoginRequest;
import com.learn.userapi.dto.request.RefreshRequest;
import com.learn.userapi.dto.request.RegisterRequest;
import com.learn.userapi.dto.response.AuthResponse;
import com.learn.userapi.dto.response.UserResponse;
import com.learn.userapi.exception.DuplicateResourceException;
import com.learn.userapi.exception.InvalidTokenException;
import com.learn.userapi.model.RefreshToken;
import com.learn.userapi.model.Role;
import com.learn.userapi.model.User;
import com.learn.userapi.repository.RefreshTokenRepository;
import com.learn.userapi.repository.UserRepository;
import com.learn.userapi.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.learn.userapi.security.JwtProperties;

@Service
@Transactional
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final JwtProperties jwtProperties;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.jwtProperties = jwtProperties;
    }

    // Registration

    public AuthResponse register(RegisterRequest request, String deviceInfo) {
        log.debug("Registration attempt for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email already registered: " + request.getEmail());
        }

        // create and persist the user
        User user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.USER                       // all registrations get USER role
        );
        userRepository.save(user);
        log.info("New user registered with id: {}", user.getId());

        // issue tokens
        return issueTokens(user, deviceInfo);
    }

    // Login

    public AuthResponse login(LoginRequest request, String deviceInfo) {
        log.debug("Login attempt for email: {}", request.getEmail());

        // delegate credential verification to Spring Security
        // throws AuthenticationException if credentials are wrong
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // authentication succeeded — principal is our User entity
        User user = (User) authentication.getPrincipal();

        // revoke all existing refresh tokens for this user on login
        // prevents unlimited token accumulation across devices
        // comment this out if you want true multi-device support
        refreshTokenRepository.revokeAllUserTokens(user);

        log.info("User logged in: id={}", user.getId());
        return issueTokens(user, deviceInfo);
    }

    // Token Refresh

    public AuthResponse refresh(RefreshRequest request) {
        log.debug("Token refresh requested");

        RefreshToken storedToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException(
                        "Refresh token not found"));

        if (!storedToken.isValid()) {
            // token is revoked or expired — could be a reuse attack
            // revoke ALL tokens for this user as a precaution
            refreshTokenRepository.revokeAllUserTokens(storedToken.getUser());
            log.warn("Invalid refresh token used for user id: {}. " +
                    "All tokens revoked.", storedToken.getUser().getId());
            throw new InvalidTokenException("Refresh token is invalid or expired");
        }

        User user = storedToken.getUser();

        // token rotation — revoke old token, issue new one
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        // generate new access token only (no user info needed in refresh response)
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshTokenValue = jwtService.generateRefreshToken();

        RefreshToken newRefreshToken = new RefreshToken(
                newRefreshTokenValue,
                user,
                jwtService.getRefreshTokenExpiry(),
                storedToken.getDeviceInfo()     // keep same device info
        );
        refreshTokenRepository.save(newRefreshToken);

        log.info("Tokens rotated for user id: {}", user.getId());

        long expiresIn = jwtProperties.getAccessTokenExpiration() / 1000;
        return AuthResponse.ofTokensOnly(newAccessToken, newRefreshTokenValue, expiresIn);
    }

    // Logout

    public void logout(RefreshRequest request) {
        log.debug("Logout requested");

        refreshTokenRepository.findByToken(request.getRefreshToken())
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                    log.info("Refresh token revoked for user id: {}",
                            token.getUser().getId());
                });

        // if token not found — treat as already logged out (idempotent)
    }

    // Logout all devices

    public void logoutAllDevices(User currentUser) {
        refreshTokenRepository.revokeAllUserTokens(currentUser);
        log.info("All refresh tokens revoked for user id: {}", currentUser.getId());
    }

    // Private helpers

    private AuthResponse issueTokens(User user, String deviceInfo) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = jwtService.generateRefreshToken();

        RefreshToken refreshToken = new RefreshToken(
                refreshTokenValue,
                user,
                jwtService.getRefreshTokenExpiry(),
                deviceInfo
        );
        refreshTokenRepository.save(refreshToken);

        long expiresIn = jwtProperties.getAccessTokenExpiration() / 1000;
        return AuthResponse.of(accessToken, refreshTokenValue,
                expiresIn, UserResponse.fromUser(user));
    }
}