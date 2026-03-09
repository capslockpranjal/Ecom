package org.example.zenvybackend.user.repository;

import org.example.zenvybackend.user.token.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.UUID;

public interface BlacklistedTokenRepository
        extends JpaRepository<BlacklistedToken, UUID> {

    boolean existsByToken(String token);

    void deleteByExpiryDateBefore(LocalDateTime time);

}