package org.example.zenvybackend.admin.controller;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.admin.dto.AdminCustomerResponse;
import org.example.zenvybackend.category.dto.request.PageRequestDto;
import org.example.zenvybackend.admin.dto.AdminSellerResponse;
import org.example.zenvybackend.admin.service.AdminService;
import org.example.zenvybackend.common.response.ApiResponse;
import org.example.zenvybackend.common.response.PagedResponse;
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
    public String test() {
        return "Admin access granted";
    }

    @GetMapping("/customers")
    public PagedResponse<AdminCustomerResponse> getCustomers(

            @RequestParam(defaultValue = "0") int pageOffset,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "user.id") String sort,
            @RequestParam(required = false) String email
    ) {

        return adminService.listCustomers(pageOffset, pageSize, sort, email);
    }

    @GetMapping("/sellers")
    public PagedResponse<AdminSellerResponse> getSellers(

            @RequestParam(defaultValue = "0") int pageOffset,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "user.id") String sort,
            @RequestParam(required = false) String email
    ) {

        return adminService.listSellers(pageOffset, pageSize, sort, email);
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

    @GetMapping("/products")
    public ApiResponse<?> getProducts(
            @RequestParam(required = false) Integer max,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) UUID sellerId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID productId
    ) {
        PageRequestDto dto = new PageRequestDto();
        dto.setMax(max);
        dto.setOffset(offset);
        dto.setSort(sort);
        dto.setOrder(order);

        return ApiResponse.success(
                "Product list",
                adminService.getProducts(productId, sellerId, categoryId, dto)
        );
    }

    @PutMapping("/products/{id}/activate")
    public ApiResponse<Void> activateProduct(@PathVariable UUID id) {
        adminService.activateProduct(id);
        return ApiResponse.success("Product activated successfully");
    }

    @PutMapping("/products/{id}/deactivate")
    public ApiResponse<Void> deactivateProduct(@PathVariable UUID id) {
        adminService.deactivateProduct(id);
        return ApiResponse.success("Product deactivated successfully");
    }
}
