package org.example.zenvybackend.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.i18n.MessageResolver;
import org.example.zenvybackend.common.response.ApiResponse;
import org.example.zenvybackend.common.response.PagedResponse;
import org.example.zenvybackend.order.dto.request.CheckoutRequest;
import org.example.zenvybackend.order.dto.response.OrderResponse;
import org.example.zenvybackend.order.dto.response.OrderSummaryResponse;
import org.example.zenvybackend.order.service.OrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/customer/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerOrderController {

    private final OrderService orderService;
    private final MessageResolver messageResolver;

    @PostMapping("/checkout")
    public ApiResponse<OrderResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        return ApiResponse.success(
                messageResolver.get("response.order.placed", "Order placed successfully"),
                orderService.checkout(request)
        );
    }

    @GetMapping
    public PagedResponse<OrderSummaryResponse> getOrders(
            @RequestParam(defaultValue = "0") int pageOffset,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return orderService.getCustomerOrders(pageOffset, pageSize);
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrder(@PathVariable UUID orderId) {
        return ApiResponse.success(
                messageResolver.get("response.order.details", "Order details"),
                orderService.getCustomerOrder(orderId)
        );
    }

    @PostMapping("/{orderId}/cancel")
    public ApiResponse<String> cancelOrder(@PathVariable UUID orderId) {
        orderService.cancelCustomerOrder(orderId);
        return ApiResponse.success(messageResolver.get("response.order.cancelled", "Order cancelled"));
    }

    @PostMapping("/{orderId}/confirm-payment")
    public ApiResponse<OrderResponse> confirmPayment(@PathVariable UUID orderId) {
        return ApiResponse.success(
                messageResolver.get("response.payment.confirmed", "Payment confirmed"),
                orderService.confirmPayment(orderId)
        );
    }
}
