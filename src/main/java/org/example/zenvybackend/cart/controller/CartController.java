package org.example.zenvybackend.cart.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.cart.dto.request.AddCartItemRequest;
import org.example.zenvybackend.cart.dto.request.UpdateCartItemRequest;
import org.example.zenvybackend.cart.dto.response.CartResponse;
import org.example.zenvybackend.cart.service.CartService;
import org.example.zenvybackend.common.i18n.MessageResolver;
import org.example.zenvybackend.common.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/customer/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {

    private final CartService cartService;
    private final MessageResolver messageResolver;

    @GetMapping
    public ApiResponse<CartResponse> getCart() {
        return ApiResponse.success(
                messageResolver.get("response.cart.fetched", "Cart fetched"),
                cartService.getCart()
        );
    }

    @PostMapping("/items")
    public ApiResponse<CartResponse> addItem(@Valid @RequestBody AddCartItemRequest request) {
        return ApiResponse.success(
                messageResolver.get("response.cart.item_added", "Item added to cart"),
                cartService.addItem(request)
        );
    }

    @PatchMapping("/items/{itemId}")
    public ApiResponse<CartResponse> updateItem(
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return ApiResponse.success(
                messageResolver.get("response.cart.item_updated", "Cart item updated"),
                cartService.updateItem(itemId, request)
        );
    }

    @DeleteMapping("/items/{itemId}")
    public ApiResponse<CartResponse> removeItem(@PathVariable UUID itemId) {
        return ApiResponse.success(
                messageResolver.get("response.cart.item_removed", "Cart item removed"),
                cartService.removeItem(itemId)
        );
    }

    @DeleteMapping
    public ApiResponse<String> clearCart() {
        cartService.clearCart();
        return ApiResponse.success(messageResolver.get("response.cart.cleared", "Cart cleared"));
    }
}
