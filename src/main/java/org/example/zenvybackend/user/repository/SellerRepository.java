package org.example.zenvybackend.user.repository;

import org.example.zenvybackend.user.entity.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SellerRepository extends JpaRepository<Seller, UUID> {

    boolean existsByGst(String gst);

    boolean existsByCompanyName(String companyName);

    @Query("select s from Seller s join fetch s.user u left join fetch u.addresses where s.userId = :id")
    Optional<Seller> findByIdWithUserAndAddresses(@Param("id") UUID id);

    @Query("select distinct s from Seller s join fetch s.user u left join fetch u.addresses")
    List<Seller> findAllWithUserAndAddresses();

    @EntityGraph(attributePaths = {"user", "user.addresses"})
    Page<Seller> findByUserEmailContainingIgnoreCase(String email, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "user.addresses"})
    Page<Seller> findAll(Pageable pageable);
}


