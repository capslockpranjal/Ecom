package org.example.zenvybackend.product.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateProductVariationRequest {

    private Integer quantityAvailable;

    private Double price;

    private String metadata;

    private String primaryImageName;

    private List<String> secondaryImages;

    private Boolean isActive;
}
