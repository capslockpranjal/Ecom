package org.example.zenvybackend.admin.controller;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.admin.dto.AdminCustomerResponse;
import org.example.zenvybackend.admin.dto.AdminSellerResponse;
import org.example.zenvybackend.admin.service.AdminService;
import org.example.zenvybackend.common.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/test")
    public String test() {
        return "Admin access granted";
    }

    @GetMapping("/customers")
    public ApiResponse<List<AdminCustomerResponse>> listCustomers(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection
    ) {

        return ApiResponse.success(
                "Customers fetched successfully",
                adminService.listCustomers(pageNo, pageSize, filter, sortDirection)
        );
    }

    @GetMapping("/sellers")
    public ApiResponse<List<AdminSellerResponse>> listSellers(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection
    ) {

        return ApiResponse.success(
                "Sellers fetched successfully",
                adminService.listSellers(pageNo, pageSize, filter, sortDirection)
        );
    }

    @PatchMapping("/customers/{id}/activate")
    public ApiResponse<Void> activateCustomer(@PathVariable UUID id) {

        adminService.activateCustomer(id);
        return ApiResponse.success("Customer activated successfully");
    }

    @PatchMapping("/customers/{id}/deactivate")
    public ApiResponse<Void> deactivateCustomer(@PathVariable UUID id) {

        adminService.deactivateCustomer(id);
        return ApiResponse.success("Customer deactivated successfully");
    }

    @PatchMapping("/sellers/{id}/activate")
    public ApiResponse<Void> activateSeller(@PathVariable UUID id) {

        adminService.activateSeller(id);
        return ApiResponse.success("Seller activated successfully");
    }

    @PatchMapping("/sellers/{id}/deactivate")
    public ApiResponse<Void> deactivateSeller(@PathVariable UUID id) {

        adminService.deactivateSeller(id);
        return ApiResponse.success("Seller deactivated successfully");
    }
}