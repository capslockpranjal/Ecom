package org.example.zenvybackend.order.repository;

import org.example.zenvybackend.order.entity.Order;
import org.example.zenvybackend.user.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByCustomerOrderByCreatedAtDesc(Customer customer, Pageable pageable);

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.id = :id AND o.customer.userId = :customerUserId")
    Optional<Order> findByIdAndCustomerUserId(
            @Param("id") UUID id,
            @Param("customerUserId") UUID customerUserId
    );

    @Query("""
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.sellerOrders so
            LEFT JOIN FETCH so.seller
            WHERE o.id = :id
            """)
    Optional<Order> findByIdWithSellerOrders(@Param("id") UUID id);

    @Query("""
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.sellerOrders so
            LEFT JOIN FETCH so.seller
            WHERE o.id = :id AND o.customer.userId = :customerUserId
            """)
    Optional<Order> findByIdWithSellerOrdersForCustomer(
            @Param("id") UUID id,
            @Param("customerUserId") UUID customerUserId
    );

    @Query("""
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.sellerOrders so
            LEFT JOIN FETCH so.seller
            WHERE o.id IN :ids
            """)
    List<Order> findByIdsWithSellerOrders(@Param("ids") Collection<UUID> ids);
}
