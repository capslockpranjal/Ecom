package org.example.zenvybackend.common.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.user.repository.BlacklistedTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanExpiredTokens(){

        blacklistedTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());

    }
}
