package org.example.zenvybackend.cart.repository;

import org.example.zenvybackend.cart.entity.Cart;
import org.example.zenvybackend.user.entity.Customer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    @EntityGraph(attributePaths = {"items", "items.productVariation", "items.productVariation.product"})
    Optional<Cart> findByCustomerUserId(UUID customerUserId);

    Optional<Cart> findByCustomer(Customer customer);
}
