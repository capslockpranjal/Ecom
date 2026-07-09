package org.example.zenvybackend.product.dto.request;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class CustomerProductFilterDto {

    private String brand;
    private Double minPrice;
    private Double maxPrice;
    private Map<String, String> metadata = new HashMap<>();
}
