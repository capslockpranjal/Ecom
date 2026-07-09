package org.example.zenvybackend.order.repository;

import org.example.zenvybackend.order.entity.ReturnRequest;
import org.example.zenvybackend.order.enums.ReturnStatus;
import org.example.zenvybackend.user.entity.Customer;
import org.example.zenvybackend.user.entity.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, UUID> {

    @Query("""
        select rr from ReturnRequest rr
        join fetch rr.orderItem oi
        join fetch rr.sellerOrder so
        where rr.customer = :customer
        order by rr.createdAt desc
    """)
    Page<ReturnRequest> findByCustomerOrderByCreatedAtDesc(Customer customer, Pageable pageable);

    @Query("""
        select rr from ReturnRequest rr
        join fetch rr.orderItem oi
        join fetch rr.sellerOrder so
        join fetch so.order o
        where so.seller = :seller
        order by rr.createdAt desc
    """)
    Page<ReturnRequest> findBySellerOrderByCreatedAtDesc(Seller seller, Pageable pageable);

    @Query("""
        select rr from ReturnRequest rr
        join fetch rr.orderItem
        join fetch rr.sellerOrder so
        join fetch so.order
        where rr.id = :id and rr.customer.userId = :customerUserId
    """)
    Optional<ReturnRequest> findByIdAndCustomerUserId(UUID id, UUID customerUserId);

    @Query("""
        select rr from ReturnRequest rr
        join fetch rr.orderItem
        join fetch rr.sellerOrder so
        join fetch so.order
        where rr.id = :id and so.seller.userId = :sellerUserId
    """)
    Optional<ReturnRequest> findByIdAndSellerUserId(UUID id, UUID sellerUserId);

    boolean existsByOrderItemIdAndStatusIn(UUID orderItemId, List<ReturnStatus> statuses);
}
