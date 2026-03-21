package org.example.zenvybackend.product.dto.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UpdateProductVariationRequest {

    private Integer quantityAvailable;

    private Double price;

    private Map<String, String> metadata;

    private String primaryImageName;

    private List<String> secondaryImages;

    private Boolean isActive;
}
