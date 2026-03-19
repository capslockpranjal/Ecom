package org.example.zenvybackend.product.mapper;

import org.example.zenvybackend.product.dto.response.ProductResponse;
import org.example.zenvybackend.product.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {

        if (product == null) return null;

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .brand(product.getBrand())
                .isActive(product.getIsActive())
                .isCancellable(product.getIsCancellable())
                .isReturnable(product.getIsReturnable())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .build();
    }
}
