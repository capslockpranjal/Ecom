package org.example.zenvybackend.user.repository;

import org.example.zenvybackend.user.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findByUserId(UUID userId);


    Optional<Address> findByIdAndUserId(UUID addressId, UUID userId);

    boolean existsByUserIdAndIsDeletedFalse(UUID userId);

    Optional<Address> findFirstByUserId(UUID userId);



}
