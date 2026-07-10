package org.example.zenvybackend.common.cache;

public final class CacheNames {

    public static final String CATEGORIES = "categories";
    public static final String CUSTOMER_CATEGORIES = "customerCategories";
    public static final String CATEGORY_FILTERS = "categoryFilters";
    public static final String CUSTOMER_PRODUCTS = "customerProducts";
    public static final String CUSTOMER_PRODUCT_DETAIL = "customerProductDetail";
    public static final String SIMILAR_PRODUCTS = "similarProducts";

    public static final String[] CATALOG_READ_CACHES = {
            CATEGORIES,
            CUSTOMER_CATEGORIES,
            CATEGORY_FILTERS,
            CUSTOMER_PRODUCTS,
            SIMILAR_PRODUCTS
    };

    public static final String[] PRODUCT_READ_CACHES = {
            CUSTOMER_PRODUCTS,
            CUSTOMER_PRODUCT_DETAIL,
            SIMILAR_PRODUCTS
    };

    private CacheNames() {
    }
}
