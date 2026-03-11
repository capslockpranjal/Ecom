package org.example.zenvybackend.bootstrap;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.example.zenvybackend.common.constants.RoleConstants;
import org.example.zenvybackend.user.entity.Role;
import org.example.zenvybackend.user.entity.User;
import org.example.zenvybackend.user.repository.RoleRepository;
import org.example.zenvybackend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class BootstrapService {


    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;
    @PostConstruct
    public void init(){

        createRoles();
        createAdmin();
    }

    /* ---------------- CREATE ROLES ---------------- */

    private void createRoles(){

        createRoleIfNotExists(RoleConstants.ADMIN);
        createRoleIfNotExists(RoleConstants.CUSTOMER);
        createRoleIfNotExists(RoleConstants.SELLER);
    }

    private void createRoleIfNotExists(String roleName){

        if(roleRepository.findByAuthority(roleName).isEmpty()){

            Role role = new Role();
            role.setAuthority(roleName);

            roleRepository.save(role);
        }
    }

    /* ---------------- CREATE ADMIN ---------------- */

    private void createAdmin(){

        if(userRepository.existsByEmail(adminEmail)){
            return;
        }

        Role adminRole = roleRepository.findByAuthority("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("Admin role not found"));

        User admin = new User();

        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setIsActive(true);
        admin.setRoles(Set.of(adminRole));

        userRepository.save(admin);
    }
}