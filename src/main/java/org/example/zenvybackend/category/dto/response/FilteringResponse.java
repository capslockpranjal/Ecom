package org.example.zenvybackend.category.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilteringResponse {

    private List<MetadataFieldResponse> metadata;

    private List<String> brands;

    private Double minPrice;
    private Double maxPrice;
}
