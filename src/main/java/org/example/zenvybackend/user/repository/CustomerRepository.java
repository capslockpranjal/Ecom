package org.example.zenvybackend.user.repository;

import org.example.zenvybackend.user.entity.Customer;
import org.example.zenvybackend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    @Query("""
select c
from Customer c
join fetch c.user u
where c.userId = :id
""")
    Optional<Customer> findByIdWithUser(@Param("id") UUID id);

    Optional<Customer> findByUser(User user);

    @EntityGraph(attributePaths = {"user"})
    Page<Customer> findByUserEmailContainingIgnoreCase(String email, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Page<Customer> findAll(Pageable pageable);

}