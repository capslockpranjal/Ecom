package org.example.zenvybackend.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AddCartItemRequest {

    @NotNull
    private UUID variationId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
