package org.example.zenvybackend.product.repository;

import org.example.zenvybackend.product.entity.Category;
import org.example.zenvybackend.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsByCategoryAndIsDeletedFalse(Category category);

}
