package org.example.zenvybackend.product.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FilteringResponse {

    private List<MetadataFieldResponse> metadata;

    private List<String> brands;

    private Double minPrice;
    private Double maxPrice;
}
