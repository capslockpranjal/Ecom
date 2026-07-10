package org.example.zenvybackend.security.service;

import java.time.Duration;

public interface TokenBlacklistStore {

    void add(String token, Duration ttl);

    boolean contains(String token);
}
