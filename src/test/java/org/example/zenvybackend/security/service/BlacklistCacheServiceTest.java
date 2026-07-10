package org.example.zenvybackend.security.service;

import org.example.zenvybackend.security.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlacklistCacheServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    private BlacklistCacheService blacklistCacheService;

    @BeforeEach
    void setUp() {
        blacklistCacheService = new BlacklistCacheService(new InMemoryTokenBlacklistStore(), jwtUtil);
    }

    @Test
    void blacklistToken_marksTokenAsBlacklistedUntilExpiry() {
        String token = "access-token";
        when(jwtUtil.extractExpiration(token))
                .thenReturn(new Date(System.currentTimeMillis() + 60_000));

        assertFalse(blacklistCacheService.isBlacklisted(token));
        blacklistCacheService.blacklistToken(token);
        assertTrue(blacklistCacheService.isBlacklisted(token));
    }

    @Test
    void blacklistToken_skipsExpiredTokens() {
        String token = "expired-token";
        when(jwtUtil.extractExpiration(token)).thenReturn(new Date(System.currentTimeMillis() - 1_000));

        blacklistCacheService.blacklistToken(token);

        assertFalse(blacklistCacheService.isBlacklisted(token));
    }
}
