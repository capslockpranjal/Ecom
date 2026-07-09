package org.example.zenvybackend.order.repository;

import org.example.zenvybackend.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    @Query("""
        select oi from OrderItem oi
        join fetch oi.sellerOrder so
        join fetch so.order o
        join fetch so.seller
        where oi.id = :orderItemId
        and o.customer.userId = :customerUserId
    """)
    Optional<OrderItem> findByIdAndCustomerUserId(UUID orderItemId, UUID customerUserId);
}
