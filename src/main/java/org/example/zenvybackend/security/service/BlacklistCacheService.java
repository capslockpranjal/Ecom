package org.example.zenvybackend.security.service;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.security.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class BlacklistCacheService {

    private final TokenBlacklistStore blacklistStore;
    private final JwtUtil jwtUtil;

    public void blacklistToken(String token) {
        blacklistStore.add(token, remainingTtl(token));
    }

    public boolean isBlacklisted(String token) {
        return blacklistStore.contains(token);
    }

    private Duration remainingTtl(String token) {
        Date expiry = jwtUtil.extractExpiration(token);
        long millis = expiry.getTime() - System.currentTimeMillis();
        return millis > 0 ? Duration.ofMillis(millis) : Duration.ZERO;
    }
}
