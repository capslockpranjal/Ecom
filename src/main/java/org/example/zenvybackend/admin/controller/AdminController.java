package org.example.zenvybackend.admin.controller;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.admin.service.AdminService;
import org.example.zenvybackend.common.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/test")
    public String test(){
        return "Admin access granted";
    }

    @PatchMapping("/sellers/{id}/approve")
    public ApiResponse<Void> approveSeller(@PathVariable UUID id){

        adminService.approveSeller(id);

        return ApiResponse.success("Seller approved successfully");
    }
}