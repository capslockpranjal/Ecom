package org.example.zenvybackend.cart.repository;

import org.example.zenvybackend.cart.entity.Cart;
import org.example.zenvybackend.cart.entity.CartItem;
import org.example.zenvybackend.product.entity.ProductVariation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByIdAndCartAndIsDeletedFalse(UUID id, Cart cart);

    Optional<CartItem> findByCartAndProductVariationAndIsDeletedFalse(Cart cart, ProductVariation variation);
}
