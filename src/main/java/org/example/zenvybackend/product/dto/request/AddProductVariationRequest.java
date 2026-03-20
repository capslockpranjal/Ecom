package org.example.zenvybackend.product.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class AddProductVariationRequest {

    @NotNull
    private UUID productId;

    @NotNull
    @Min(0)
    private Integer quantityAvailable;

    @NotNull
    @DecimalMin("0.0")
    private Double price;

    private String primaryImageName;

    @NotNull
    private Map<String, String> metadata;

    private List<String> secondaryImages;
}
