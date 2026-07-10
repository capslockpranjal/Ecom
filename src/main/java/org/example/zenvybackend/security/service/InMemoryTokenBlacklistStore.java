package org.example.zenvybackend.security.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "false")
public class InMemoryTokenBlacklistStore implements TokenBlacklistStore {

    private final Map<String, Instant> blacklist = new ConcurrentHashMap<>();

    @Override
    public void add(String token, Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            return;
        }
        blacklist.put(token, Instant.now().plus(ttl));
    }

    @Override
    public boolean contains(String token) {
        Instant expiresAt = blacklist.get(token);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt.isBefore(Instant.now())) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }
}
