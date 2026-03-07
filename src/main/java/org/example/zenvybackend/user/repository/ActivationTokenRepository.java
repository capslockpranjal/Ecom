package org.example.zenvybackend.user.repository;

import org.example.zenvybackend.user.entity.User;
import org.example.zenvybackend.user.token.ActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ActivationTokenRepository extends JpaRepository<ActivationToken, UUID> {
    Optional<ActivationToken> findByToken(String token);
    void deleteByUser(User user);
}