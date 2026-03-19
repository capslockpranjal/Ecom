package org.example.zenvybackend.product.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ProductResponse {

    private UUID id;
    private String name;
    private String description;
    private String brand;
    private Boolean isActive;
    private Boolean isCancellable;
    private Boolean isReturnable;

    // 🔥 Category details
    private UUID categoryId;
    private String categoryName;
}
