package org.example.zenvybackend.cart.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.cart.dto.request.AddCartItemRequest;
import org.example.zenvybackend.cart.dto.request.UpdateCartItemRequest;
import org.example.zenvybackend.cart.dto.response.CartResponse;
import org.example.zenvybackend.cart.entity.Cart;
import org.example.zenvybackend.cart.entity.CartItem;
import org.example.zenvybackend.cart.mapper.CartMapper;
import org.example.zenvybackend.cart.repository.CartItemRepository;
import org.example.zenvybackend.cart.repository.CartRepository;
import org.example.zenvybackend.cart.service.CartService;
import org.example.zenvybackend.common.exception.BadRequestException;
import org.example.zenvybackend.common.exception.ResourceNotFoundException;
import org.example.zenvybackend.product.entity.Product;
import org.example.zenvybackend.product.entity.ProductVariation;
import org.example.zenvybackend.product.repository.ProductVariationRepository;
import org.example.zenvybackend.security.util.SecurityUtil;
import org.example.zenvybackend.user.entity.Customer;
import org.example.zenvybackend.user.entity.User;
import org.example.zenvybackend.user.repository.CustomerRepository;
import org.example.zenvybackend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ProductVariationRepository productVariationRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart() {
        return cartMapper.toResponse(getOrCreateCart());
    }

    @Override
    @Transactional
    public CartResponse addItem(AddCartItemRequest request) {
        Cart cart = getOrCreateCart();
        ProductVariation variation = loadAvailableVariation(request.getVariationId());

        validateStock(variation, request.getQuantity());

        CartItem existingItem = cartItemRepository
                .findByCartAndProductVariationAndIsDeletedFalse(cart, variation)
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            validateStock(variation, newQuantity);
            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .productVariation(variation)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(item);
            cartRepository.save(cart);
        }

        return cartMapper.toResponse(getOrCreateCart());
    }

    @Override
    @Transactional
    public CartResponse updateItem(UUID itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart();

        CartItem item = cartItemRepository.findByIdAndCartAndIsDeletedFalse(itemId, cart)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        ProductVariation variation = item.getProductVariation();
        validateStock(variation, request.getQuantity());
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        return cartMapper.toResponse(getOrCreateCart());
    }

    @Override
    @Transactional
    public CartResponse removeItem(UUID itemId) {
        Cart cart = getOrCreateCart();

        CartItem item = cartItemRepository.findByIdAndCartAndIsDeletedFalse(itemId, cart)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        return cartMapper.toResponse(getOrCreateCart());
    }

    @Override
    @Transactional
    public void clearCart() {
        Cart cart = getOrCreateCart();
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart() {
        Customer customer = getCurrentCustomer();
        return cartRepository.findByCustomerUserId(customer.getUserId())
                .orElseGet(() -> {
                    Cart cart = Cart.builder().customer(customer).build();
                    return cartRepository.save(cart);
                });
    }

    private Customer getCurrentCustomer() {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadRequestException("Customer account is not activated");
        }

        return customerRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("User is not a customer"));
    }

    private ProductVariation loadAvailableVariation(UUID variationId) {
        ProductVariation variation = productVariationRepository
                .findByIdAndIsDeletedFalseAndIsActiveTrue(variationId)
                .orElseThrow(() -> new ResourceNotFoundException("Product variation not found"));

        Product product = variation.getProduct();
        if (!Boolean.TRUE.equals(product.getIsActive()) || Boolean.TRUE.equals(product.getIsDeleted())) {
            throw new BadRequestException("Product is not available");
        }

        return variation;
    }

    private void validateStock(ProductVariation variation, int requestedQuantity) {
        if (requestedQuantity <= 0) {
            throw new BadRequestException("Quantity must be at least 1");
        }
        if (variation.getQuantityAvailable() < requestedQuantity) {
            throw new BadRequestException("Insufficient stock available");
        }
    }
}
