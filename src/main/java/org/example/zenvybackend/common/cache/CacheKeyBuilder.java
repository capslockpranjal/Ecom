package org.example.zenvybackend.common.cache;

import org.example.zenvybackend.category.dto.request.PageRequestDto;
import org.example.zenvybackend.product.dto.request.CustomerProductFilterDto;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

public final class CacheKeyBuilder {

    private CacheKeyBuilder() {
    }

    public static String categories(UUID categoryId, PageRequestDto dto) {
        return nullSafe(categoryId) + ":" + pageRequest(dto);
    }

    public static String customerCategories(UUID categoryId) {
        return nullSafe(categoryId);
    }

    public static String categoryFilters(UUID categoryId) {
        return categoryId.toString();
    }

    public static String customerProducts(UUID categoryId, PageRequestDto dto, CustomerProductFilterDto filter) {
        return nullSafe(categoryId) + ":" + pageRequest(dto) + ":" + productFilter(filter);
    }

    public static String similarProducts(UUID productId, PageRequestDto dto) {
        return productId + ":" + pageRequest(dto);
    }

    private static String pageRequest(PageRequestDto dto) {
        if (dto == null) {
            return "default";
        }
        return String.join(":",
                String.valueOf(dto.getMax()),
                String.valueOf(dto.getOffset()),
                nullSafe(dto.getSort()),
                nullSafe(dto.getOrder()),
                nullSafe(dto.getQuery())
        );
    }

    private static String productFilter(CustomerProductFilterDto filter) {
        if (filter == null) {
            return "none";
        }

        String metadata = "none";
        if (filter.getMetadata() != null && !filter.getMetadata().isEmpty()) {
            metadata = new TreeMap<>(filter.getMetadata()).entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining(","));
        }

        return String.join(":",
                nullSafe(filter.getBrand()),
                String.valueOf(filter.getMinPrice()),
                String.valueOf(filter.getMaxPrice()),
                metadata
        );
    }

    private static String nullSafe(UUID value) {
        return value == null ? "root" : value.toString();
    }

    private static String nullSafe(String value) {
        return value == null ? "-" : value;
    }
}
