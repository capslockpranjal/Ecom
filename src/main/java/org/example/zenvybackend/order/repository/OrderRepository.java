package org.example.zenvybackend.order.repository;

import org.example.zenvybackend.order.entity.Order;
import org.example.zenvybackend.user.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @EntityGraph(attributePaths = {"sellerOrders", "sellerOrders.items", "sellerOrders.seller"})
    Page<Order> findByCustomerOrderByCreatedAtDesc(Customer customer, Pageable pageable);

    @EntityGraph(attributePaths = {"sellerOrders", "sellerOrders.items", "sellerOrders.seller", "sellerOrders.seller.user"})
    Optional<Order> findByIdAndCustomerUserId(UUID id, UUID customerUserId);

    @EntityGraph(attributePaths = {"customer", "customer.user", "sellerOrders", "sellerOrders.items", "sellerOrders.seller", "sellerOrders.seller.user"})
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"customer", "customer.user", "sellerOrders", "sellerOrders.items", "sellerOrders.seller"})
    Optional<Order> findWithDetailsById(UUID id);
}
