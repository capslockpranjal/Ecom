package org.example.zenvybackend.user.repository;

import org.example.zenvybackend.user.entity.Customer;
import org.example.zenvybackend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByUser(User user);

}