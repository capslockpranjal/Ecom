package org.example.zenvybackend.cart.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.cart.dto.response.CartItemResponse;
import org.example.zenvybackend.cart.dto.response.CartResponse;
import org.example.zenvybackend.cart.entity.Cart;
import org.example.zenvybackend.cart.entity.CartItem;
import org.example.zenvybackend.common.storage.ImageStorageService;
import org.example.zenvybackend.product.entity.Product;
import org.example.zenvybackend.product.entity.ProductVariation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CartMapper {

    private final ObjectMapper objectMapper;
    private final ImageStorageService imageStorageService;

    public CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .filter(item -> !Boolean.TRUE.equals(item.getIsDeleted()))
                .map(this::toItemResponse)
                .toList();

        double subtotal = items.stream()
                .mapToDouble(CartItemResponse::getLineTotal)
                .sum();

        int itemCount = items.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();

        return CartResponse.builder()
                .id(cart.getId())
                .items(items)
                .subtotal(subtotal)
                .itemCount(itemCount)
                .build();
    }

    private CartItemResponse toItemResponse(CartItem item) {
        ProductVariation variation = item.getProductVariation();
        Product product = variation.getProduct();

        return CartItemResponse.builder()
                .id(item.getId())
                .variationId(variation.getId())
                .productId(product.getId())
                .productName(product.getName())
                .brand(product.getBrand())
                .metadata(parseMetadata(variation.getMetadata()))
                .unitPrice(variation.getPrice())
                .quantity(item.getQuantity())
                .lineTotal(variation.getPrice() * item.getQuantity())
                .quantityAvailable(variation.getQuantityAvailable())
                .primaryImage(imageStorageService.getVariationPrimaryImageUrl(product.getId(), variation.getId()))
                .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> parseMetadata(String metadata) {
        try {
            return objectMapper.readValue(metadata, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing variation metadata");
        }
    }
}
