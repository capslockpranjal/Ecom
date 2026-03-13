package org.example.zenvybackend.common.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.user.repository.TokenRepository;
import org.example.zenvybackend.user.token.TokenType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final TokenRepository tokenRepository;

    @Scheduled(cron = "0 0 * * * ?")
    public void cleanExpiredTokens(){

        tokenRepository.deleteByExpiryDateBeforeAndType(
                LocalDateTime.now(),
                TokenType.BLACKLISTED
        );

    }
}
