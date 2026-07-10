package org.example.zenvybackend.common.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.CATEGORIES, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.CUSTOMER_CATEGORIES, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.CATEGORY_FILTERS, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.CUSTOMER_PRODUCTS, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.SIMILAR_PRODUCTS, allEntries = true)
})
public @interface EvictCatalogReadCaches {
}
