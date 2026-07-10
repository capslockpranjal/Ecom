package org.example.zenvybackend.common.config;

import org.example.zenvybackend.common.cache.CacheNames;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "false")
public class LocalCacheConfig {

    @Bean
    public ConcurrentMapCacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                CacheNames.CATEGORIES,
                CacheNames.CUSTOMER_CATEGORIES,
                CacheNames.CATEGORY_FILTERS,
                CacheNames.CUSTOMER_PRODUCTS,
                CacheNames.CUSTOMER_PRODUCT_DETAIL,
                CacheNames.SIMILAR_PRODUCTS
        );
    }
}
