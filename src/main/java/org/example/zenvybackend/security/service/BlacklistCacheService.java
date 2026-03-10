package org.example.zenvybackend.security.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BlacklistCacheService {

    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    public void blacklistToken(String token){
        blacklist.add(token);
    }

    public boolean isBlacklisted(String token){
        return blacklist.contains(token);
    }

}
