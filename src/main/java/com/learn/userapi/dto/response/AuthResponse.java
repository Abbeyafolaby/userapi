package com.learn.userapi.dto.response;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn;         // access token lifetime in seconds
    private UserResponse user;

    private AuthResponse(String accessToken, String refreshToken,
                         long expiresIn, UserResponse user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public static AuthResponse of(String accessToken, String refreshToken,
                                  long expiresIn, UserResponse user) {
        return new AuthResponse(accessToken, refreshToken, expiresIn, user);
    }

    // for refresh — no user object needed
    public static AuthResponse ofTokensOnly(String accessToken,
                                            String refreshToken,
                                            long expiresIn) {
        return new AuthResponse(accessToken, refreshToken, expiresIn, null);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public UserResponse getUser() {
        return user;
    }
}