package org.example.zenvybackend.order.repository;

import org.example.zenvybackend.order.entity.SellerOrder;
import org.example.zenvybackend.user.entity.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SellerOrderRepository extends JpaRepository<SellerOrder, UUID> {

    @EntityGraph(attributePaths = {"order", "order.customer", "order.customer.user", "items", "seller", "seller.user"})
    Page<SellerOrder> findBySellerOrderByCreatedAtDesc(Seller seller, Pageable pageable);

    @EntityGraph(attributePaths = {"order", "order.customer", "order.customer.user", "items", "seller", "seller.user"})
    Optional<SellerOrder> findByIdAndSellerUserId(UUID id, UUID sellerUserId);
}
