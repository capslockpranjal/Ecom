package org.example.zenvybackend.product.repository;

import org.example.zenvybackend.category.entity.Category;
import org.example.zenvybackend.product.entity.Product;
import org.example.zenvybackend.user.entity.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>, ProductRepositoryCustom {

    // 🔹 Check if category has products (used in validation)
    boolean existsByCategoryAndIsDeletedFalse(Category category);

    // 🔹 Get distinct brands (ignore null + deleted)
    @Query("""
        SELECT DISTINCT p.brand
        FROM Product p
        WHERE p.category = :category
        AND p.isDeleted = false
        AND p.isActive = true
        AND p.brand IS NOT NULL
    """)
    List<String> findDistinctBrandsByCategory(Category category);

    @Query("""
        select
            case when count(p) > 0 then true else false end
        from Product p
        where lower(trim(p.name)) = lower(trim(:name))
          and lower(trim(p.brand)) = lower(trim(:brand))
          and p.category = :category
          and p.seller = :seller
          and p.isDeleted = false
    """)
    boolean existsActiveDuplicate(
            @Param("name") String name,
            @Param("brand") String brand,
            @Param("category") Category category,
            @Param("seller") Seller seller
    );

    Page<Product> findBySellerAndIsDeletedFalse(
            Seller seller, Pageable pageable
    );

    Page<Product> findBySellerAndNameContainingIgnoreCaseAndIsDeletedFalse(
            Seller seller,
            String name,
            Pageable pageable
    );

    @Query("""
SELECT DISTINCT p.brand
FROM Product p
WHERE p.category.id = :categoryId
AND p.isDeleted = false
AND p.isActive = true
""")
    List<String> findDistinctBrands(UUID categoryId);

    @Query(
            value = """
                select distinct p
                from Product p
                join ProductVariation pv on pv.product = p
                where p.category in :categories
                  and p.isDeleted = false
                  and p.isActive = true
                  and pv.isDeleted = false
                  and pv.isActive = true
            """,
            countQuery = """
                select count(distinct p.id)
                from Product p
                join ProductVariation pv on pv.product = p
                where p.category in :categories
                  and p.isDeleted = false
                  and p.isActive = true
                  and pv.isDeleted = false
                  and pv.isActive = true
            """
    )
    Page<Product> findActiveCustomerVisibleProductsByCategories(
            @Param("categories") List<Category> categories,
            Pageable pageable
    );

    @Query("""
        select p
        from Product p
        where p.isDeleted = false
          and p.isActive = true
          and (:seller is null or p.seller = :seller)
          and (:category is null or p.category = :category)
    """)
    Page<Product> findAdminVisibleProducts(
            @Param("seller") Seller seller,
            @Param("category") Category category,
            Pageable pageable
    );

    java.util.Optional<Product> findByIdAndIsDeletedFalse(UUID id);

    java.util.Optional<Product> findByIdAndIsDeletedFalseAndIsActiveTrue(UUID id);

    @Query(
            value = """
                select distinct p
                from Product p
                join ProductVariation pv on pv.product = p
                where p.category = :category
                  and p.id <> :productId
                  and p.isDeleted = false
                  and p.isActive = true
                  and pv.isDeleted = false
                  and pv.isActive = true
            """,
            countQuery = """
                select count(distinct p.id)
                from Product p
                join ProductVariation pv on pv.product = p
                where p.category = :category
                  and p.id <> :productId
                  and p.isDeleted = false
                  and p.isActive = true
                  and pv.isDeleted = false
                  and pv.isActive = true
            """
    )
    Page<Product> findSimilarActiveCustomerVisibleProducts(
            @Param("category") Category category,
            @Param("productId") UUID productId,
            Pageable pageable
    );

}
