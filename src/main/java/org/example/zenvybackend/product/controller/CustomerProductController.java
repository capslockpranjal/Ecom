package org.example.zenvybackend.product.controller;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.category.dto.request.PageRequestDto;
import org.example.zenvybackend.common.i18n.MessageResolver;
import org.example.zenvybackend.common.response.ApiResponse;
import org.example.zenvybackend.product.dto.request.CustomerProductFilterDto;
import org.example.zenvybackend.product.dto.response.CustomerProductDetailResponse;
import org.example.zenvybackend.product.service.ProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/customer/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerProductController {

    private final ProductService productService;
    private final MessageResolver messageResolver;

    @GetMapping
    public ApiResponse<?> getProducts(
            @RequestParam UUID categoryId,
            @RequestParam(required = false) Integer max,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam Map<String, String> allParams
    ) {
        PageRequestDto dto = new PageRequestDto();
        dto.setMax(max);
        dto.setOffset(offset);
        dto.setSort(sort);
        dto.setOrder(order);

        CustomerProductFilterDto filter = new CustomerProductFilterDto();
        filter.setBrand(brand);
        filter.setMinPrice(minPrice);
        filter.setMaxPrice(maxPrice);

        Map<String, String> metadata = new HashMap<>();
        allParams.forEach((key, value) -> {
            if (key.startsWith("metadata.") && value != null && !value.isBlank()) {
                metadata.put(key.substring("metadata.".length()), value);
            }
        });
        filter.setMetadata(metadata);

        return ApiResponse.success(
                messageResolver.get("response.product.list", "Product list"),
                productService.getCustomerProducts(categoryId, dto, filter)
        );
    }

    @GetMapping("/{productId}")
    public ApiResponse<CustomerProductDetailResponse> getProduct(@PathVariable UUID productId) {
        return ApiResponse.success(
                messageResolver.get("response.product.details", "Product details"),
                productService.getCustomerProduct(productId)
        );
    }

    @GetMapping("/{productId}/similar")
    public ApiResponse<?> getSimilarProducts(
            @PathVariable UUID productId,
            @RequestParam(required = false) Integer max,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order
    ) {
        PageRequestDto dto = new PageRequestDto();
        dto.setMax(max);
        dto.setOffset(offset);
        dto.setSort(sort);
        dto.setOrder(order);

        return ApiResponse.success(
                messageResolver.get("response.product.similar", "Similar products"),
                productService.getSimilarCustomerProducts(productId, dto)
        );
    }
}
