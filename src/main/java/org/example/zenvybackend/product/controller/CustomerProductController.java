package org.example.zenvybackend.product.controller;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.category.dto.request.PageRequestDto;
import org.example.zenvybackend.common.response.ApiResponse;
import org.example.zenvybackend.product.dto.response.CustomerProductDetailResponse;
import org.example.zenvybackend.product.service.ProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/customer/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<?> getProducts(
            @RequestParam UUID categoryId,
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
                "Product list",
                productService.getCustomerProducts(categoryId, dto)
        );
    }

    @GetMapping("/{productId}")
    public ApiResponse<CustomerProductDetailResponse> getProduct(@PathVariable UUID productId) {
        return ApiResponse.success(
                "Product details",
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
                "Similar products",
                productService.getSimilarCustomerProducts(productId, dto)
        );
    }
}
