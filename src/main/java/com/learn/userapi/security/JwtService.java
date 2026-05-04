package com.learn.userapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;



import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        // decode the hex secret and build the HMAC-SHA256 signing key
        // Keys.hmacShaKeyFor requires at least 256 bits — our 64-char hex = 256 bits
        this.signingKey = Keys.hmacShaKeyFor(
                hexStringToByteArray(jwtProperties.getSecret())
        );
    }

    // Access Token Generation

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        // embed the role directly in the token — no DB lookup needed on validation
        extraClaims.put("role", userDetails.getAuthorities()
                .iterator().next().getAuthority());

        return buildToken(extraClaims, userDetails,
                jwtProperties.getAccessTokenExpiration());
    }

    private String buildToken(Map<String, Object> extraClaims,
                              UserDetails userDetails,
                              long expiration) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .claims(extraClaims)                        // custom claims (role)
                .subject(userDetails.getUsername())         // sub = email
                .issuedAt(new Date(now))                    // iat
                .expiration(new Date(now + expiration))     // exp
                .signWith(signingKey)                       // sign with HMAC-SHA256
                .compact();                                 // build the token string
    }

    // Refresh Token Generation

    // Refresh token is NOT a JWT — it's an opaque random string
    // stored in the DB. This way we can revoke it at any time.
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public LocalDateTime getRefreshTokenExpiry() {
        return LocalDateTime.now().plusSeconds(
                jwtProperties.getRefreshTokenExpiration() / 1000
        );
    }

    // Token Validation

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            String email = extractEmail(token);
            return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.warn("JWT signature invalid: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("JWT malformed: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("JWT unsupported: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    // Claims Extraction

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // generic claim extractor — takes a function that maps Claims → T
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        // parse and verify the token — throws if invalid/expired/tampered
        return Jwts.parser()
                .verifyWith(signingKey)     // use our secret key to verify signature
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Utility

    // converts hex string secret to byte array for key construction
    private byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
    
}