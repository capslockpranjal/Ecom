package org.example.zenvybackend.category.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCategoryResponse {

    private UUID id;
    private String name;

    private List<MetadataFieldResponse> metadataFields;

    private List<String> brands;

    private Double minPrice;
    private Double maxPrice;
}
