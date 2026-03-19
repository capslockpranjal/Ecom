package org.example.zenvybackend.product.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.zenvybackend.product.dto.response.ProductVariationResponse;
import org.example.zenvybackend.product.entity.ProductVariation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ProductVariationMapper {

    private final ObjectMapper mapper = new ObjectMapper();

    public ProductVariationResponse toResponse(ProductVariation variation) {

        try {

            Map<String, String> metadata =
                    mapper.readValue(variation.getMetadata(), Map.class);

            List<String> images =
                    variation.getSecondaryImages() == null
                            ? new ArrayList<>()
                            : mapper.readValue(variation.getSecondaryImages(), List.class);

            return ProductVariationResponse.builder()
                    .id(variation.getId())
                    .quantityAvailable(variation.getQuantityAvailable())
                    .price(variation.getPrice())
                    .isActive(variation.getIsActive())
                    .metadata(metadata)
                    .primaryImage(variation.getPrimaryImageName())
                    .secondaryImages(images)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error parsing variation data");
        }
    }
}
