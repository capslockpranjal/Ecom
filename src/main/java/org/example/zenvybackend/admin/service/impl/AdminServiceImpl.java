package org.example.zenvybackend.admin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.admin.service.AdminService;
import org.example.zenvybackend.common.exception.BadRequestException;
import org.example.zenvybackend.common.exception.ResourceNotFoundException;
import org.example.zenvybackend.user.entity.User;
import org.example.zenvybackend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    @Override
    public void approveSeller(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        boolean isSeller = user.getRoles()
                .stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_SELLER"));

        if(!isSeller){
            throw new BadRequestException("User is not a seller");
        }

        userRepository.save(user);
    }
}