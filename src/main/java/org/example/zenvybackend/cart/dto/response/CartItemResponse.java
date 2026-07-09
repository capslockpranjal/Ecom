package org.example.zenvybackend.cart.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class CartItemResponse {

    private UUID id;
    private UUID variationId;
    private UUID productId;
    private String productName;
    private String brand;
    private Map<String, String> metadata;
    private Double unitPrice;
    private Integer quantity;
    private Double lineTotal;
    private Integer quantityAvailable;
    private String primaryImage;
}
