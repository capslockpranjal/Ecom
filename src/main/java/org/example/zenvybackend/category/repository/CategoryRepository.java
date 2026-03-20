package org.example.zenvybackend.category.repository;

import org.example.zenvybackend.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    //  Unique check (ignore deleted)
    Optional<Category> findByNameIgnoreCaseAndParentCategoryAndIsDeletedFalse(
            String name,
            Category parent
    );

    //  Search (ignore deleted)
    Page<Category> findByNameContainingIgnoreCaseAndIsDeletedFalse(String name, Pageable pageable);

    //  Get children (ignore deleted)
    List<Category> findByParentCategoryAndIsDeletedFalse(Category parent);

    Page<Category> findByParentCategoryAndIsDeletedFalse(Category parent, Pageable pageable);

    //  Root categories
    List<Category> findByParentCategoryIsNullAndIsDeletedFalse();

    //  Get all active categories
    Page<Category> findByIsDeletedFalse(Pageable pageable);

    boolean existsByParentCategoryAndIsDeletedFalse(Category parent);
}
