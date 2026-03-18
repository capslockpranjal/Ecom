package org.example.zenvybackend.product.repository;

import org.example.zenvybackend.product.entity.CategoryMetadataField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryMetadataFieldRepository
        extends JpaRepository<CategoryMetadataField, UUID> {

    Optional<CategoryMetadataField> findByNameIgnoreCase(String name);

    Page<CategoryMetadataField> findByNameContainingIgnoreCase(String name, Pageable pageable);

}


