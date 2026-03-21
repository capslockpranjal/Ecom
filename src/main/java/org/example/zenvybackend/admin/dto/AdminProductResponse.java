package org.example.zenvybackend.admin.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class AdminProductResponse {

    private UUID id;
    private String name;
    private String description;
    private String brand;
    private Boolean isCancellable;
    private Boolean isReturnable;
    private Boolean isActive;
    private UUID sellerId;
    private AdminProductCategoryResponse category;
    private List<String> primaryImages;
}
