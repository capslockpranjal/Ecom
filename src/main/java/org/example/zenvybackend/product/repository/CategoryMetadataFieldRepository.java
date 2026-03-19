package org.example.zenvybackend.product.repository;

import org.example.zenvybackend.product.entity.CategoryMetadataField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryMetadataFieldRepository
        extends JpaRepository<CategoryMetadataField, UUID> {

    // 🔹 Unique check (ignore deleted)
    Optional<CategoryMetadataField> findByNameIgnoreCaseAndIsDeletedFalse(String name);

    // 🔹 Search (ignore deleted)
    Page<CategoryMetadataField> findByNameContainingIgnoreCaseAndIsDeletedFalse(String name, Pageable pageable);

    // 🔹 Get all active
    Page<CategoryMetadataField> findByIsDeletedFalse(Pageable pageable);
}

