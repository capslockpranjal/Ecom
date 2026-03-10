package org.example.zenvybackend.user.repository;

import org.example.zenvybackend.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByAuthority(String authority);
}
