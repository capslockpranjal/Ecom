package org.example.zenvybackend.user.repository;

import org.example.zenvybackend.user.entity.User;
import org.example.zenvybackend.user.token.Token;
import org.example.zenvybackend.user.token.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {

    Optional<Token> findByTokenAndType(String token, TokenType type);

    boolean existsByTokenAndType(String token, TokenType type);

    boolean existsByTokenAndTypeAndExpiryDateAfter(String token, TokenType type, LocalDateTime time);

    void deleteByUserAndType(User user, TokenType type);

    Optional<Token> findByUserAndType(User user, TokenType type);

    void deleteByExpiryDateBeforeAndType(LocalDateTime time, TokenType type);

    void deleteByUserEmailAndType(String email, TokenType type);
}
