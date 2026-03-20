package org.example.zenvybackend.product.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.storage.ImageStorageService;
import org.example.zenvybackend.product.dto.response.ProductVariationResponse;
import org.example.zenvybackend.product.entity.ProductVariation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductVariationMapper {

    private final ObjectMapper mapper;
    private final ImageStorageService imageStorageService;

    public ProductVariationResponse toResponse(ProductVariation variation) {

        try {

            Map<String, String> metadata =
                    mapper.readValue(variation.getMetadata(), Map.class);

            List<String> images =
                    variation.getSecondaryImages() == null
                            ? new ArrayList<>()
                            : mapper.readValue(variation.getSecondaryImages(), List.class);

            String primaryImageUrl = imageStorageService.getVariationPrimaryImageUrl(
                    variation.getProduct().getId(),
                    variation.getId()
            );
            List<String> secondaryImageUrls = imageStorageService.getVariationSecondaryImageUrls(
                    variation.getProduct().getId(),
                    variation.getId()
            );

            return ProductVariationResponse.builder()
                    .id(variation.getId())
                    .quantityAvailable(variation.getQuantityAvailable())
                    .price(variation.getPrice())
                    .isActive(variation.getIsActive())
                    .metadata(metadata)
                    .primaryImage(primaryImageUrl != null ? primaryImageUrl : variation.getPrimaryImageName())
                    .secondaryImages(secondaryImageUrls.isEmpty() ? images : secondaryImageUrls)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error parsing variation data");
        }
    }
}
