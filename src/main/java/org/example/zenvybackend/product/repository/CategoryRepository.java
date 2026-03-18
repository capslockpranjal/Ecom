package org.example.zenvybackend.product.repository;

import org.example.zenvybackend.product.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findByNameAndParentCategory(String name, Category parent);

    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Category> findByParentCategory(Category parent);

    Page<Category> findByParentCategory(Category parent, Pageable pageable);
}