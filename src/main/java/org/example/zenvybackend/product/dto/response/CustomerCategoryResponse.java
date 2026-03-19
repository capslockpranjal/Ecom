package org.example.zenvybackend.product.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CustomerCategoryResponse {

    private UUID id;
    private String name;

    private List<MetadataFieldResponse> metadataFields;

    private List<String> brands;

    private Double minPrice;
    private Double maxPrice;
}
