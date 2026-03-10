package org.example.zenvybackend.admin.controller;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.response.ApiResponse;
import org.example.zenvybackend.user.entity.User;
import org.example.zenvybackend.user.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/test")
    public String test(){
        return "Admin access granted";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/approve-seller")
    public ApiResponse<String> approveSeller(@RequestParam String email){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        user.setIsActive(true);

        userRepository.save(user);

        return ApiResponse.success("Seller approved successfully");
    }
}