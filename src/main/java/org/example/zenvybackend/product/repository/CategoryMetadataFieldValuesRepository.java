package org.example.zenvybackend.product.repository;

import org.example.zenvybackend.product.entity.Category;
import org.example.zenvybackend.product.entity.CategoryMetadataField;
import org.example.zenvybackend.product.entity.CategoryMetadataFieldValues;
import org.example.zenvybackend.product.entity.CategoryMetadataFieldValuesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryMetadataFieldValuesRepository
        extends JpaRepository<CategoryMetadataFieldValues, CategoryMetadataFieldValuesId> {

    // 🔹 Get metadata for category + field (ignore deleted)
    Optional<CategoryMetadataFieldValues> findByCategoryAndFieldAndIsDeletedFalse(
            Category category,
            CategoryMetadataField field
    );

    // 🔹 Get all metadata of category (ignore deleted)
    List<CategoryMetadataFieldValues> findByCategoryAndIsDeletedFalse(Category category);

    // 🔹 Fetch with field (for response)
    @Query("""
        SELECT v
        FROM CategoryMetadataFieldValues v
        JOIN FETCH v.field
        WHERE v.category = :category
        AND v.isDeleted = false
    """)
    List<CategoryMetadataFieldValues> findByCategoryWithField(Category category);
}