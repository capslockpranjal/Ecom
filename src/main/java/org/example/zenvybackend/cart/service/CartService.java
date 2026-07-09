package org.example.zenvybackend.cart.service;

import org.example.zenvybackend.cart.dto.request.AddCartItemRequest;
import org.example.zenvybackend.cart.dto.request.UpdateCartItemRequest;
import org.example.zenvybackend.cart.dto.response.CartResponse;

import java.util.UUID;

public interface CartService {

    CartResponse getCart();

    CartResponse addItem(AddCartItemRequest request);

    CartResponse updateItem(UUID itemId, UpdateCartItemRequest request);

    CartResponse removeItem(UUID itemId);

    void clearCart();
}
