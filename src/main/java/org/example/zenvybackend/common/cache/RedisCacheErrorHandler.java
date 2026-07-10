package org.example.zenvybackend.common.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

@Slf4j
public class RedisCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.warn(
                "Cache read failed for cache='{}' key='{}'; evicting entry and loading fresh data",
                cache.getName(),
                key,
                exception
        );
        safelyEvict(cache, key);
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.warn("Cache write failed for cache='{}' key='{}'", cache.getName(), key, exception);
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Cache evict failed for cache='{}' key='{}'", cache.getName(), key, exception);
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.warn("Cache clear failed for cache='{}'", cache.getName(), exception);
    }

    private void safelyEvict(Cache cache, Object key) {
        try {
            cache.evict(key);
        } catch (RuntimeException evictException) {
            log.warn(
                    "Failed to evict incompatible cache entry cache='{}' key='{}'",
                    cache.getName(),
                    key,
                    evictException
            );
        }
    }
}
