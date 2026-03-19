package org.example.zenvybackend.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AddProductRequest {

    @NotBlank
    private String name;

    @NotNull
    private UUID categoryId;

    @NotBlank
    private String brand;

    private String description;

    private Boolean isCancellable;

    private Boolean isReturnable;
}
