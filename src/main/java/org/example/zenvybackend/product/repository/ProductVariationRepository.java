package org.example.zenvybackend.product.repository;

import org.example.zenvybackend.product.entity.ProductVariation;
import org.example.zenvybackend.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ProductVariationRepository extends JpaRepository<ProductVariation, UUID> {

    // 🔹 Minimum price for category
    @Query("""
        SELECT MIN(pv.price)
        FROM ProductVariation pv
        WHERE pv.product.category = :category
        AND pv.isDeleted = false
        AND pv.isActive = true
        AND pv.price IS NOT NULL
    """)
    Double findMinPriceByCategory(Category category);

    // 🔹 Maximum price for category
    @Query("""
        SELECT MAX(pv.price)
        FROM ProductVariation pv
        WHERE pv.product.category = :category
        AND pv.isDeleted = false
        AND pv.isActive = true
        AND pv.price IS NOT NULL
    """)
    Double findMaxPriceByCategory(Category category);
}
