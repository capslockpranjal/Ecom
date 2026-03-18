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

    Optional<CategoryMetadataFieldValues>
    findByFieldAndCategory(CategoryMetadataField field, Category category);


    List<CategoryMetadataFieldValues> findByCategory(Category category);

    Optional<CategoryMetadataFieldValues> findByCategoryAndField(
            Category category,
            CategoryMetadataField field
    );

    @Query("""
SELECT v
FROM CategoryMetadataFieldValues v
JOIN FETCH v.field
WHERE v.category = :category
""")
    List<CategoryMetadataFieldValues> findByCategoryWithField(Category category);
}
