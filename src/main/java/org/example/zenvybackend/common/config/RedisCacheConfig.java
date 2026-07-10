package org.example.zenvybackend.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.zenvybackend.common.cache.CacheNames;
import org.example.zenvybackend.common.cache.RedisCacheErrorHandler;
import org.example.zenvybackend.common.cache.RedisCacheObjectMapperFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisCacheConfig implements CachingConfigurer {

    @Value("${app.cache.ttl-minutes:10}")
    private long cacheTtlMinutes;

    @Value("${app.cache.key-prefix:v2:}")
    private String cacheKeyPrefix;

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper cacheObjectMapper = RedisCacheObjectMapperFactory.create();

        RedisSerializationContext.SerializationPair<Object> serializer =
                RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(cacheObjectMapper)
                );

        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(serializer)
                .prefixCacheNameWith(cacheKeyPrefix)
                .entryTtl(Duration.ofMinutes(cacheTtlMinutes))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        for (String cacheName : CacheNames.CATALOG_READ_CACHES) {
            cacheConfigurations.put(cacheName, defaults);
        }
        cacheConfigurations.put(CacheNames.CUSTOMER_PRODUCT_DETAIL, defaults);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new RedisCacheErrorHandler();
    }
}
