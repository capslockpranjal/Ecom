package org.example.zenvybackend.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariationResponse {

    private UUID id;
    private Integer quantityAvailable;
    private Double price;
    private Boolean isActive;

    private Map<String, String> metadata;

    private String primaryImage;

    private List<String> secondaryImages;
}
