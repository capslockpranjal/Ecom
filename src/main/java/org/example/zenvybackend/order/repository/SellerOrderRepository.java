package org.example.zenvybackend.order.repository;

import org.example.zenvybackend.order.entity.SellerOrder;
import org.example.zenvybackend.user.entity.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SellerOrderRepository extends JpaRepository<SellerOrder, UUID> {

    Page<SellerOrder> findBySellerOrderByCreatedAtDesc(Seller seller, Pageable pageable);

    @Query("SELECT so FROM SellerOrder so WHERE so.id = :id AND so.seller.userId = :sellerUserId")
    Optional<SellerOrder> findByIdAndSellerUserId(
            @Param("id") UUID id,
            @Param("sellerUserId") UUID sellerUserId
    );

    @Query("""
            SELECT DISTINCT so FROM SellerOrder so
            LEFT JOIN FETCH so.items
            WHERE so.order.id IN :orderIds
            """)
    List<SellerOrder> findByOrderIdInWithItems(@Param("orderIds") Collection<UUID> orderIds);

    @Query("""
            SELECT DISTINCT so FROM SellerOrder so
            LEFT JOIN FETCH so.items
            WHERE so.id IN :sellerOrderIds
            """)
    List<SellerOrder> findByIdInWithItems(@Param("sellerOrderIds") Collection<UUID> sellerOrderIds);
}
