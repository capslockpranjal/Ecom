package org.example.zenvybackend.user.repository;

import org.example.zenvybackend.user.entity.User;
import org.example.zenvybackend.user.token.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);

    void deleteByUser(User user);

    void deleteByExpiryDateBefore(LocalDateTime time);
}