package org.example.zenvybackend.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class OrderItemResponse {

    private UUID id;
    private UUID productVariationId;
    private UUID productId;
    private String productName;
    private String brand;
    private Map<String, String> metadata;
    private Integer quantity;
    private Double unitPrice;
    private Double lineTotal;
}
