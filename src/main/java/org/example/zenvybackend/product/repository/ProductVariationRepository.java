package org.example.zenvybackend.product.repository;

import org.example.zenvybackend.product.entity.Product;
import org.example.zenvybackend.product.entity.ProductVariation;
import org.example.zenvybackend.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductVariationRepository extends JpaRepository<ProductVariation, UUID> {

    // 🔹 Minimum price for category
    @Query("""
        SELECT MIN(pv.price)
        FROM ProductVariation pv
        WHERE pv.product.category = :category
        AND pv.product.isDeleted = false
        AND pv.product.isActive = true
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
        AND pv.product.isDeleted = false
        AND pv.product.isActive = true
        AND pv.isDeleted = false
        AND pv.isActive = true
        AND pv.price IS NOT NULL
    """)
    Double findMaxPriceByCategory(Category category);


    @Query("""
    SELECT MIN(pv.price) FROM ProductVariation pv
    WHERE pv.product.category.id = :categoryId
    AND pv.product.isDeleted = false
    AND pv.product.isActive = true
    AND pv.isDeleted = false
    AND pv.isActive = true
    AND pv.price IS NOT NULL
""")
    Double findMinPrice(UUID categoryId);

    @Query("""
    SELECT MAX(pv.price) FROM ProductVariation pv
    WHERE pv.product.category.id = :categoryId
    AND pv.product.isDeleted = false
    AND pv.product.isActive = true
    AND pv.isDeleted = false
    AND pv.isActive = true
    AND pv.price IS NOT NULL
""")
    Double findMaxPrice(UUID categoryId);

    @Query("""
        SELECT DISTINCT p.brand FROM ProductVariation pv
        JOIN pv.product p
        WHERE p.category.id = :categoryId
        AND p.isDeleted = false
        AND p.isActive = true
        AND pv.isDeleted = false
        AND pv.isActive = true
    """)
    List<String> findDistinctBrands(UUID categoryId);

    @Query("""
SELECT pv.metadata
FROM ProductVariation pv
WHERE pv.product.category.id = :categoryId
AND pv.product.isDeleted = false
AND pv.product.isActive = true
AND pv.isDeleted = false
AND pv.isActive = true
""")
    List<String> findMetadataByCategory(UUID categoryId);

    boolean existsByProductAndMetadataAndIsDeletedFalse(Product product, String metadata);

    boolean existsByProductAndMetadataAndIsDeletedFalseAndIdNot(Product product, String metadata, UUID id);

    List<ProductVariation> findByProductAndIsDeletedFalseAndIsActiveTrue(Product product);

    List<ProductVariation> findByProductAndIsDeletedFalse(Product product);

    List<ProductVariation> findByProductAndProduct_IsDeletedFalseAndIsActiveTrue(Product product);

    Page<ProductVariation> findByProductAndIsDeletedFalseAndIsActiveTrue(
            Product product,
            Pageable pageable
    );

    @Query("""
        select pv
        from ProductVariation pv
        where pv.product = :product
          and pv.isDeleted = false
          and pv.isActive = true
          and lower(pv.metadata) like lower(concat('%', :query, '%'))
    """)
    Page<ProductVariation> searchByProductAndQuery(
            Product product,
            String query,
            Pageable pageable
    );

    Optional<ProductVariation> findByIdAndIsDeletedFalse(UUID id);

    Optional<ProductVariation> findByIdAndIsDeletedFalseAndIsActiveTrue(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select pv from ProductVariation pv where pv.id = :id and pv.isDeleted = false")
    Optional<ProductVariation> findByIdForUpdate(UUID id);
}
