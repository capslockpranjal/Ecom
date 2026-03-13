package org.example.zenvybackend.user.repository;

import org.example.zenvybackend.user.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SellerRepository extends JpaRepository<Seller, UUID> {
    boolean existsByGst(String gst);
}

