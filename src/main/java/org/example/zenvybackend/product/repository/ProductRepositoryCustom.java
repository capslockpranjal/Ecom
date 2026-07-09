package org.example.zenvybackend.product.repository;

import org.example.zenvybackend.category.entity.Category;
import org.example.zenvybackend.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ProductRepositoryCustom {

    Page<Product> findFilteredCustomerVisibleProducts(
            List<Category> categories,
            String brand,
            Double minPrice,
            Double maxPrice,
            Map<String, String> metadataFilters,
            Pageable pageable
    );
}
