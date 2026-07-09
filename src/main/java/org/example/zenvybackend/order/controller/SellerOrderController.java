package org.example.zenvybackend.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.i18n.MessageResolver;
import org.example.zenvybackend.common.response.ApiResponse;
import org.example.zenvybackend.common.response.PagedResponse;
import org.example.zenvybackend.order.dto.request.UpdateSellerOrderStatusRequest;
import org.example.zenvybackend.order.dto.response.OrderSummaryResponse;
import org.example.zenvybackend.order.dto.response.SellerOrderDetailResponse;
import org.example.zenvybackend.order.service.OrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/seller/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class SellerOrderController {

    private final OrderService orderService;
    private final MessageResolver messageResolver;

    @GetMapping
    public PagedResponse<OrderSummaryResponse> getOrders(
            @RequestParam(defaultValue = "0") int pageOffset,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return orderService.getSellerOrders(pageOffset, pageSize);
    }

    @GetMapping("/{sellerOrderId}")
    public ApiResponse<SellerOrderDetailResponse> getOrder(@PathVariable UUID sellerOrderId) {
        return ApiResponse.success(
                messageResolver.get("response.order.details", "Order details"),
                orderService.getSellerOrder(sellerOrderId)
        );
    }

    @PatchMapping("/{sellerOrderId}/status")
    public ApiResponse<String> updateStatus(
            @PathVariable UUID sellerOrderId,
            @Valid @RequestBody UpdateSellerOrderStatusRequest request
    ) {
        orderService.updateSellerOrderStatus(sellerOrderId, request);
        return ApiResponse.success(messageResolver.get("response.order.status_updated", "Order status updated"));
    }
}
