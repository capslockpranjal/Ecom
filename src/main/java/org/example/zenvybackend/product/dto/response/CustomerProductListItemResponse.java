package org.example.zenvybackend.product.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CustomerProductListItemResponse {

    private UUID id;
    private String name;
    private String description;
    private String brand;
    private Boolean isCancellable;
    private Boolean isReturnable;
    private CustomerProductCategoryResponse category;
    private List<String> primaryImages;
}
