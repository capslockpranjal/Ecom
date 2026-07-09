package org.example.zenvybackend.admin.controller;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.admin.dto.AdminCustomerResponse;
import org.example.zenvybackend.category.dto.request.PageRequestDto;
import org.example.zenvybackend.admin.dto.AdminSellerResponse;
import org.example.zenvybackend.admin.service.AdminService;
import org.example.zenvybackend.common.i18n.MessageResolver;
import org.example.zenvybackend.common.response.ApiResponse;
import org.example.zenvybackend.common.response.PagedResponse;
import org.example.zenvybackend.order.dto.response.OrderResponse;
import org.example.zenvybackend.order.dto.response.OrderSummaryResponse;
import org.example.zenvybackend.order.service.OrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final OrderService orderService;
    private final MessageResolver messageResolver;

    @GetMapping("/test")
    public String test() {
        return messageResolver.get("response.admin.access_granted", "Admin access granted");
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
        return ApiResponse.success(messageResolver.get("response.customer.activated", "Customer activated successfully"));
    }

    @PatchMapping("/customers/{id}/deactivate")
    public ApiResponse<Void> deactivateCustomer(@PathVariable UUID id) {

        adminService.deactivateCustomer(id);
        return ApiResponse.success(messageResolver.get("response.customer.deactivated", "Customer deactivated successfully"));
    }

    @PatchMapping("/sellers/{id}/activate")
    public ApiResponse<Void> activateSeller(@PathVariable UUID id) {

        adminService.activateSeller(id);
        return ApiResponse.success(messageResolver.get("response.seller.activated", "Seller activated successfully"));
    }

    @PatchMapping("/sellers/{id}/deactivate")
    public ApiResponse<Void> deactivateSeller(@PathVariable UUID id) {

        adminService.deactivateSeller(id);
        return ApiResponse.success(messageResolver.get("response.seller.deactivated", "Seller deactivated successfully"));
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
                messageResolver.get("response.product.list", "Product list"),
                adminService.getProducts(productId, sellerId, categoryId, dto)
        );
    }

    @PutMapping("/products/{id}/activate")
    public ApiResponse<Void> activateProduct(@PathVariable UUID id) {
        adminService.activateProduct(id);
        return ApiResponse.success(messageResolver.get("response.product.activated", "Product activated successfully"));
    }

    @PutMapping("/products/{id}/deactivate")
    public ApiResponse<Void> deactivateProduct(@PathVariable UUID id) {
        adminService.deactivateProduct(id);
        return ApiResponse.success(messageResolver.get("response.product.deactivated", "Product deactivated successfully"));
    }

    @GetMapping("/orders")
    public PagedResponse<OrderSummaryResponse> getOrders(
            @RequestParam(defaultValue = "0") int pageOffset,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return orderService.getAdminOrders(pageOffset, pageSize);
    }

    @GetMapping("/orders/{orderId}")
    public ApiResponse<OrderResponse> getOrder(@PathVariable UUID orderId) {
        return ApiResponse.success(
                messageResolver.get("response.order.details", "Order details"),
                orderService.getAdminOrder(orderId)
        );
    }
}
