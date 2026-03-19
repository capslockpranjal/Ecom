package org.example.zenvybackend.product.repository;

import org.example.zenvybackend.category.entity.Category;
import org.example.zenvybackend.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    // 🔹 Check if category has products (used in validation)
    boolean existsByCategoryAndIsDeletedFalse(Category category);

    // 🔹 Get distinct brands (ignore null + deleted)
    @Query("""
        SELECT DISTINCT p.brand
        FROM Product p
        WHERE p.category = :category
        AND p.isDeleted = false
        AND p.brand IS NOT NULL
    """)
    List<String> findDistinctBrandsByCategory(Category category);
}
