package com.learn.userapi.repository;

import com.learn.userapi.model.RefreshToken;
import com.learn.userapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUser(User user);

    // revoke all tokens for a user — used on password change or "logout all devices"
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user")
    void revokeAllUserTokens(@Param("user") User user);

    // clean up expired tokens — can be called by a scheduled job
    @Modifying
    @Query("""
            DELETE FROM RefreshToken rt
            WHERE rt.expiresAt < CURRENT_TIMESTAMP
               OR rt.revoked = true
            """)
    void deleteExpiredAndRevokedTokens();

    // count active tokens per user — enforce device limit
    @Query("""
            SELECT COUNT(rt) FROM RefreshToken rt
            WHERE rt.user = :user
              AND rt.revoked = false
              AND rt.expiresAt > CURRENT_TIMESTAMP
            """)
    long countActiveTokensByUser(@Param("user") User user);
}