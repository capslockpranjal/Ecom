package org.example.zenvybackend.product.dto.request;

import lombok.Data;

@Data
public class UpdateProductRequest {

    private String name;

    private String description;

    private Boolean isCancellable;

    private Boolean isReturnable;
}
